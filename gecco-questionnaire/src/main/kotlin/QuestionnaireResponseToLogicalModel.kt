import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import org.hl7.fhir.r4.model.*
import java.io.FileReader
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaSetter
import kotlin.reflect.typeOf

fun main() {
    val jsonParser = FhirContext.forR4().newJsonParser().setPrettyPrint(true)
    val questionnaire =
        jsonParser.parseResource(FileReader("./././questionnaire.json")) as Questionnaire
    val questionnaireResponse = generateResponse(questionnaire)

    val logicalModel = toLogicalModel(questionnaire, questionnaireResponse)

    println(logicalModel)

}

fun toLogicalModel(questionnaire: Questionnaire, qr: QuestionnaireResponse): LogicalModel {
    addExtensions(qr, questionnaire)
    val mapped = qItemByGeccoId(qr).filter { it.value.value != null } as HashMap
    val processedMap = processMappedCodes(mapped)

    val logicalModel = LogicalModel()

    for (item in processedMap) {
        setValueForCode(logicalModel, item.key, item.value)
    }

    return logicalModel
}

fun toYesNoUnknown(value: Type?): YesNoUnknown? {
    if (value == null) {
        return null
    }
    if (value is Coding) {
        val result = getByCoding<YesNoUnknown>(value)
        if (result != null) {
            return result
        }
    }
    if (value is BooleanType) {
        return if (value.booleanValue()) YesNoUnknown.YES else YesNoUnknown.NO
    }
    val answerValue = when (value) {
        is StringType -> value.toString()
        is Coding -> value.display?.toString()
        else -> null
    }
    return when (answerValue?.lowercase()) {
        "ja", "yes" -> YesNoUnknown.YES
        "nein", "no" -> YesNoUnknown.NO
        "unbekannt", "unknown" -> YesNoUnknown.UNKNOWN
        else -> null
    }
}


fun toYesNoUnknownOtherNa(value: Type?): YesNoUnknownOtherNa? {
    if (value == null) {
        return null
    }
    if (value is Coding) {
        val result = getByCoding<YesNoUnknownOtherNa>(value)
        if (result != null) {
            return result
        }
    }

    val answerValue = when (value) {
        is StringType -> value.toString()
        is Coding -> value.display?.toString()
        else -> null
    }
    return when (answerValue?.lowercase()) {
        "ja", "yes" -> YesNoUnknownOtherNa.YES
        "nein", "no" -> YesNoUnknownOtherNa.NO
        "andere", "other" -> YesNoUnknownOtherNa.OTHER
        "na", "not applicable", "nicht anwendbar", "unzutreffend" -> YesNoUnknownOtherNa.NA
        "unbekannt", "unknown" -> YesNoUnknownOtherNa.UNKNOWN
        else -> null
    }
}

fun toDate(value: Type?): Date? {
    if (value is DateType) {
        val format = getSimpleDateFormatterForPrecision(value.precision)
        return format?.parse(value.valueAsString)
    }

    return null
}

fun toLocalDate(value: Type): LocalDate? {
    if (value is DateType) {
        val format = getSimpleDateFormatterForPrecision(value.precision)
        return format?.parse(value.valueAsString)?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
    }

    return null
}

fun getSimpleDateFormatterForPrecision(precision: TemporalPrecisionEnum): SimpleDateFormat? {
    return when (precision.name) {
        "YEAR" -> SimpleDateFormat("yyyy")
        "MONTH" -> SimpleDateFormat("yyyy-MM")
        "DAY" -> SimpleDateFormat("yyyy-MM-dd")
        "MINUTE" -> SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
        "SECOND" -> SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        "MILLISECOND" -> SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        else -> null
    }
}

fun toFloat(value: Type): Float? {
    if (value is DecimalType) {
        return value.value.toFloat()
    }
    return null
}

fun toInt(value: Type?): Int? {
    if (value is IntegerType) {
        return value.value.toInt()
    }
    return null
}

fun toStringValue(value: Type): String? {
    if (value is StringType) {
        return value.valueAsString
    }

    return null
}


