import Medication as Medi
import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.*
import java.io.FileReader
import java.time.LocalDate
import java.util.*
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaSetter
import kotlin.reflect.jvm.jvmErasure


fun main() {

    val jsonParser = FhirContext.forR4().newJsonParser().setPrettyPrint(true)
    val questionnaire =
        jsonParser.parseResource(FileReader("./././questionnaire.json")) as Questionnaire
    val qr = generateResponse(questionnaire)

    var lm = toLogicalModel(questionnaire, qr)

    println(lm)

}

fun toLogicalModel(questionnaire: Questionnaire, qr: QuestionnaireResponse): LogicalModel {
    val mapped = mapByExtension(qr)
    addExtensions(qr, questionnaire)

    var lm = LogicalModel()

    //this function extracts all the values, that have to be extracted separately (usually in a hardcoded way)
    //the main function to extract does it one value at a time, but there are specific fields which require two or more values at once, meaning they have to be extracted separately
    lm = extractSpecial(lm, mapped)

    //every element is added one by one if no special treatment is needed
    for (item in qr.allItems) {
        val code = (item.getExtensionByUrl(COMPASS_GECCO_ITEM_EXTENSION).value as Coding).code

        //code exclusions here are based on what has to be hardcoded, they will be set null if removed here
        if (item.answer.isNotEmpty()
            && (!code.lowercase().contains("symptoms") or code.lowercase().contains("asymptomatic"))
            && !code.lowercase().contains("immunizationstatus.influenza")
            && !code.lowercase().contains("immunizationstatus.pneumococcal")
            && !code.lowercase().contains("immunizationstatus.bcg")
        ) {

            val value = item.answer[0]

            //just for bugfixing
            //println("$code - $value - ${value.value}")

            if (value != null) {
                lm = setValue(lm, code, value)
            }

        }

    }
    return lm
}

fun addExtensions(response: QR, questionnaire: Questionnaire) {
    for (qrItem in response.item) {
        val qItem = questionnaire.item.find { it.linkId == qrItem.linkId }
            ?: throw UnknownLinkIdException(qrItem.linkId)
        addExtensions(qrItem, qItem)
    }
}

fun addExtensions(qrItem: QRItem, qItem: QItem) {
    for (answerComponent in qrItem.answer) {
        for (itemComponent in answerComponent.item) {
            val qItem = qItem.item.find { it.linkId == itemComponent.linkId }
                ?: throw UnknownLinkIdException(itemComponent.linkId)
            addExtensions(itemComponent, qItem)
        }
    }
    for (itemComponent in qrItem.item) {
        val qItem = qItem.item.find { it.linkId == itemComponent.linkId }
            ?: throw UnknownLinkIdException(itemComponent.linkId)
        addExtensions(itemComponent, qItem)
    }

    qrItem.extension = qItem.extension.map { it.copy() }

}

class UnknownLinkIdException(val linkId: String): Exception() {
    override val message: String
        get() = "Encountered linkId '${linkId}' in QuestionnaireResponse, which does not exist in Questionnaire! Please make sure, that Questionnaire and QuestionnaireResponse are corresponding to each other!"
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
        return if(value.booleanValue()) YesNoUnknown.YES else YesNoUnknown.NO
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
    return when (answerValue?.toLowerCase()) {
        "ja", "yes" -> YesNoUnknownOtherNa.YES
        "nein", "no" -> YesNoUnknownOtherNa.NO
        "andere", "other" -> YesNoUnknownOtherNa.OTHER
        "na", "not applicable", "nicht anwendbar", "unzutreffend" -> YesNoUnknownOtherNa.NA
        "unbekannt", "unknown" -> YesNoUnknownOtherNa.UNKNOWN
        else -> null
    }
}

