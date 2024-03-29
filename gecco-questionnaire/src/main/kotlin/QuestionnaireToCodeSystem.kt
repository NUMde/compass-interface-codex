import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.CodeSystem
import org.hl7.fhir.r4.model.CodeType
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Enumerations


/**
 * This class creates a FHIR CodeSystem resource for all CompassGeccoItem codes in the sample questionnaire
 */
fun main() {
    val codeSystem = CodeSystem().apply {
        id = "CompassGeccoItem"
        url = COMPASS_GECCO_ITEM_CS
        version = "1.0"
        title = "CompassGeccoItem"
        status = Enumerations.PublicationStatus.ACTIVE
        compositional = false
        content = CodeSystem.CodeSystemContentMode.COMPLETE
        concept = LogicalModel.toQuestionnaire().allItems.map {
            CodeSystem.ConceptDefinitionComponent(
                CodeType((it.getExtensionByUrl(COMPASS_GECCO_ITEM_EXTENSION).value as Coding).code)
            )
        } //TODO: make hierarchical
        count = concept.size
    }


    println(FhirContext.forR4().newJsonParser().setPrettyPrint(true).encodeResourceToString(codeSystem))
}