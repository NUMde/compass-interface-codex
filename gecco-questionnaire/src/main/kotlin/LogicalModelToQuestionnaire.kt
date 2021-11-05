import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.*
import java.io.File
import java.time.LocalDate
import java.util.*
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure
import org.hl7.fhir.r4.model.Questionnaire.QuestionnaireItemComponent as QItem

data class Dummy(val logicalModel: LogicalModel)

fun main() {
    val ctx = FhirContext.forR4()
    val parser = ctx.newJsonParser().setPrettyPrint(true)
    val questionnaireJson = parser.encodeResourceToString(toQuestionnaire())
//    println(questionnaireJson)
    val outfile = File("questionnaire.json")
    outfile.createNewFile()
    outfile.writeText(questionnaireJson)
}


fun toQuestionnaire(): Questionnaire {
    return replaceGeccoIDWithLinkID(
        Questionnaire().apply {
            status = Enumerations.PublicationStatus.ACTIVE
            url = "https://num-compass.science/fhir/Questionnaires/GECCO"
            version = "1.0"
            item = listOf(propertyToItem(Dummy::logicalModel))
            item = item[0].item
        })
}



fun propertyToItem(
    property: KProperty<*>,
    newLinkId: String = "",
    compassId: String = "",
    fhirProfileUrl2: String = ""
): QItem {
    val returnType = property.returnType.jvmErasure
    val item = QItem().apply {
        linkId = newLinkId.removePrefix(".")
        text = property.findAnnotation<Text>()?.text ?: property.name
        addExtension(compassGeccoItem(compassId))
    }

    val fhirProfileUrl = property.findAnnotation<FhirProfile>()?.url ?: fhirProfileUrl2

    property.findAnnotation<EnableWhenYes>()?.let {
        item.enableWhen = listOf(Questionnaire.QuestionnaireItemEnableWhenComponent().apply {
            question = it.geccoId // in a later step, run thru entire questionnaire and replace with linkId
            operator = Questionnaire.QuestionnaireItemOperator.EQUAL
            answer = YesNoUnknown.YES.coding
        })
    }


    property.findAnnotation<EnableWhen>()?.let {
        item.enableWhen = listOf(Questionnaire.QuestionnaireItemEnableWhenComponent().apply {
            question = it.geccoId //in a later step, run thru entire questionnaire and replace with linkId
            operator = Questionnaire.QuestionnaireItemOperator.EQUAL
            answer = Coding(it.system, it.code, null)
        })
    }

    property.findAnnotation<ComboBox>()?.let {
        item.addExtension(
            "http://hl7.org/fhir/StructureDefinition/questionnaire-itemControl",
            CodeableConcept(Coding("http://hl7.org/fhir/questionnaire-item-control", "drop-down", "Drop down"))
        )
    }


    if (returnType.isData) {
        item.apply {
            type = Questionnaire.QuestionnaireItemType.GROUP
            val constructorPropertiesMap = returnType.memberProperties.associateBy { it.name }

            var i = 0
            val result = mutableListOf<QItem>()
            for (param in returnType.constructors.first().parameters) {
                val prop = constructorPropertiesMap[param.name]!!
                //Find the annotation on the property and use the FhirProfile as parameter for the call
                var url = prop.findAnnotation<FhirProfile>()?.url ?: ""

                //if no annotation is found on the property the search the annotation on the class instead
                if (url == "") {
                    url = returnType.findAnnotation<FhirProfile>()?.url ?: ""
                }
                if (prop.findAnnotation<Ignore>() == null) {
                    result += propertyToItem(prop, "$newLinkId.${i + 1}", "$compassId.${prop.name}", url)
                    i++
                }
            }
            this.item = result

        }
    } else if (returnType == YesNoUnknown::class) {
        item.apply {
            //Add the parameter FhirProfile as definitionElement
            if (fhirProfileUrl != "") {
                addExtension(compassGeccoTargetProfile(fhirProfileUrl))
            }
            type = Questionnaire.QuestionnaireItemType.CHOICE
            extractEnum(YesNoUnknown::class.java)
        }
    } else if (returnType == YesNoUnknownWithSymptomSeverity::class) {
        item.apply {
            if (fhirProfileUrl != "") {
                addExtension(compassGeccoTargetProfile(fhirProfileUrl))
            }
            type = Questionnaire.QuestionnaireItemType.GROUP
            val presence = QItem().apply {
                linkId = item.linkId + ".1"
                text = "Vorhandensein?"
                type = Questionnaire.QuestionnaireItemType.CHOICE
                extractEnum(YesNoUnknown::class.java)
                addExtension(compassGeccoItem("$compassId.presence"))
            }
            this.addItem(presence)

            val severity = QItem().apply {
                linkId = item.linkId + ".2"
                text = "Schweregrad?"
                type = Questionnaire.QuestionnaireItemType.CHOICE
                answerOption = listOf(
                    snomed("255604002", "mild"),
                    snomed("6736007", "moderat"),
                    snomed("24484000", "schwer"),
                    snomed("442452003", "lebensbedrohlich")
                ).map { Questionnaire.QuestionnaireItemAnswerOptionComponent(it) }
                enableWhen = listOf(Questionnaire.QuestionnaireItemEnableWhenComponent().apply {
                    question =
                        "${compassId.removePrefix(".")}.presence" // TODO: in a later step, run thru entire questionnaire and replace with linkId
                    operator = Questionnaire.QuestionnaireItemOperator.EQUAL
                    answer = YesNoUnknown.YES.coding
                })
                addExtension(compassGeccoItem("$compassId.severity"))
            }
            item.addItem(severity)
        }
    } else if (returnType == YesNoUnknownWithIntent::class) {
        item.apply {
            if (fhirProfileUrl != "") {
                addExtension(compassGeccoTargetProfile(fhirProfileUrl))
            }
            type = Questionnaire.QuestionnaireItemType.GROUP
            val presence = QItem().apply {
                linkId = item.linkId + ".1"
                text = "Wurde das Medikament verabreicht?"
                type = Questionnaire.QuestionnaireItemType.CHOICE
                extractEnum(YesNoUnknown::class.java)
                addExtension(compassGeccoItem("$compassId.given"))
            }
            this.addItem(presence)

            val severity = QItem().apply {
                linkId = item.linkId + ".2"
                text = "Mit welcher therapheutischen Absicht das Medikament verarbreicht?"
                type = Questionnaire.QuestionnaireItemType.CHOICE
                extractEnum(TherapeuticIntent::class.java)
                enableWhen = listOf(Questionnaire.QuestionnaireItemEnableWhenComponent().apply {
                    question = "${compassId.removePrefix(".")}.given"
                    operator = Questionnaire.QuestionnaireItemOperator.EQUAL
                    answer = YesNoUnknown.YES.coding
                })
                addExtension(compassGeccoItem("$compassId.intent"))
            }
            item.addItem(severity)
        }
    } else if (returnType == YesNoUnknownWithDate::class) {
        item.apply {
            if (fhirProfileUrl != "") {
                addExtension(compassGeccoTargetProfile(fhirProfileUrl))
            }

            type = Questionnaire.QuestionnaireItemType.GROUP
            val answerItem = QItem().apply {
                //TODO: Reuse
                linkId = item.linkId + ".1"
                extractEnum(YesNoUnknown::class.java)
                addExtension(compassGeccoItem("$compassId.status"))
            }

            val dateItem = QItem().apply {
                linkId = item.linkId + ".2"
                text = "Datum"
                type = Questionnaire.QuestionnaireItemType.DATE
                addExtension(compassGeccoItem("$compassId.date"))
                enableWhen = listOf(Questionnaire.QuestionnaireItemEnableWhenComponent().apply {
                    question = "${compassId.removePrefix(".")}.status"
                    operator = Questionnaire.QuestionnaireItemOperator.EQUAL
                    answer = YesNoUnknown.YES.coding
                })
            }
            this.item = listOf(answerItem, dateItem)

        }
    } else if (returnType.isSubclassOf(Enum::class)) {
        item.apply {
            //Find the annotation on the class and add the FhirProfile as definitionElement
            if (fhirProfileUrl != "") {
                addExtension(compassGeccoTargetProfile(fhirProfileUrl))
            }

            extractEnum(property.returnType.jvmErasure.java)
        }
    } else if (returnType == Float::class) {
        item.apply {
            type = Questionnaire.QuestionnaireItemType.DECIMAL
            code = extractCodingFromLabAnnotation(property)?.let { listOf(it) }
            code.forEach { addExtension(questionnaireUnit(it)) }
        }
    } else if (returnType == Int::class) {
        item.apply {
            type = Questionnaire.QuestionnaireItemType.INTEGER
            code = extractCodingFromLabAnnotation(property)?.let { listOf(it) }
            code.forEach { addExtension(questionnaireUnit(it)) }
        }
    } else if (returnType == Date::class || returnType == LocalDate::class) {
        item.apply {
            type = Questionnaire.QuestionnaireItemType.DATE
        }
    } else if (returnType == String::class) {
        item.apply {
            type = Questionnaire.QuestionnaireItemType.STRING
        }
    }
    return item
}

