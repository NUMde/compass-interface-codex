import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.*
import java.io.FileReader
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import kotlin.reflect.*
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.withNullability

fun main() {
    val jsonParser = FhirContext.forR4().newJsonParser().setPrettyPrint(true)
    val questionnaire =
        jsonParser.parseResource(FileReader("./././questionnaire.json")) as Questionnaire
    val questionnaireResponse = generateResponse(questionnaire)

    val logicalModel = toLogicalModel(questionnaire, questionnaireResponse, getRenderersForGecco())

    println(logicalModel)

}

fun getRenderersForGecco() = listOf(
    YesNoUnknownWithDateRenderer(),
    YesNoUnknownWithIntentRenderer(),
    YesNoUnknownWithSeverityRenderer()
)


fun toLogicalModel(
    questionnaire: Questionnaire,
    qr: QuestionnaireResponse,
    renderers: List<ClassToItemRenderer<*>>
): LogicalModel {
    copyExtensions(qr, questionnaire)
    val qrAnswerByGeccoId = qrAnswerByGeccoId(qr).filter { it.value?.value != null } as Map<String, QRAnswer>

    val logicalModel = LogicalModel()
    val specialCases = mutableMapOf<String, Pair<QRAnswer, KCallable<*>>>()
    for ((geccoId, answer) in qrAnswerByGeccoId) {
        val (parent, property) = logicalModel.getPropertyThroughReflectionRecursive(geccoId)
        property ?: error("Cannot find property for $geccoId")

        val value = if ((property.returnType.classifier as KClass<*>).javaObjectType.isEnum) {
            extractEnum(answer.value as Coding, property.returnType)
        } else if (isSpecialClass(property.returnType, renderers)) {
            specialCases[geccoId] = answer to property
            null
        } else {
            when (property.returnType) {
                typeOf<Int?>() -> toInt(answer.value)
                typeOf<Float?>() -> toFloat(answer.value)
                typeOf<Double?>() -> toFloat(answer.value)?.toDouble()
                typeOf<Date?>() -> toDate(answer.value)
                typeOf<LocalDate?>() -> (answer.value as? DateType)?.toLocalDate()
                typeOf<String?>() -> toStringValue(answer.value)
                typeOf<YesNoUnknown?>() -> toYesNoUnknown(answer.value)
                typeOf<YesNoUnknownOtherNa>() -> toYesNoUnknownOtherNa(answer.value)
                else -> null
            }
        }
        if (value != null) {
            parent.setPropertyThroughReflection(geccoId.substringAfterLast("."), value)
        }
    }

    for ((prefix, elements) in specialCases.entries.groupBy { it.key.substringBeforeLast(".") }) {
        val (_, property) = elements.first().value
        val answerByGeccoId = elements.map { it.key to it.value.first }.toMap()
        val value = renderers.find { it.getType() == property.returnType.withNullability(false) }
            ?.parse(prefix, answerByGeccoId)
        val (that, _) = logicalModel.getPropertyThroughReflectionRecursive(prefix)
        (property as KMutableProperty<*>).setter.call(that, value)
    }

    return logicalModel
}

fun isSpecialClass(type: KType, renderers: List<ClassToItemRenderer<*>>): Boolean {
    return renderers.any { it.getType() == type.withNullability(false) }
}

fun extractEnum(coding: Coding, returnType: KType): Enum<*> {
    return when (returnType) {
        typeOf<ChronicKidneyDisease?>() -> getByCoding2<ChronicKidneyDisease>(coding)
        typeOf<Diabetes?>() -> getByCoding2<Diabetes>(coding)
        typeOf<Countries?>() -> getByCoding<Countries>(coding)
        typeOf<FederalStates?>() -> getByCoding<FederalStates>(coding)
        typeOf<CancerStatus?>() -> getByCoding<CancerStatus>(coding)
        typeOf<Resuscitation?>() -> getByCoding<Resuscitation>(coding)
        typeOf<SmokingStatus?>() -> getByCoding2<SmokingStatus>(coding)
        typeOf<BirthSex?>() -> getByCoding<BirthSex>(coding)
        typeOf<EthnicGroup?>() -> getByCoding<EthnicGroup>(coding)
        typeOf<FrailityScore?>() -> getByCoding2<FrailityScore>(coding)
        typeOf<PregnancyStatus?>() -> getByCoding2<PregnancyStatus>(coding)
        typeOf<Covid19Vaccine?>() -> getByCoding<Covid19Vaccine>(coding)
        typeOf<RadiologicFindings?>() -> getByCoding2<RadiologicFindings>(coding)
        typeOf<ACEInhibitorAdministration?>() -> getByCoding<ACEInhibitorAdministration>(coding)
        typeOf<StageAtDiagnosis?>() -> getByCoding2<StageAtDiagnosis>(coding)
        typeOf<DetectedNotDetectedInconclusive?>() ->
            getByCoding2<DetectedNotDetectedInconclusive>(coding)

        typeOf<TypeOfDischarge?>() -> getByCoding2<TypeOfDischarge>(coding)
        typeOf<VentilationTypes?>() -> getByCoding<VentilationTypes>(coding)
        typeOf<YesNoUnknown?>() -> toYesNoUnknown(coding)
        typeOf<YesNoUnknownOtherNa?>() -> toYesNoUnknownOtherNa(coding)
        else -> error("No enum for $returnType  $coding")
    }!!
}

fun LogicalModel.getPropertyThroughReflectionRecursive(geccoId: String): Pair<Any, KCallable<*>?> {
    val obj = this.getInstanceThroughReflection<Any>(geccoId.substringBefore("."))
    return obj!!.getPropertyThroughReflectionRecursive(geccoId.substringAfter("."))
}

fun Any.getPropertyThroughReflectionRecursive(geccoId: String): Pair<Any, KCallable<*>?> {
    if (geccoId.contains(".")) {
        val obj = this.getInstanceThroughReflection<Any>(geccoId.substringBefore("."))
        if (obj == null) {
            return this to this.getPropertyThroughReflection(geccoId.substringBefore("."))
        }
        return obj.getPropertyThroughReflectionRecursive(geccoId.substringAfter("."))
    } else {
        return this to this.getPropertyThroughReflection(geccoId)
    }
}

fun qrAnswerByGeccoId(qr: QuestionnaireResponse): Map<String, QRAnswer?> {
    return qr.allItems.groupBy { (it.getExtensionByUrl(COMPASS_GECCO_ITEM_EXTENSION)?.value as Coding).code }
        .mapValues { it.value.firstOrNull()?.answer?.firstOrNull() }
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
        val format = value.precision.toSimpleDateFormat()
        return format?.parse(value.valueAsString)
    }

    return null
}

fun toLocalDate(value: Type): LocalDate? {
    if (value is DateType) {
        val format = value.precision.toSimpleDateFormat()
        return format?.parse(value.valueAsString)?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
    }

    return null
}


fun toFloat(value: Type) = (value as? DecimalType)?.value?.toFloat()
fun toInt(value: Type?) = (value as? IntegerType)?.value?.toInt()
fun toStringValue(value: Type) = (value as? StringType)?.valueAsString


fun Any.getPropertyThroughReflection(propertyName: String): KCallable<*>? {
    return kotlin.runCatching {
        this::class.members.find { it.name == propertyName }
    }.getOrNull()
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
