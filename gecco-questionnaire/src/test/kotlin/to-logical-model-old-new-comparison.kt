import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.*
import java.io.FileReader
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

    val logicalModel = toLogicalModel(questionnaire, questionnaireResponse, getRenderersForGecco())
    val logicalModel2 = toLogicalModelOld(questionnaire, questionnaireResponse)

    println(logicalModel)
    println(logicalModel2)

    println(logicalModel.toString() == logicalModel2.toString())

}


fun toLogicalModelOld(questionnaire: Questionnaire, qr: QuestionnaireResponse): LogicalModel {
    copyExtensions(qr, questionnaire)
    val qrAnswerByGeccoId = qrAnswerByGeccoId(qr).filter { it.value?.value != null } as Map<String, QRAnswer>
    val processedMap = processMappedCodes(qrAnswerByGeccoId)

    val logicalModel = LogicalModel()

    for (item in processedMap) {
        setValueForCode(logicalModel, item.key, item.value)
    }

    return logicalModel
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


class ProcessingHolder(var map: MutableMap<String, QRAnswer>, var result: MutableMap<String, Any>) {
    fun addResultAndFilterCodes(resultsToAdd: Map<String, Any>, subCodes: Map<String, Map<String, QRAnswer>>? = null) {
        this.result.putAll(resultsToAdd)
        this.map = this.map.filter { !this.result.containsKey(it.key) }.toMutableMap()

        if (subCodes != null) {
            for (entry in subCodes.entries) {
                for (subCode in entry.value) {
                    this.map.remove(subCode.key)
                }
            }
        }

    }
}


fun processMappedCodes(map: Map<String, QRAnswer>): Map<String, Any> {
    val processingHolder = ProcessingHolder(map.toMutableMap(), HashMap())
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
    processingHolder.addResultAndFilterCodes(
        createValuesFromCodes(valueCodes),
        valueCodes.mapValues { mapOf(it.key to it.value) })

    return processingHolder.result
}

data class SpecialEnumTypeToCodeAndAnswer(val clazz: KClass<*>?, val code: String, val answer: QRAnswer)

fun getSpecialTypesFromLogicalModelWithPath(subProperties: Any, path: String = ""): Map<String, KClass<*>> {
    val map = HashMap<String, KClass<*>>()
    for (property in subProperties.javaClass.kotlin.memberProperties) {
        val propertyValue = property.get(subProperties)

        val newPath = if (path.isNotEmpty()) "$path.${property.name}" else property.name

        if (propertyValue?.javaClass?.kotlin?.memberProperties?.isNotEmpty() == true) {
            map.putAll(getSpecialTypesFromLogicalModelWithPath(propertyValue, newPath))
        } else {
            when (property.returnType.toString()) {
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
                    map[newPath] = property.returnType.classifier as KClass<*>
                }
            }
        }
    }
    return map
}

fun getSpecialTypeToCodeAndAnswer(map: Map<String, QRAnswer>): List<SpecialEnumTypeToCodeAndAnswer> {
    val codesToFind = getSpecialTypesFromLogicalModelWithPath(LogicalModel())

    return map.entries
        .filter { codesToFind.containsKey(it.key) }
        .map { (key, value) -> SpecialEnumTypeToCodeAndAnswer(codesToFind[key], key, value) }
}

