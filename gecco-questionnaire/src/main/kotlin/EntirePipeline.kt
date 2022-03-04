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
        jsonParser.parseResource(FileReader("./././questionnaire.json")) as Questionnaire
    val qr = generateResponse(questionnaire)
    //these commands need to be implemented in QuestionnaireResponseToLogicalModel now, if they are still wanted
    //addExtensions(qr, questionnaire)
    //println("Encoded QR")
    //jsonParser.encodeResourceToWriter(qr, System.out.writer())
    //println("end encoded qr")
    val lm = toLogicalModel(questionnaire, qr)
    println("Current Logical Model")
    print(lm.toString())
    println("End Current Logical Model")
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