fun setValueForCode(
    logicalModel: LogicalModel,
    code: String,
    value: Any,
    subProperties: Any? = null,
    layer: Int = 0
) {
    val splittedCode = code.split(".")

    val subProps: Any?
    val instance: Any?

    if (subProperties != null) {
        subProps = subProperties.getPropertyThroughReflection(splittedCode[layer])
        instance = subProperties.getInstanceThroughReflection(splittedCode[layer])
    } else {
        subProps = logicalModel.getPropertyThroughReflection(splittedCode[layer])
        instance = logicalModel.getInstanceThroughReflection(splittedCode[layer])
    }

    if (subProps != null) {
        if (splittedCode.size > layer + 1) {
            //Walk through Logical model until all codes are used
            setValueForCode(logicalModel, code, value, instance, layer + 1)
        } else {
            try {
                //Terminal value. Call setter on parent
                subProperties?.setPropertyThroughReflection((subProps as KMutableProperty<*>).name, value)
            } catch (e: Exception) {
                println("Could not set value for code $code with type ${value.javaClass.simpleName}; Expected ${(subProps as KMutableProperty<*>).returnType}")
            }
        }
    }
}

fun Any.getPropertyThroughReflection(propertyName: String): Any? {
    return try {
        this::class.members.find { it.name == propertyName }
    } catch (e: NoSuchMethodException) {
        null
    }
}

inline fun <reified T : Any> Any.getInstanceThroughReflection(propertyName: String): T? {
    val getterName =
        "get" + propertyName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    return try {
        javaClass.getMethod(getterName).invoke(this) as? T
    } catch (e: NoSuchMethodException) {
        null
    }
}

fun Any.setPropertyThroughReflection(propertyName: String, value: Any) {
    for (prop in this::class.declaredMemberProperties) {
        if (prop.name == propertyName) {
            (prop as? KMutableProperty<*>)?.setter?.call(this, value)
        }
    }
}

data class ProcessingHolder(var map: HashMap<String, QRAnswer>, var result: HashMap<String, Any>)

fun ProcessingHolder.addResultAndFilterCodes(
    resultsToAdd: Map<String, Any>,
    subCodes: Map<String, List<MutableMap.MutableEntry<String, QRAnswer>>>? = null
) {
    this.result.putAll(resultsToAdd)
    this.map = this.map.filter { !this.result.containsKey(it.key) } as HashMap<String, QRAnswer>

    if (subCodes != null) {
        for (entry in subCodes.entries) {
            for (subCode in entry.value)
                this.map.remove(subCode.key)
        }
    }

}

fun processMappedCodes(map: HashMap<String, QRAnswer>): HashMap<String, Any> {
    val processingHolder = ProcessingHolder(map, HashMap())
    //Order somewhat important as logic used to find codes overlaps

    val specialTypes = getSpecialTypeToCodeAndAnswer(processingHolder.map)
    processingHolder.addResultAndFilterCodes(createSpecialTypeForCodeAndAnswer(specialTypes))

    val coronaCodes = findCovid19VaccinationCodes(processingHolder.map)
    processingHolder.addResultAndFilterCodes(createCovid19VaccinationFromCodes(coronaCodes), coronaCodes)

    val statusWithDate = findYesNoUnknownWithDateCodes(processingHolder.map)
    processingHolder.addResultAndFilterCodes(createYesNoUnknownWithDateFromCodes(statusWithDate), statusWithDate)

    val symptomSeverity = findYesNoUnknownWithSymptomSeverityCodes(processingHolder.map)
    processingHolder.addResultAndFilterCodes(
        createYesNoUnknownWithSymptomSeverityFromCodes(symptomSeverity),
        symptomSeverity
    )

    val yesNoUnknownCodes = findYesNoUnknownCodes(processingHolder.map)
    processingHolder.addResultAndFilterCodes(createYesNoUnknownFromCodes(yesNoUnknownCodes), yesNoUnknownCodes)

    val yesNoUnknownOtherNaCodes = findYesNoUnknownOtherNaCodes(processingHolder.map)
    processingHolder.addResultAndFilterCodes(
        createYesNoUnknownOtherNaFromCodes(yesNoUnknownOtherNaCodes),
        yesNoUnknownOtherNaCodes
    )

    val yesNoUnknownWithIntent = findYesNoUnknownWithIntentCodes(processingHolder.map)
    processingHolder.addResultAndFilterCodes(
        createYesNoUnknownWithIntentFromCodes(yesNoUnknownWithIntent),
        yesNoUnknownWithIntent
    )

    val valueCodes = findValueCodes(processingHolder.map)
    processingHolder.addResultAndFilterCodes(createValuesFromCodes(valueCodes), valueCodes)

    return processingHolder.result
}

