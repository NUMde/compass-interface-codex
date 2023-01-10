import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.DateType
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

typealias QR = QuestionnaireResponse
typealias QRItem = QuestionnaireResponse.QuestionnaireResponseItemComponent
typealias QItem = Questionnaire.QuestionnaireItemComponent
typealias QRAnswer = QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent

const val COMPASS_GECCO_ITEM_EXTENSION = "https://num-compass.science/fhir/StructureDefinition/CompassGeccoItem"

const val COMPASS_GECCO_ITEM_CS = "https://num-compass.science/fhir/CodeSystem/CompassGeccoItem"

val Questionnaire.allItems: List<QItem>
    get() = this.item + this.item.flatMap { it.allItems }

val QItem.allItems: List<QItem>
    get() = this.item + this.item.flatMap { it.allItems }

val QuestionnaireResponse.allItems: List<QRItem>
    get() = this.item + this.item.flatMap { it.allItems }

val QRItem.allItems: List<QRItem>
    get() = this.item + this.item.flatMap { it.allItems }


fun LocalDate.toFhir() = DateType(this.year, this.monthValue - 1, this.dayOfMonth)

fun DateTimeType.toLocalDateTime() =
    LocalDateTime.of(year, month + 1, day, hour, minute, second, millis * 1000000)!!

fun LocalDate.toUtilDate(): Date = Date.from(this.atStartOfDay(ZoneId.systemDefault()).toInstant())

/**
 * Helper function that copies all the extensions from the Questionnaire's items to the QuestionnaireResponse's items
 */
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

class UnknownLinkIdException(val linkId: String) : Exception() {
    override val message: String
        get() = "Encountered linkId '${linkId}' in QuestionnaireResponse, which does not exist in Questionnaire! " +
                "Please make sure, that Questionnaire and QuestionnaireResponse are corresponding to each other!"
}