fun extractCodingFromLabAnnotation(property: KProperty<*>): Coding? {
    for (annotation in property.annotations) {
        if (annotation.annotationClass.simpleName!!.startsWith("Lab")) {
            return when (annotation) {
                is LabCRPEnum -> annotation.enum.coding
                is LabFerritin -> annotation.enum.coding
                is LabBilirubin -> annotation.enum.coding
                is LabLactateDehydrogenase -> annotation.enum.coding
                is LabCreatineMassPerVolume -> annotation.enum.coding
                is LabCreatineMolesPerVolume -> annotation.enum.coding
                is LabLactateMassPerVolume -> annotation.enum.coding
                is LabLactateMolesPerVolume -> annotation.enum.coding
                is LabLeukocytes -> annotation.enum.coding
                is LabLymphocytes -> annotation.enum.coding
                is LabPPT -> annotation.enum.coding
                is LabAlbuminMassPerVolume -> annotation.enum.coding
                is LabAlbuminMolesPerVolume -> annotation.enum.coding
                is LabAntithrombin -> annotation.enum.coding
                is LabFibrinogen -> annotation.enum.coding
                is LabHemoglobinMassPerVolume -> annotation.enum.coding
                is LabHemoglobinMolesPerVolume -> annotation.enum.coding
                is LabInterleukin6 -> annotation.enum.coding
                is LabNatriureticPeptideB -> annotation.enum.coding
                is LabNeutrophils -> annotation.enum.coding
                is LabPlateletsCountPerVolume -> annotation.enum.coding
                is LabProcalcitonin -> annotation.enum.coding
                is LabTroponinICardiacMassPerVolume -> annotation.enum.coding
                is LabTroponinTCardiacMassPerVolume -> annotation.enum.coding
                is LabINR -> annotation.enum.coding
                else -> throw Exception("Unknown Lab Enum: " + annotation)
            }
        }
    }
    return null
}