data class SpecialEnumTypeToCodeAndAnswer(val clazz: KClass<*>?, val code: String, val answer: QRAnswer)

fun getSpecialTypesFromLogicalModelWithPath(subProperties: Any, path: String = ""): Map<String, KClass<*>> {
    val map = HashMap<String, KClass<*>>()
    var newPath: String
    subProperties.javaClass.kotlin.memberProperties.forEach {
        val propertyValue = it.get(subProperties)

        newPath = if (path.isNotEmpty()) {
            "$path.${it.name}"
        } else {
            it.name
        }

        if (propertyValue?.javaClass?.kotlin?.memberProperties?.isNotEmpty() == true) {
            map.putAll(getSpecialTypesFromLogicalModelWithPath(propertyValue, newPath))
        } else {
            when (it.returnType.toString()) {
                "YesNoUnknown?" -> {}
                "YesNoUnknownWithIntent?" -> {}
                "YesNoUnknownOtherNa?" -> {}
                "YesNoUnknownWithSymptomSeverity?" -> {}
                "YesNoUnknownWithDate?" -> {}
                "Covid19Vaccine?" -> {}
                "kotlin.Float?" -> {}
                "java.time.LocalDate?" -> {}
                "kotlin.Int?" -> {}
                "kotlin.String?" -> {}
                //Only for classes that cannot be inferred by logic
                else -> {
                    map[newPath] = it.returnType.classifier as KClass<*>
                }
            }
        }
    }
    return map
}

fun getSpecialTypeToCodeAndAnswer(map: HashMap<String, QRAnswer>): List<SpecialEnumTypeToCodeAndAnswer> {
    val result = mutableListOf<SpecialEnumTypeToCodeAndAnswer>()
    val codesToFind = getSpecialTypesFromLogicalModelWithPath(LogicalModel())

    map.filter {
        codesToFind.containsKey(it.key)
    }.forEach {
        result.add(SpecialEnumTypeToCodeAndAnswer(codesToFind[it.key], it.key, it.value))
    }
    return result
}

