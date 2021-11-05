import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.*
import java.io.File
import java.util.*
import kotlin.random.Random


fun main() {
    val ctx = FhirContext.forR4()
    val parser = ctx.newJsonParser().setPrettyPrint(true)
    val q =
        parser.parseResource(LogicalModel::class.java.getResource("/questionnaire.json").readText()) as Questionnaire
    val response = generateResponse(q)
    File("generated-response.json").writeText(parser.encodeResourceToString(response))
}

fun generateResponse(q: Questionnaire): QR {
    return QR().apply {
        questionnaire = q.url + "|" + q.version
        authoredElement = DateTimeType.now()
        item = q.item.mapNotNull { generateItem(it) }
    }
}

fun generateItem(item: QItem, answersMap: HashMap<String, List<QRAnswer>> = HashMap()): QRItem? {
    val enabled = item.enableWhen.all {
        if (it.hasAnswer()) {
            val enabledWhenAnswer = it.answer
            val actualAnswer = answersMap[it.question]
            if (enabledWhenAnswer != null && actualAnswer != null) {
                when (it.operator) {
                    Questionnaire.QuestionnaireItemOperator.EXISTS -> TODO()
                    Questionnaire.QuestionnaireItemOperator.EQUAL ->
                        (actualAnswer.firstOrNull()?.valueCoding?.code
                            ?: false) == (enabledWhenAnswer as Coding).code
                    Questionnaire.QuestionnaireItemOperator.NOT_EQUAL ->
                        (actualAnswer.firstOrNull()?.valueCoding?.code
                            ?: false) != (enabledWhenAnswer as Coding).code
                    Questionnaire.QuestionnaireItemOperator.GREATER_THAN -> TODO()
                    Questionnaire.QuestionnaireItemOperator.LESS_THAN -> TODO()
                    Questionnaire.QuestionnaireItemOperator.GREATER_OR_EQUAL -> TODO()
                    Questionnaire.QuestionnaireItemOperator.LESS_OR_EQUAL -> TODO()
                    Questionnaire.QuestionnaireItemOperator.NULL -> TODO()
                    else -> TODO()
                }
            } else true
        } else true
    }
    if (true) {
        return QRItem().apply {
            text = item.text
            linkId = item.linkId
            answer = listOf(
                QRAnswer().apply {
                    value = when {
                        item.answerOption.isNotEmpty() -> item.answerOption.shuffled().first().value
                        item.type == Questionnaire.QuestionnaireItemType.DECIMAL -> DecimalType(Random.nextDouble())
                        item.type == Questionnaire.QuestionnaireItemType.STRING -> StringType("Foo")
                        item.type == Questionnaire.QuestionnaireItemType.TEXT -> StringType("Foo")
                        item.type == Questionnaire.QuestionnaireItemType.DATE -> DateType(Date())
                        item.type == Questionnaire.QuestionnaireItemType.INTEGER -> IntegerType(Random.nextInt())
                        else -> null
                    }
                }
            )
            answersMap[item.linkId] = answer
            this.item = item.item.mapNotNull { generateItem(it, answersMap) }
        }
    } else {
        return null
    }
}