fun findProperty(lm: Any, code: String, prop: KProperty1<out Any, *>, value: QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent, instance: Any): Pair<Any, Boolean> {

    //TODO: typeofdischarge, followupswapresult (there were no from(coding) or toEnums)
    //TODO: MedicationAnticoagulation (missing intents in Model)

    var found = false

    var inst = instance

    //Gets property instance to write to
    try {
        inst = instance.javaClass.kotlin.memberProperties.find { it.name == prop.javaField!!.name }!!.get(instance)!!
    } catch (e: Exception) {
        //println("Can't get property instance. Error code: $e")
    }

    //This would have to be implemented if a property directly in logicalmodel exists that needs to be provided with a value
    /*if (prop.toString().lowercase().substringBefore(":") == code.lowercase()) {
        //set value
        println("Found $code")
    }*/

    //going through subproperties of the current property
    if (prop.javaGetter!!.returnType.kotlin.memberProperties.isNotEmpty()) {
        for (subprop in prop.javaGetter!!.returnType.kotlin.memberProperties) {

            if ((prop.toString().lowercase().substringAfter("var ").substringBefore(".") in subprop.toString().lowercase().substringAfter("var ").substringBefore(":")) or ("logicalmodel" in prop.toString().lowercase().substringAfter("var ").substringBefore(":").substringBeforeLast("."))) {

                //covid 19 immunization has to be hardcoded, because subproperties of anamnesis containing immunization info are missing anamnesis markers
                if("anamnesis.immunizationstatus.covid19" in code.lowercase()) {
                    if (subprop.toString().lowercase().substringAfter("var ").substringBefore(":").replace(".", "") == code.lowercase().substringBeforeLast(".").replace(".", "")) {
                        for (covProp in subprop.javaGetter!!.returnType.kotlin.memberProperties) {
                            if (covProp.toString().lowercase().substringAfter(".").substringBefore(":")
                                in code.lowercase().replace(".", "").substringAfter("covid19_")
                            ) {
                                if (covProp is KMutableProperty<*>) {
                                    try {
                                        //getting correct property instance, because we are deeper in the LM structure
                                        inst = inst.javaClass.kotlin.memberProperties.find { it.name == subprop.javaField!!.name }!!.get(inst)!!
                                        val covValue = value.value

                                        covProp.javaSetter!!.invoke(
                                            inst, when (covProp.name) {
                                                in "status" -> toYesNoUnknown(covValue)
                                                in "date" -> value.toLocalDate()
                                                in "vaccine" -> value.toEnum1<Covid19Vaccine>()
                                                else -> null
                                            }
                                        )
                                        found = true
                                        return Pair(inst, found)
                                    } catch (e:Exception) {
                                        println("Could not set covid19 vaccination ${code.substringAfter("status")} property. Error code $e")
                                    }
                                }
                            }
                        }
                    }
                }

                //checks if the code name and name of the current property are the same
                if ((subprop.toString().lowercase().substringAfter("var ").substringBefore(":").replace(".", "") == code.lowercase().replace(".", ""))
                    or (subprop.toString().lowercase().substringAfter("var ").substringBefore(":").replace(".", "") == code.lowercase().substringAfter(".").replace(".", ""))
                    //this is necessary, because laboratoryvalues are mostly stored in an inner class called "laboratoryvalueslaboratoryvalue" but the code is "laboratoryvalues.laboratoryvalues"
                    or (subprop.toString().lowercase().substringAfter("var ").substringBefore(":").replace(".", "") == "laboratoryvalueslaboratoryvalue" + code.substringAfterLast(".").lowercase())
                    or (subprop.toString().lowercase().substringAfter("var ").substringBefore(":").replace(".", "") == "medicationanticoagulation" + code.substringAfterLast(".").lowercase())
                ) {
                    if (subprop is KMutableProperty<*>) {
                        //based on what type the property is, the value will be inserted into the instance with the respective function
                        if (subprop.returnType.toString() == "YesNoUnknown?") {
                            try {
                                subprop.javaSetter!!.invoke(inst, toYesNoUnknown(value.value))
                                //println("$code invoked at $inst")
                                found = true
                                return Pair(inst, found)
                            } catch (e: Exception) {
                                println("Can't set ${subprop.name} to ${value.value}. Error Code: $e")
                            }
                        }
                        if (subprop.returnType.toString() == "YesNoUnknownOtherNa?") {
                            try {
                                subprop.javaSetter!!.invoke(inst, toYesNoUnknownOtherNa(value.value))
                                //println("$code invoked at $inst")
                                found = true
                                return Pair(inst, found)
                                //break
                            } catch (e: Exception) {
                                println("Can't set ${subprop.name} to $value. Error Code: $e")
                            }
                        }
                        else {
                            try {
                                val valueGot = value.value
                                if (valueGot != null) {
                                    subprop.javaSetter!!.invoke(
                                        inst, when (subprop.name) {
                                            //hardcoded enum transformations, this must be hardcoded
                                            in "ethnicGroup" -> value.toEnum1<EthnicGroup>()
                                            in "chronicKidneyDisease" -> ChronicKidneyDisease.from(CodeableConcept(value.valueCoding))
                                            in "diabetesMellitusType1" -> value.toEnum2<Diabetes>()
                                            in "tobaccoSmokingStatus" -> value.toEnum2<SmokingStatus>()
                                            in "malignantNeoplasticDiseases" -> value.toEnum1<CancerStatus>()
                                            in "country" -> value.toEnum1<Countries>()
                                            in "federalState" -> value.toEnum1<FederalStates>()
                                            in "resuscitateOrder" -> value.toEnum1<Resuscitation>()
                                            in "biologicalSex" -> value.toEnum1<BirthSex>()
                                            in "pregnancyStatus" -> value.toEnum2<PregnancyStatus>()
                                            in "frailityScore" -> value.toEnum2<FrailityScore>()
                                            in "stageAtDiagnosis" -> value.toEnum2<StageAtDiagnosis>()
                                            in "aceInhibitors" -> value.toEnum1<ACEInhibitorAdministration>()
                                            in "ventilationType" -> value.toEnum1<VentilationTypes>()
                                            in "radiologicalFindings" -> RadiologicFindings.from(CodeableConcept(value.valueCoding))
                                            in "typeOfDischarge" -> TypeOfDischarge.ALIVE //to TypeOfDischarge
                                            in "followupSwapResultIsPositive" -> null //to DetectedNotDetectedInconclusive
                                            else -> when (valueGot) {
                                                //variable type based transformations
                                                is DecimalType -> valueGot.value.toFloat()
                                                is IntegerType -> valueGot.value.toInt()
                                                is StringType -> valueGot.value
                                                is DateType -> value.toLocalDate()
                                                is Coding -> toYesNoUnknown(valueGot)
                                                else -> null
                                            }
                                        }
                                    )
                                    found = true
                                    return Pair(inst, found)
                                }
                            } catch (e: Exception) {
                                println("Can't set ${subprop.name} to $value. Error Code: $e")
                            }
                        }
                    }
                }
                if (subprop.javaGetter!!.returnType.kotlin.memberProperties.isNotEmpty()) {
                    findProperty(lm, code, subprop, value, inst)
                }
            }
        }
    }

    return Pair(inst, found)
}