fun createSpecialTypeForCodeAndAnswer(list: List<SpecialEnumTypeToCodeAndAnswer>): Map<String, Any> {
    val result = HashMap<String, Any>()
    for (entry in list) {
        try {
            when (entry.clazz) {
                typeOf<ChronicKidneyDisease>().classifier -> {
                    result[entry.code] = getByCoding2<ChronicKidneyDisease>(entry.answer.valueCoding)!!
                }

                typeOf<Diabetes>().classifier -> {
                    result[entry.code] = getByCoding2<Diabetes>(entry.answer.valueCoding)!!
                }

                typeOf<Countries>().classifier -> {
                    result[entry.code] = getByCoding<Countries>(entry.answer.valueCoding)!!
                }

                typeOf<FederalStates>().classifier -> {
                    result[entry.code] = getByCoding<FederalStates>(entry.answer.valueCoding)!!
                }

                typeOf<CancerStatus>().classifier -> {
                    result[entry.code] = getByCoding<CancerStatus>(entry.answer.valueCoding)!!
                }

                typeOf<Resuscitation>().classifier -> {
                    result[entry.code] = getByCoding<Resuscitation>(entry.answer.valueCoding)!!
                }

                typeOf<SmokingStatus>().classifier -> {
                    result[entry.code] = getByCoding2<SmokingStatus>(entry.answer.valueCoding)!!
                }

                typeOf<BirthSex>().classifier -> {
                    result[entry.code] = getByCoding<BirthSex>(entry.answer.valueCoding)!!
                }

                typeOf<EthnicGroup>().classifier -> {
                    result[entry.code] = getByCoding<EthnicGroup>(entry.answer.valueCoding)!!
                }

                typeOf<FrailityScore>().classifier -> {
                    result[entry.code] = getByCoding2<FrailityScore>(entry.answer.valueCoding)!!
                }

                typeOf<PregnancyStatus>().classifier -> {
                    result[entry.code] = getByCoding2<PregnancyStatus>(entry.answer.valueCoding)!!
                }

                typeOf<RadiologicFindings>().classifier -> {
                    result[entry.code] = getByCoding2<RadiologicFindings>(entry.answer.valueCoding)!!
                }

                typeOf<ACEInhibitorAdministration>().classifier -> {
                    result[entry.code] = getByCoding<ACEInhibitorAdministration>(entry.answer.valueCoding)!!
                }

                typeOf<StageAtDiagnosis>().classifier -> {
                    result[entry.code] = getByCoding2<StageAtDiagnosis>(entry.answer.valueCoding)!!
                }

                typeOf<DetectedNotDetectedInconclusive>().classifier -> {
                    result[entry.code] = getByCoding2<DetectedNotDetectedInconclusive>(entry.answer.valueCoding)!!
                }

                typeOf<TypeOfDischarge>().classifier -> {
                    result[entry.code] = getByCoding2<TypeOfDischarge>(entry.answer.valueCoding)!!
                }

                typeOf<VentilationTypes>().classifier -> {
                    result[entry.code] = getByCoding<VentilationTypes>(entry.answer.valueCoding)!!
                }
            }
        } catch (e: Exception) {
            println("Could not fetch correct Enum type. QuestionnaireResponse is probably faulty.")
        }
    }

    return result
}

fun findCovid19VaccinationCodes(map: HashMap<String, QRAnswer>): Map<String, List<MutableMap.MutableEntry<String, QRAnswer>>> {
    return map.entries.filter {
        it.key.substringAfterLast(".").contains("date") ||
                it.key.substringAfterLast(".").contains("status") ||
                it.key.substringAfterLast(".").contains("vaccine")
    }.groupBy { it.key.substringBeforeLast(".") }
        .filter { it.value.size > 2 }
}

fun createCovid19VaccinationFromCodes(map: Map<String, List<MutableMap.MutableEntry<String, QRAnswer>>>): Map<String, Any> {
    val result = mutableMapOf<String, Any>()
    for (code in map) {
        val coding = Covid19Vaccination()
        for (subcode in code.value) {
            if (subcode.key.contains("status")) {
                coding.status = toYesNoUnknown(subcode.value.valueCoding)
            } else if (subcode.key.contains("date")) {
                coding.date = toLocalDate(subcode.value.value)
            } else if (subcode.key.contains("vaccine")) {
                coding.vaccine = getByCoding<Covid19Vaccine>(subcode.value.valueCoding)
            }
        }
        result[code.key] = coding
    }
    return result
}

fun findYesNoUnknownWithDateCodes(map: HashMap<String, QRAnswer>): Map<String, List<MutableMap.MutableEntry<String, QRAnswer>>> {
    return map.entries.filter {
        it.key.substringAfterLast(".").contains("date") ||
                it.key.substringAfterLast(".").contains("status")
    }.groupBy { it.key.substringBeforeLast(".") }
        .filter { it.value.size > 1 }
}

fun createYesNoUnknownWithDateFromCodes(map: Map<String, List<MutableMap.MutableEntry<String, QRAnswer>>>): Map<String, Any> {
    val result = mutableMapOf<String, Any>()
    for (code in map) {
        val coding = YesNoUnknownWithDate()
        for (subcode in code.value) {
            if (subcode.key.contains("status")) {
                coding.yesNoUnknown = toYesNoUnknown(subcode.value.valueCoding)
            } else if (subcode.key.contains("date")) {
                coding.date = toDate(subcode.value.value)
            }
        }
        result[code.key] = coding
    }
    return result
}