fun createSpecialTypeForCodeAndAnswer(list: List<SpecialEnumTypeToCodeAndAnswer>): Map<String, Any> {
    val result = HashMap<String, Any>()
    for (entry in list) {
        try {
            val coding = entry.answer.valueCoding
            result[entry.code] = when (entry.clazz) {
                typeOf<ChronicKidneyDisease>().classifier -> getByCoding2<ChronicKidneyDisease>(coding)!!
                typeOf<Diabetes>().classifier -> getByCoding2<Diabetes>(coding)!!
                typeOf<Countries>().classifier -> getByCoding<Countries>(coding)!!
                typeOf<FederalStates>().classifier -> getByCoding<FederalStates>(coding)!!
                typeOf<CancerStatus>().classifier -> getByCoding<CancerStatus>(coding)!!
                typeOf<Resuscitation>().classifier -> getByCoding<Resuscitation>(coding)!!
                typeOf<SmokingStatus>().classifier -> getByCoding2<SmokingStatus>(coding)!!
                typeOf<BirthSex>().classifier -> getByCoding<BirthSex>(coding)!!
                typeOf<EthnicGroup>().classifier -> getByCoding<EthnicGroup>(coding)!!
                typeOf<FrailityScore>().classifier -> getByCoding2<FrailityScore>(coding)!!
                typeOf<PregnancyStatus>().classifier -> getByCoding2<PregnancyStatus>(coding)!!
                typeOf<RadiologicFindings>().classifier -> getByCoding2<RadiologicFindings>(coding)!!
                typeOf<ACEInhibitorAdministration>().classifier -> getByCoding<ACEInhibitorAdministration>(coding)!!
                typeOf<StageAtDiagnosis>().classifier -> getByCoding2<StageAtDiagnosis>(coding)!!
                typeOf<DetectedNotDetectedInconclusive>().classifier ->
                    getByCoding2<DetectedNotDetectedInconclusive>(coding)!!

                typeOf<TypeOfDischarge>().classifier -> getByCoding2<TypeOfDischarge>(coding)!!
                typeOf<VentilationTypes>().classifier -> getByCoding<VentilationTypes>(coding)!!
                else -> {}
            }
        } catch (e: Exception) {
            println("Could not fetch correct Enum type. QuestionnaireResponse is probably faulty. $e")
        }
    }

    return result
}

fun findCovid19VaccinationCodes(map: Map<String, QRAnswer>): Map<String, Map<String, QRAnswer>> {
    return map.entries.filter {
        it.key.substringAfterLast(".").contains("date") ||
                it.key.substringAfterLast(".").contains("status") ||
                it.key.substringAfterLast(".").contains("vaccine")
    }.groupBy { it.key.substringBeforeLast(".") }
        .mapValues { it.value.toMap() }
        .filter { it.value.size > 2 }
}

private fun <K, V> List<Map.Entry<K, V>>.toMap() = this.associate { it.toPair() }

