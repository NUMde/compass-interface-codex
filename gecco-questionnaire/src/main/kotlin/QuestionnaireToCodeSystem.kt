import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.CodeSystem
import org.hl7.fhir.r4.model.CodeType
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Enumerations


val codeSystem = CodeSystem().apply {
    id = "CompassGeccoItem"
    url = COMPASS_GECCO_ITEM_CS //TODO
    version = "1.0"
    title = "CompassGeccoItem"
    status = Enumerations.PublicationStatus.ACTIVE
    compositional = false
    content = CodeSystem.CodeSystemContentMode.COMPLETE
    concept = toQuestionnaire().allItems.map {
        CodeSystem.ConceptDefinitionComponent(
            CodeType((it.getExtensionByUrl(COMPASS_GECCO_ITEM_EXTENSION).value as Coding).code)
        )
    } //TODO: make hierarchical
    count = concept.size
}


fun main() {
    println(FhirContext.forR4().newJsonParser().setPrettyPrint(true).encodeResourceToString(codeSystem))

}