fun findYesNoUnknownWithSymptomSeverityCodes(map: HashMap<String, QRAnswer>): Map<String, List<MutableMap.MutableEntry<String, QRAnswer>>> {
    return map.entries.filter {
        it.key.substringAfterLast(".").contains("presence") ||
                it.key.substringAfterLast(".").contains("severity")
    }.groupBy { it.key.substringBeforeLast(".") }
        .filter { it.value.size > 1 }
}

fun createYesNoUnknownWithSymptomSeverityFromCodes(map: Map<String, List<MutableMap.MutableEntry<String, QRAnswer>>>): HashMap<String, Any> {
    val result = HashMap<String, Any>()
    for (code in map) {
        val coding = YesNoUnknownWithSymptomSeverity()
        for (subcode in code.value) {
            if (subcode.key.contains("severity")) {
                coding.severity = getByCoding<SymptomSeverity>(subcode.value.valueCoding)
            } else if (subcode.key.contains("presence")) {
                coding.yesNoUnknown = toYesNoUnknown(subcode.value.valueCoding)
            }
        }
        result[code.key] = coding
    }
    return result
}

fun findYesNoUnknownCodes(map: HashMap<String, QRAnswer>): Map<String, List<MutableMap.MutableEntry<String, QRAnswer>>> {
    return map.entries.filter {
        it.value.value is Coding &&
                (
                        (it.value.value as Coding).code == YesNoUnknown.YES.coding.code ||
                                (it.value.value as Coding).code == YesNoUnknown.NO.coding.code ||
                                (it.value.value as Coding).code == YesNoUnknown.UNKNOWN.coding.code
                        )
    }.groupBy { it.key }
}

fun createYesNoUnknownFromCodes(map: Map<String, List<MutableMap.MutableEntry<String, QRAnswer>>>): Map<String, Any> {
    val result = HashMap<String, Any>()
    for (code in map) {
        // Should always have one entry, but to be consistent in syntax
        for (subcode in code.value) {
            result[code.key] = toYesNoUnknown(subcode.value.valueCoding)!!
        }
    }
    return result
}

fun findYesNoUnknownOtherNaCodes(map: HashMap<String, QRAnswer>): Map<String, List<MutableMap.MutableEntry<String, QRAnswer>>> {
    return map.entries.filter {
        it.value.value is Coding &&
                (
                        (it.value.value as Coding).code == YesNoUnknownOtherNa.YES.coding.code ||
                                (it.value.value as Coding).code == YesNoUnknownOtherNa.NO.coding.code ||
                                (it.value.value as Coding).code == YesNoUnknownOtherNa.UNKNOWN.coding.code ||
                                (it.value.value as Coding).code == YesNoUnknownOtherNa.OTHER.coding.code ||
                                (it.value.value as Coding).code == YesNoUnknownOtherNa.NA.coding.code
                        )
    }.groupBy { it.key }
}

fun createYesNoUnknownOtherNaFromCodes(map: Map<String, List<MutableMap.MutableEntry<String, QRAnswer>>>): Map<String, Any> {
    val result = HashMap<String, Any>()
    for (code in map) {
        // Should always have one entry, but to be consistent in syntax
        for (subcode in code.value) {
            result[code.key] = toYesNoUnknownOtherNa(subcode.value.valueCoding)!!
        }
    }
    return result
}

fun findYesNoUnknownWithIntentCodes(map: HashMap<String, QRAnswer>): Map<String, List<MutableMap.MutableEntry<String, QRAnswer>>> {
    return map.entries.filter {
        it.key.substringAfterLast(".").contains("intent") ||
                it.key.substringAfterLast(".").contains("administration")
    }.groupBy { it.key.substringBeforeLast(".") }
        .filter { it.value.size > 1 }
}

fun createYesNoUnknownWithIntentFromCodes(map: Map<String, List<MutableMap.MutableEntry<String, QRAnswer>>>): Map<String, Any> {
    val result = HashMap<String, Any>()
    for (code in map) {
        val coding = YesNoUnknownWithIntent()
        for (subcode in code.value) {
            if (subcode.key.contains("intent")) {
                coding.intent = getByCoding2<TherapeuticIntent>(subcode.value.valueCoding)
            } else if (subcode.key.contains("administration")) {
                coding.administration = toYesNoUnknown(subcode.value.valueCoding)
            }
        }
        result[code.key] = coding
    }
    return result
}

