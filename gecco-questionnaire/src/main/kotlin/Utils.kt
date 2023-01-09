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