fun setValue(lm: LogicalModel, code: String, value: QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent): LogicalModel {

    for (property in lm::class.memberProperties) {

        var inst = lm

        //property is being searched, an instance of the property the value is inserted into and a found boolean are returned
        var (valueInst, found) = findProperty(lm, code, property, value, inst)

        if (property is KMutableProperty<*> && found) {
            try {
                //inserts the final instance with the inserted value into the logicalmodel
                property.javaSetter!!.invoke(lm, valueInst)
                return lm
            } catch (e: Exception) {
                println("Couldn't set value in lm. Error Code: $e")
            }

        }
    }

    return lm
}

fun extractSpecial(lm: LogicalModel, mapped: HashMap<String, QRAnswer>): LogicalModel {

    //property of symptoms has to be found and extracted in different manner, because more than one value has to be added to each element at a time
    for (prop in lm::class.memberProperties) {
        if (prop.name == "symptoms") {
            if (prop is KMutableProperty<*>) {
                try {
                    var extSymptoms = extractSymptoms(mapped)
                    prop.javaSetter!!.invoke(lm, extSymptoms)
                    println("Set symptoms")
                } catch (e:Exception) {
                    println("Couldn't extract symptoms to lm. Error Code: $e")
                }
            }
        }
        //immunization has to be done seperately, because two values are needed for some properties
        if (prop.name == "anamnesis") {
            var immu = AnamnesisImmunizationStatus()
            for (immuProp in immu.javaClass.kotlin.memberProperties) {
                if (immuProp is KMutableProperty<*>) {
                    if ("influenza" in immuProp.name) {
                        try {
                            immuProp.javaSetter!!.invoke(immu, YesNoUnknownWithDate(
                                toYesNoUnknown(mapped["anamnesis.immunizationStatus.influenza"]?.value),
                                (mapped["anamnesis.immunizationStatus.influenza.Datum"] as? BaseDateTimeType)?.toCalendar()?.time
                            ))
                        } catch (e:Exception) {
                            println("Couldn't extract influenza immunization status. Error code: $e")
                        }
                    }
                    if ("pneumococcal" in immuProp.name) {
                        try {
                            immuProp.javaSetter!!.invoke(immu, YesNoUnknownWithDate(
                                toYesNoUnknown(mapped["anamnesis.immunizationStatus.pneumococcal"]?.value),
                                (mapped["anamnesis.immunizationStatus.pneumococcal.Datum"]?.value as? BaseDateTimeType)?.toCalendar()?.time
                            ))
                        } catch (e:Exception) {
                            println("Couldn't extract pneumococcal immunization status. Error code: $e")
                        }
                    }
                    if ("bcg" in immuProp.name) {
                        try {
                            immuProp.javaSetter!!.invoke(immu, YesNoUnknownWithDate(
                                toYesNoUnknown(mapped["anamnesis.immunizationStatus.bcg"]?.value),
                                (mapped["anamnesis.immunizationStatus.bcg.Datum"]?.value as? BaseDateTimeType)?.toCalendar()?.time
                            ))
                        } catch (e:Exception) {
                            println("Couldn't extract bcg immunization status. Error code: $e")
                        }
                    }
                }
            }

            var anam = Anamnesis()
            for (anamProp in anam.javaClass.kotlin.memberProperties) {
                if (anamProp is KMutableProperty<*>) {
                    if ("immunizationStatus" in anamProp.name) {
                        try {
                            anamProp.javaSetter!!.invoke(anam, immu)
                        } catch (e:Exception) {
                            println("Couldn't insert immunizationStatus object into anamnesis object. Error code: $e")
                        }
                    }
                }
            }
            if (prop is KMutableProperty<*>) {
                try {
                    prop.javaSetter!!.invoke(lm, anam)
                } catch (e:Exception) {
                    println("Couldn't insert anamnesis object with immunization status for influenza, pneumococcal and bcg into lm. Error code: $e")
                }
            }
        }
    }
    return lm
}

