import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.IdType
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Quantity
import java.time.LocalDate

fun main() {
    val parser = FhirContext.forR4().newJsonParser()

    val lm = LogicalModel(
        demographics = Demographics(
//            ageInYears = 22,
//            ageInMonth = 22*12,
            dateOfBirth = LocalDate.of(1990, 1, 1)
        )
    )
    val bundle = logicalModelToGeccoProfile(lm, IdType("123"), DateTimeType.now(), TransactionBundleBuilder())

    val patientResource = bundle.entry.first().resource as Patient
    println(parser.setPrettyPrint(true).encodeResourceToString(patientResource))

    assert(patientResource.birthDate.toString() == "1990-01-01")

    val age = patientResource
        .getExtensionByUrl("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/age")
        .getExtensionByUrl("age")
        .value?.let { (it as Quantity).value.toInt() }

    assert(age == LocalDate.now().year - 1990)
}