fun findValueCodes(map: HashMap<String, QRAnswer>): Map<String, List<MutableMap.MutableEntry<String, QRAnswer>>> {
    return map.entries.filter {
        it.value.value is IntegerType ||
                it.value.value is DecimalType ||
                it.value.value is StringType ||
                it.value.value is DateType
    }.groupBy { it.key }
}

fun createValuesFromCodes(map: Map<String, List<MutableMap.MutableEntry<String, QRAnswer>>>): Map<String, Any> {
    val result = HashMap<String, Any>()
    for (code in map) {
        for (subcode in code.value) {
            val value = when (subcode.value.value::class) {
                StringType::class -> toStringValue(subcode.value.value)
                DateType::class -> toLocalDate(subcode.value.value)
                IntegerType::class -> toInt(subcode.value.value)
                DecimalType::class -> toFloat(subcode.value.value)
                else -> null
            }
            result[code.key] = value!!
        }
    }
    return result
}


fun extractLab(mapByExtension: HashMap<String, QRAnswer>): LaboratoryValuesLaboratoryValue {
    val result = LaboratoryValuesLaboratoryValue()
    for (property in result::class.declaredMemberProperties) {
        if (property is KMutableProperty<*>) {
            val valueRaw = mapByExtension["laboratoryValues.laboratoryValues.${property.name}"]?.value
            try {
                property.javaSetter!!.invoke(
                    result, when (valueRaw) {
                        is DecimalType -> valueRaw.value.toFloat()
                        is StringType -> valueRaw.value
                        is Coding -> toYesNoUnknown(valueRaw)
                        else -> null
                    }
                )
                val extension = mapByExtension["laboratoryValues.laboratoryValues.${property.name}"]?.getExtensionByUrl(
                    "http://hl7.org/fhir/StructureDefinition/questionnaire-unit"
                )?.value
            } catch (e: Exception) {
                println(property.name + "   " + valueRaw)
                println(e)
            }
        }
    }
    //TODO: Test this function
    return result
}


fun qItemByGeccoId(response: QR, hashMap: HashMap<String, QRAnswer> = HashMap()): HashMap<String, QRAnswer> {
    response.item.forEach { itemComponent -> itemComponent.answer?.forEach { qItemByGeccoId(it, hashMap) } }
    response.item.forEach { qItemByGeccoId(it, hashMap) }
//    response.extension?.forEach { extension -> hashMap.put((extension.value as CodeType).code, responseAnswer)}
    return hashMap
}


fun qItemByGeccoId(responseItem: QRItem, hashMap: HashMap<String, QRAnswer> = HashMap()): HashMap<String, QRAnswer> {
    responseItem.item.filterNotNull()
        .forEach { itemComponent -> itemComponent.answer?.forEach { qItemByGeccoId(it, hashMap) } }
    responseItem.item.forEach { qItemByGeccoId(it, hashMap) }

    val extension =
        responseItem.getExtensionByUrl("https://num-compass.science/fhir/StructureDefinition/CompassGeccoItem")
    if (extension != null) {
        hashMap[(extension.value as Coding).code] = responseItem.answerFirstRep
    }

    return hashMap
}

fun qItemByGeccoId(
    responseAnswer: QRAnswer,
    hashMap: HashMap<String, QRAnswer> = HashMap()
): HashMap<String, QRAnswer> {
    responseAnswer.item.forEach { itemComponent -> itemComponent?.answer?.forEach { qItemByGeccoId(it, hashMap) } }
//    responseAnswer.extension?.forEach { extension -> hashMap[(extension.value as Coding).code] = responseAnswer }
    val extension =
        responseAnswer.getExtensionByUrl("https://num-compass.science/fhir/StructureDefinition/CompassGeccoItem")
    if (extension != null) {
        hashMap[(extension.value as Coding).code] = responseAnswer
    }
    return hashMap
}