fun extractSymptoms(mapByExtension: HashMap<String, QRAnswer>): Symptoms {
    val result = Symptoms()
    for (property in result::class.declaredMemberProperties) {
        if (property is KMutableProperty<*>) {
            val presence = toYesNoUnknown(mapByExtension["symptoms.${property.name}.presence"]?.value)
            val severity =
                if (presence == YesNoUnknown.YES && "asymptomatic" !in property.name) mapByExtension["symptoms.${property.name}.severity"]?.toEnum1<SymptomSeverity>() else null

            //var value = if (property.returnType.jvmErasure == YesNoUnknownWithSymptomSeverity::class) {
            val value = if (property.returnType.toString() == "YesNoUnknownWithSymptomSeverity?") {
                YesNoUnknownWithSymptomSeverity(presence, severity)
            } else {
                presence
            }

            try {
                property.javaSetter!!.invoke(result, value)
            } catch (e: Exception) {
                println("${property.name}    presence = $presence severity = $severity value = $value")
                println(e)
            }
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
                property.javaSetter!!.invoke(result, when(valueRaw) {
                    is DecimalType -> valueRaw.value.toFloat()
                    is StringType -> valueRaw.value
                    is Coding -> toYesNoUnknown(valueRaw)
                    else -> null
                })
                val extension = mapByExtension["laboratoryValues.laboratoryValues.${property.name}"]?.getExtensionByUrl(
                    "http://hl7.org/fhir/StructureDefinition/questionnaire-unit")?.value
            } catch (e: Exception) {
                println(property.name + "   " + valueRaw)
                println(e)
            }
        }
    }
    //TODO: Test this function
    return result
}


inline fun <reified T> QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent.toEnum1(): T? where T : CodeableEnum<T>, T : Enum<T> {
    return (this.value as? Coding)?.let { getByCoding<T>(it) }
}

inline fun <reified T> QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent.toEnum2(): T? where T : ConceptEnum<T>, T : Enum<T> {
    return (this.value as? Coding)?.let { getByCoding2<T>(it) }
}

fun QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent.toDecimal(): Float? {
    return (this.value as? DecimalType)?.value?.toFloat()
}

fun QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent.toInt(): Int? {
    return (this.value as? IntegerType)?.value
}

fun QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent.toLocalDate(): LocalDate? {
    val dateType = this.value as? DateType
    return dateType?.let { LocalDate.of(it.year, it.month, it.day) }
}


fun mapByExtension(response: QR, hashMap: HashMap<String, QRAnswer> = HashMap()): HashMap<String, QRAnswer> {
    response.item.forEach { itemComponent -> itemComponent.answer?.forEach { mapByExtension(it, hashMap) } }
    response.item.forEach { mapByExtension(it, hashMap) }
//    response.extension?.forEach { extension -> hashMap.put((extension.value as CodeType).code, responseAnswer)}
    return hashMap;
}


fun mapByExtension(responseItem: QRItem, hashMap: HashMap<String, QRAnswer> = HashMap()): HashMap<String, QRAnswer> {
    responseItem.item.filterNotNull().forEach { itemComponent -> itemComponent.answer?.forEach { mapByExtension(it, hashMap) } }
    responseItem.item.forEach { mapByExtension(it, hashMap) }

    val extension = responseItem.getExtensionByUrl("https://num-compass.science/fhir/StructureDefinition/CompassGeccoItem")
    if (extension != null) {
        hashMap[(extension.value as Coding).code] = responseItem.answerFirstRep
    }

    return hashMap;
}

fun mapByExtension(
    responseAnswer: QRAnswer,
    hashMap: HashMap<String, QRAnswer> = HashMap()
): HashMap<String, QRAnswer> {
    responseAnswer.item.forEach { itemComponent -> itemComponent?.answer?.forEach { mapByExtension(it, hashMap) } }
//    responseAnswer.extension?.forEach { extension -> hashMap[(extension.value as Coding).code] = responseAnswer }
    val extension = responseAnswer.getExtensionByUrl("https://num-compass.science/fhir/StructureDefinition/CompassGeccoItem")
    if (extension != null) {
        hashMap[(extension.value as Coding).code] = responseAnswer
    }
    return hashMap;
}