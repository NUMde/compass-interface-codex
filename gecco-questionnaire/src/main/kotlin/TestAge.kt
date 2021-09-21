import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.IdType
import java.time.LocalDate
import java.util.*

fun main(){
    val lm = LogicalModel(
        demographics = Demographics(
//            ageInYears = 22,
//            ageInMonth = 22*12,
            dateOfBirth = LocalDate.of(1990, 1, 1)
        )
    )
    val bundle =
        logicalModelToGeccoProfile(lm, IdType("123"), DateTimeType.now(), TransactionBundleBuilder())
    println(FhirContext.forR4().newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle.entry.first().resource))
}