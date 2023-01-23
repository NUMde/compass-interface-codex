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


/**
 * Convert the logical model into a FHIR Questionnaire
 */
fun main() {
    val ctx = FhirContext.forR4()
    val parser = ctx.newJsonParser().setPrettyPrint(true)
    val questionnaireJson = parser.encodeResourceToString(LogicalModel.toQuestionnaire())
//    println(questionnaireJson)
    val outfile = File("questionnaire.json")
    outfile.createNewFile()
    outfile.writeText(questionnaireJson)
}


fun LogicalModel.Companion.toQuestionnaire(): Questionnaire {
    return replaceGeccoIDWithLinkID(
        Questionnaire().apply {
            status = Enumerations.PublicationStatus.ACTIVE
            url = "https://num-compass.science/fhir/Questionnaires/GECCO"
            version = "1.0"
            item = propertyToItem(Dummy::logicalModel).item
        })
}

data class Dummy(val logicalModel: LogicalModel)

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
        addExtension(CompassGeccoItemExtension(compassId))
    }

    val fhirProfileUrl = property.findAnnotation<FhirProfile>()?.url ?: fhirProfileUrl2
    if (fhirProfileUrl != "") {
        item.addExtension(GeccoTargetProfileExtension(fhirProfileUrl))
    }

    property.findAnnotation<EnableWhenYes>()?.let {
        item.enableWhen = listOf(Questionnaire.QuestionnaireItemEnableWhenComponent().apply {
            question = it.geccoId // in a later step, run through entire questionnaire and replace with linkId
            operator = Questionnaire.QuestionnaireItemOperator.EQUAL
            answer = YesNoUnknown.YES.coding
        })
    }
    property.findAnnotation<EnableWhen>()?.let {
        item.enableWhen = listOf(Questionnaire.QuestionnaireItemEnableWhenComponent().apply {
            question = it.geccoId //in a later step, run through entire questionnaire and replace with linkId
            operator = Questionnaire.QuestionnaireItemOperator.EQUAL
            answer = Coding(it.system, it.code, null)
        })
    }

    if (property.findAnnotation<ComboBox>() != null) {
        item.addExtension(DropDownExtension())
    }


    if (returnType.isData) {
        item.apply {
            type = Questionnaire.QuestionnaireItemType.GROUP
            item.removeExtension("https://num-compass.science/fhir/StructureDefinition/GeccoTargetProfile")
            val constructorPropertiesMap = returnType.memberProperties.associateBy { it.name }

            var i = 0
            this.item = buildList {
                for (param in returnType.constructors.first().parameters) {
                    val prop = constructorPropertiesMap[param.name]!!
                    //Find the annotation on the property and use the FhirProfile as parameter for the call
                    //if no annotation is found on the property the search the annotation on the class instead
                    val url = (prop.findAnnotation<FhirProfile>() ?: returnType.findAnnotation())?.url ?: ""

                    if (prop.findAnnotation<Ignore>() == null) {
                        add(propertyToItem(prop, "$newLinkId.${i + 1}", "$compassId.${prop.name}", url))
                        i++
                    }
                }
            }

        }
    } else if (returnType == YesNoUnknown::class) {
        YesNoUnknownRenderer().render(compassId, item)
    } else if (returnType == YesNoUnknownWithSymptomSeverity::class) {
        YesNoUnknownWithSeverityRenderer().render(compassId, item)
    } else if (returnType == YesNoUnknownWithIntent::class) {
        YesNoUnknownWithIntentRenderer().render(compassId, item)
    } else if (returnType == YesNoUnknownWithDate::class) {
        YesNoUnknownWithDateRenderer().render(compassId, item)
    } else if (returnType.isSubclassOf(Enum::class)) {
        item.apply {
            extractEnum(property.returnType.jvmErasure.java)
        }
    } else if (returnType == Float::class) {
        item.apply {
            type = Questionnaire.QuestionnaireItemType.DECIMAL
            code = extractCodingFromLabAnnotation(property)?.let { listOf(it) }
            code.forEach { addExtension(QuestionnaireUnitExtension(it)) }
        }
    } else if (returnType == Int::class) {
        item.apply {
            type = Questionnaire.QuestionnaireItemType.INTEGER
            code = extractCodingFromLabAnnotation(property)?.let { listOf(it) }
            code.forEach { addExtension(QuestionnaireUnitExtension(it)) }
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
    return extractLabAnnotation(property)?.getInstanceThroughReflection("coding")
}

fun extractUnitFromLabAnnotation(property: KProperty<*>): GeccoUnits? {
    val annotation = extractLabAnnotation(property)
    return annotation?.getInstanceThroughReflection("unit")
}

fun extractLabAnnotation(property: KProperty<*>): Enum<*>? {
    for (annotation in property.annotations) {
        if (annotation.annotationClass.simpleName!!.startsWith("Lab")) {
            return when (annotation) {
                is LabCRPEnum -> annotation.enum
                is LabFerritin -> annotation.enum
                is LabBilirubin -> annotation.enum
                is LabLactateDehydrogenase -> annotation.enum
                is LabCreatineMassPerVolume -> annotation.enum
                is LabCreatineMolesPerVolume -> annotation.enum
                is LabLactateMassPerVolume -> annotation.enum
                is LabLactateMolesPerVolume -> annotation.enum
                is LabLeukocytes -> annotation.enum
                is LabLymphocytes -> annotation.enum
                is LabPPT -> annotation.enum
                is LabAlbuminMassPerVolume -> annotation.enum
                is LabAlbuminMolesPerVolume -> annotation.enum
                is LabAntithrombin -> annotation.enum
                is LabFibrinogen -> annotation.enum
                is LabHemoglobinMassPerVolume -> annotation.enum
                is LabHemoglobinMolesPerVolume -> annotation.enum
                is LabInterleukin6 -> annotation.enum
                is LabNatriureticPeptideB -> annotation.enum
                is LabNeutrophils -> annotation.enum
                is LabPlateletsCountPerVolume -> annotation.enum
                is LabProcalcitonin -> annotation.enum
                is LabTroponinICardiacMassPerVolume -> annotation.enum
                is LabTroponinTCardiacMassPerVolume -> annotation.enum
                is LabINR -> annotation.enum
                else -> error("Unknown Lab Enum: $annotation")
            }
        }
    }
    return null
}


fun Questionnaire.QuestionnaireItemComponent.extractEnum(clazz: Class<out Any>) {
    val enums = clazz.enumConstants
    type = Questionnaire.QuestionnaireItemType.CHOICE
    answerOption = enums.map {
        val coding = it::class.declaredMemberProperties.find { it.name == "coding" }?.getter
        val codeableConcept = it::class.declaredMemberProperties.find { it.name == "codeableConcept" }
        val result = when {
            coding != null -> coding.call(it) as Coding
            // If there are multiple codings, one must ensure that the codings are unique
            codeableConcept != null -> (codeableConcept.call(it) as CodeableConcept).coding.first()
            else -> Coding(null, null, it.toString())
        }
        val displayTextDe = it::class.declaredMemberProperties.find { it.name == "displayDe" }
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
    replaceGeccoIDWithLinkID(questionnnaire.item, qItemByGeccoId(questionnnaire))
    return questionnnaire;
}


fun replaceGeccoIDWithLinkID(qItems: List<QItem>, mapByExtension: Map<String, QItem>) {
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


fun qItemByGeccoId(questionnaire: Questionnaire): Map<String, QItem> {
    return questionnaire.allItems.associateBy { (it.getExtensionByUrl("https://num-compass.science/fhir/StructureDefinition/CompassGeccoItem").value as Coding).code }
}

private fun DropDownExtension() = Extension(
    "http://hl7.org/fhir/StructureDefinition/questionnaire-itemControl",
    CodeableConcept(Coding("http://hl7.org/fhir/questionnaire-item-control", "drop-down", "Drop down"))
)

fun CompassGeccoItemExtension(compassId: String) = Extension(
    COMPASS_GECCO_ITEM_EXTENSION,
    Coding(COMPASS_GECCO_ITEM_CS, compassId.removePrefix("."), null).setVersion("1.0")
)

/**
 * This extensions is needed by compass-num-conformance-checker
 */
private fun GeccoTargetProfileExtension(profile: String) =
    Extension("https://num-compass.science/fhir/StructureDefinition/GeccoTargetProfile", StringType(profile))

/**
 * This extension suppresses the ability to add a single item to the new Questionnaire by compass-questionniare-editor
 */
fun DependentItemExtension() =
    Extension("https://num-compass.science/fhir/StructureDefinition/DependentItem", BooleanType(true))

private fun QuestionnaireUnitExtension(coding: Coding) =
    Extension("http://hl7.org/fhir/StructureDefinition/questionnaire-unit", coding)