fun createCovid19VaccinationFromCodes(map: Map<String, Map<String, QRAnswer>>): Map<String, Any> {
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

fun findYesNoUnknownWithDateCodes(map: Map<String, QRAnswer>): Map<String, Map<String, QRAnswer>> {
    return map.entries.filter {
        it.key.substringAfterLast(".").contains("date") ||
                it.key.substringAfterLast(".").contains("status")
    }.groupBy { it.key.substringBeforeLast(".") }
        .filter { it.value.size > 1 }.mapValues { it.value.toMap() }
}

fun createYesNoUnknownWithDateFromCodes(map: Map<String, Map<String, QRAnswer>>): Map<String, Any> {
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


fun findYesNoUnknownWithSymptomSeverityCodes(map: Map<String, QRAnswer>): Map<String, Map<String, QRAnswer>> {
    return map.entries.filter {
        it.key.substringAfterLast(".").contains("presence")
                || it.key.substringAfterLast(".").contains("severity")
    }.groupBy { it.key.substringBeforeLast(".") }
        .filter { it.value.size > 1 }
        .mapValues { it.value.toMap() }
}

fun createYesNoUnknownWithSymptomSeverityFromCodes(map: Map<String, Map<String, QRAnswer>>): HashMap<String, Any> {
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

fun findYesNoUnknownCodes(map: Map<String, QRAnswer>): Map<String, Map<String, QRAnswer>> {
    return map.entries.filter {
        it.value.value is Coding &&
                (it.value.valueCoding.code == YesNoUnknown.YES.coding.code
                        || it.value.valueCoding.code == YesNoUnknown.NO.coding.code
                        || it.value.valueCoding.code == YesNoUnknown.UNKNOWN.coding.code)
    }.groupBy { it.key }
        .mapValues { it.value.toMap() }
}

fun createYesNoUnknownFromCodes(map: Map<String, Map<String, QRAnswer>>): Map<String, Any> {
    val result = mutableMapOf<String, Any>()
    for (code in map) {
        // Should always have one entry, but to be consistent in syntax
        for (subcode in code.value) {
            result[code.key] = toYesNoUnknown(subcode.value.valueCoding)!!
        }
    }
    return result
}

fun findYesNoUnknownOtherNaCodes(map: Map<String, QRAnswer>): Map<String, Map<String, QRAnswer>> {
    return map.entries.filter {
        it.value.value is Coding &&
                (it.value.valueCoding.code == YesNoUnknownOtherNa.YES.coding.code
                        || it.value.valueCoding.code == YesNoUnknownOtherNa.NO.coding.code
                        || it.value.valueCoding.code == YesNoUnknownOtherNa.UNKNOWN.coding.code
                        || it.value.valueCoding.code == YesNoUnknownOtherNa.OTHER.coding.code
                        || it.value.valueCoding.code == YesNoUnknownOtherNa.NA.coding.code)
    }.groupBy { it.key }.mapValues { it.value.toMap() }
}

fun createYesNoUnknownOtherNaFromCodes(map: Map<String, Map<String, QRAnswer>>): Map<String, Any> {
    val result = HashMap<String, YesNoUnknownOtherNa>()
    for (code in map) {
        // Should always have one entry, but to be consistent in syntax
        for (subcode in code.value) {
            result[code.key] = toYesNoUnknownOtherNa(subcode.value.valueCoding)!!
        }
    }
    return result
}

fun findYesNoUnknownWithIntentCodes(map: Map<String, QRAnswer>): Map<String, Map<String, QRAnswer>> {
    return map.entries.filter {
        it.key.substringAfterLast(".").contains("intent") ||
                it.key.substringAfterLast(".").contains("administration")
    }.groupBy { it.key.substringBeforeLast(".") }
        .filter { it.value.size > 1 }
        .mapValues { it.value.toMap() }
}

fun createYesNoUnknownWithIntentFromCodes(map: Map<String, Map<String, QRAnswer>>): Map<String, Any> {
    val result = HashMap<String, Any>()
    for ((geccoId, answers) in map) {
        val coding = YesNoUnknownWithIntent()
        for (subcode in answers) {
            if (subcode.key.contains("intent")) {
                coding.intent = getByCoding2<TherapeuticIntent>(subcode.value.valueCoding)
            } else if (subcode.key.contains("administration")) {
                coding.administration = toYesNoUnknown(subcode.value.valueCoding)
            }
        }
        result[geccoId] = coding
    }
    return result
}

fun findValueCodes(map: Map<String, QRAnswer>): Map<String, QRAnswer> {
    return map.filterValues {
        it.value is IntegerType || it.value is DecimalType || it.value is StringType || it.value is DateType
    }
}

fun createValuesFromCodes(map: Map<String, QRAnswer>): Map<String, Any> {
    val result = HashMap<String, Any>()
    for ((geccoId, answer) in map) {
        val value = when (answer.value::class) {
            StringType::class -> toStringValue(answer.value)
            DateType::class -> toLocalDate(answer.value)
            IntegerType::class -> toInt(answer.value)
            DecimalType::class -> toFloat(answer.value)
            else -> null
        }
        result[geccoId] = value!!
    }
    return result
}


fun extractLab(mapByExtension: HashMap<String, QRAnswer>): LaboratoryValuesLaboratoryValue {
    val result = LaboratoryValuesLaboratoryValue()
    for (property in LaboratoryValuesLaboratoryValue::class.declaredMemberProperties) {
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

/*
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
*/