fun extractUnitFromLabAnnotation(property: KProperty<*>): GeccoUnits? {
    for (annotation in property.annotations) {
        if (annotation.annotationClass.simpleName!!.startsWith("Lab")) {
            return when (annotation) {
                is LabCRPEnum -> annotation.enum.unit
                is LabFerritin -> annotation.enum.unit
                is LabBilirubin -> annotation.enum.unit
                is LabLactateDehydrogenase -> annotation.enum.unit
                is LabCreatineMassPerVolume -> annotation.enum.unit
                is LabCreatineMolesPerVolume -> annotation.enum.unit
                is LabLactateMassPerVolume -> annotation.enum.unit
                is LabLactateMolesPerVolume -> annotation.enum.unit
                is LabLeukocytes -> annotation.enum.unit
                is LabLymphocytes -> annotation.enum.unit
                is LabPPT -> annotation.enum.unit
                is LabAlbuminMassPerVolume -> annotation.enum.unit
                is LabAlbuminMolesPerVolume -> annotation.enum.unit
                is LabAntithrombin -> annotation.enum.unit
                is LabFibrinogen -> annotation.enum.unit
                is LabHemoglobinMassPerVolume -> annotation.enum.unit
                is LabHemoglobinMolesPerVolume -> annotation.enum.unit
                is LabInterleukin6 -> annotation.enum.unit
                is LabNatriureticPeptideB -> annotation.enum.unit
                is LabNeutrophils -> annotation.enum.unit
                is LabPlateletsCountPerVolume -> annotation.enum.unit
                is LabProcalcitonin -> annotation.enum.unit
                is LabTroponinICardiacMassPerVolume -> annotation.enum.unit
                is LabTroponinTCardiacMassPerVolume -> annotation.enum.unit
                is LabINR -> annotation.enum.unit
                else -> throw Exception("Unknown Lab Enum: " + annotation)
            }
        }
    }
    return null
}

