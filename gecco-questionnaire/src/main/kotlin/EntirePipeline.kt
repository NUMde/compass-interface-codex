import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.IdType
import org.hl7.fhir.r4.model.Questionnaire
import java.io.FileReader
import java.io.FileWriter


fun main() {
//    val questionnaire = toQuestionnaire()
    val jsonParser = FhirContext.forR4().newJsonParser().setPrettyPrint(true)
    val questionnaire =
        jsonParser.parseResource(FileReader("C:\\Users\\oehmj\\IdeaProjects\\compass-interface-codex\\questionnaire.json")) as Questionnaire
    val qr = generateResponse(questionnaire)
    addExtensions(qr, questionnaire)
    jsonParser.encodeResourceToWriter(qr, System.out.writer())
    val lm = toLogicalModel(qr)
    val patientId = IdType("1234").withServerBase("http://example.com/fhir", "Patient")
    val geccoBundle = logicalModelToGeccoProfile(
        lm,
        patientId,
        DateTimeType.now(),
        ValidationServerBundleBuilder(Author(), App(), questionnaire)
    )
    jsonParser.encodeResourceToWriter(geccoBundle, System.out.writer())
    jsonParser.encodeResourceToWriter(geccoBundle, FileWriter("gecco-profiles.json"))

}