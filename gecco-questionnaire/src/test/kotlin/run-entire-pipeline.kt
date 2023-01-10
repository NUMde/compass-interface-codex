import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.IdType
import java.io.FileWriter

/**
 * Call the entire pipeline (generate Questionnaire -> fill Questionnaire -> transform QuestionnaireResponse)
 * to spot runtime errors early.
 */
fun main() {
    val jsonParser = FhirContext.forR4().newJsonParser().setPrettyPrint(true)

    val questionnaire = LogicalModel.toQuestionnaire()
//    val questionnaire = jsonParser.parseResource(FileReader("./././questionnaire.json")) as Questionnaire

    val qr = generateResponse(questionnaire)
    //these commands need to be implemented in QuestionnaireResponseToLogicalModel now, if they are still wanted
    //addExtensions(qr, questionnaire)
    //println("Encoded QR")
    //jsonParser.encodeResourceToWriter(qr, System.out.writer())
    //println("end encoded qr")

    val logicalModel = toLogicalModel(questionnaire, qr)
    println("Current Logical Model")
    print(logicalModel.toString())
    println("End Current Logical Model")
    val geccoBundle = logicalModelToGeccoProfile(
        logicalModel,
        IdType("1234").withServerBase("http://example.com/fhir", "Patient"),
        DateTimeType.now(),
        ValidationServerBundleBuilder(Author(), App(), questionnaire)
    )
    jsonParser.encodeResourceToWriter(geccoBundle, System.out.writer())
    jsonParser.encodeResourceToWriter(geccoBundle, FileWriter("gecco-profiles.json"))

}