private fun Questionnaire.QuestionnaireItemComponent.extractEnum(clazz: Class<out Any>) {
    val enums = clazz.enumConstants
    type = Questionnaire.QuestionnaireItemType.CHOICE
    answerOption = enums.map {
        val coding = it::class.declaredMemberProperties.find { it.name == "coding" }?.getter
        val codeableConcept = it::class.declaredMemberProperties.find { it.name == "codeableConcept" }
        val displayTextDe = it::class.declaredMemberProperties.find { it.name == "displayDe" }
        val result = when {
            coding != null -> coding.call(it) as Coding
            codeableConcept != null ->
                (codeableConcept.call(it) as CodeableConcept).coding.first() //TODO: What if there are multiple codings?
            else -> Coding(null, null, it.toString())
        }
        if (displayTextDe != null) {
            val displayTextDeValue = displayTextDe.call(it) as String?
            if (displayTextDeValue != null) {
                result.display = displayTextDeValue
            }
        }
        result
    }.map { Questionnaire.QuestionnaireItemAnswerOptionComponent(it) }
}

fun replaceGeccoIDWithLinkID(questionnnaire: Questionnaire): Questionnaire {
    val mapByExtension = mapByExtension(questionnnaire)
    replaceGeccoIDWithLinkID(questionnnaire.item, mapByExtension)
    return questionnnaire;
}


fun replaceGeccoIDWithLinkID(qItems: List<QItem>, mapByExtension: HashMap<String, QItem>) {
    for (qItem in qItems) {
        if (qItem.item.isNotEmpty()) {
            replaceGeccoIDWithLinkID(qItem.item, mapByExtension)
        }
        qItem.enableWhen?.forEach { enableWhenComponent ->
            val enabledWhenItem = mapByExtension[enableWhenComponent.question]
            if (enabledWhenItem != null) {
                enableWhenComponent.question = enabledWhenItem.linkId
            } else {
                println("Cannot create enableWhen-Expression for " + qItem.linkId + ": " + enableWhenComponent.question)
            }
        }
    }
}

fun compassGeccoItem(compassId: String) = Extension(
    COMPASS_GECCO_ITEM_EXTENSION,
    Coding(COMPASS_GECCO_ITEM_CS, compassId.removePrefix("."), null).setVersion("1.0")
)

fun compassGeccoTargetProfile(profile: String) = Extension(
    "https://num-compass.science/fhir/StructureDefinition/GeccoTargetProfile",
    StringType(profile)
)

fun questionnaireUnit(coding: Coding) = Extension(
    "http://hl7.org/fhir/StructureDefinition/questionnaire-unit",
    coding
)


fun mapByExtension(questionnaire: Questionnaire, hashMap: HashMap<String, QItem> = HashMap()): HashMap<String, QItem> {
    questionnaire.item.forEach { mapByExtension(it, hashMap) }
    return hashMap
}

fun mapByExtension(item: QItem, hashMap: HashMap<String, QItem> = HashMap()): HashMap<String, QItem> {
    item.item.forEach { mapByExtension(it, hashMap) }
    val extension = item.getExtensionByUrl("https://num-compass.science/fhir/StructureDefinition/CompassGeccoItem")
    if (extension != null && extension.hasValue()) {
        hashMap[(extension.value as Coding).code] = item
    }
    return hashMap;
}
