import ca.uhn.fhir.util.BundleBuilder
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.*
import java.time.LocalDateTime
import java.util.*

fun uncertaintyOfPresenceExt() = Extension(
    "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/uncertainty-of-presence",
    CodeableConcept(snomed("261665006", "Unknown (qualifier value)"))
)

fun unknownDateTime() = DateTimeType().apply {
    extension = listOf(dataAbsentReasonUnknown())
}

fun unknownPeriod() = Period().apply {
    extension = listOf(dataAbsentReasonUnknown())
}

fun dataAbsentReasonUnknown() =
    Extension("http://hl7.org/fhir/StructureDefinition/data-absent-reason", CodeType("unknown"))

fun frailty_score(code: String, display: String) = Coding(
    "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/frailty-score", code, display
)

fun LocalDateTime.toUtilDate(): Date = java.sql.Timestamp.valueOf(this)

/**
 * Polyfill to stay compatible with HAPI 5.2.1 code
 */
fun BundleBuilder.addCreateEntry(resource: IBaseResource) {
    this.addTransactionCreateEntry(resource)
}


fun snomed(code: String, display: String) = Coding("http://snomed.info/sct", code, display)
fun loinc(code: String, display: String) = Coding("http://loinc.org", code, display)
fun icd10gm(code: String, display: String) = Coding("http://fhir.de/CodeSystem/dimdi/icd-10-gm", code, display)

//TODO: change to http://fhir.de/CodeSystem/bfarm/icd-10-gm
fun alpha_id(code: String, display: String) = Coding("http://fhir.de/CodeSystem/bfarm/alpha-id", code, display)
fun orphanet(code: String, display: String) = Coding("http://www.orpha.net", code, display)
fun atc(code: String, display: String) = Coding("http://fhir.de/CodeSystem/dimdi/atc", code, display)
fun dicom(code: String, display: String) = Coding("http://dicom.nema.org/resources/ontology/DCM", code, display)
fun iso3166_1_2(code: String, display: String) = Coding("urn:iso:std:iso:3166", code, display)
fun iso3155DE(code: String, display: String) = Coding("urn:iso:std:iso:3166-2:de", code, display)

fun org.hl7.fhir.r4.model.codesystems.ObservationCategory.toCoding() =
    Coding(this.system, this.toCode(), this.display)

fun org.hl7.fhir.r4.model.codesystems.ConditionVerStatus.toCoding() =
    Coding(this.system, this.toCode(), this.display)

/**
 * Constructor for a CodeableConcept with multiple Codings
 */
fun CodeableConcept(coding: Coding, coding2: Coding, vararg codings: Coding, text: String? = null): CodeableConcept {
    return CodeableConcept(coding).apply {
        addCoding(coding2)
        for (coding in codings) {
            addCoding(coding)
        }
        setText(text)
    }
}

/**
 * Get CodeableEnum by given Coding
 */
inline fun <reified T> getByCoding(coding: Coding): T? where T : CodeableEnum<T>, T : Enum<T> {
    return enumValues<T>().find { it.coding.system == coding.system && it.coding.code == coding.code }
}

/**
 * Get enum which is coded with CodeableConcept by given Coding
 */
inline fun <reified T> getByCoding2(coding: Coding): T? where T : ConceptEnum<T>, T : Enum<T> {
    return enumValues<T>().find { it.codeableConcept.coding.any { it.system == coding.system && it.code == coding.code } }
}
