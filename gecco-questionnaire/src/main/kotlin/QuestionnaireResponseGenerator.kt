import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.*
import org.hl7.fhir.r4.model.Questionnaire.QuestionnaireItemOperator.*
import org.hl7.fhir.r4.model.Questionnaire.QuestionnaireItemType.*
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
//        status = QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED
        questionnaire = q.url + "|" + q.version
        authoredElement = DateTimeType.now()
        item = q.item.mapNotNull { generateItem(it) }
    }
}

private fun generateItem(item: QItem, answersMap: MutableMap<String, List<QRAnswer>> = mutableMapOf()): QRItem? {
    val enabled = item.enableWhen.all {
        if (it.hasAnswer()) {
            val enabledWhenAnswer = (it.answer as? Coding)?.code
            val actualAnswer = answersMap[it.question]?.firstOrNull()?.valueCoding?.code
            if (enabledWhenAnswer != null && actualAnswer != null) {
                when (it.operator) {
                    EXISTS -> TODO()
                    EQUAL -> actualAnswer == enabledWhenAnswer
                    NOT_EQUAL -> actualAnswer != enabledWhenAnswer
                    GREATER_THAN -> TODO()
                    LESS_THAN -> TODO()
                    GREATER_OR_EQUAL -> TODO()
                    LESS_OR_EQUAL -> TODO()
                    else -> TODO()
                }
            } else true
        } else true
    }
    if (true) { // (enabled) {
        return QRItem().apply {
            text = item.text
            linkId = item.linkId
            answer = listOf(QRAnswer().apply {
                value = when {
                    item.answerOption.isNotEmpty() -> item.answerOption.random().value
                    item.type == DECIMAL -> DecimalType(Random.nextDouble())
                    item.type == STRING -> StringType("Foo")
                    item.type == TEXT -> StringType("Foo")
                    item.type == DATE -> DateType(Date())
                    item.type == INTEGER -> IntegerType(Random.nextInt())
                    else -> null
                }
            })
            answersMap[item.linkId] = answer
            this.item = item.item.mapNotNull { generateItem(it, answersMap) }
        }
    } else {
        return null
    }
}
