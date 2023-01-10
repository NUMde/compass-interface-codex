import org.hl7.fhir.r4.model.Questionnaire
import java.io.File
suspend fun main() {
//    val parser = FhirContext.forR4().newJsonParser()
    val folder =
        "C:\\Users\\oehmj\\IdeaProjects\\compass-interface-codex\\compass-download-kotlin\\src\\main\\resources"
    val downloader = CompassDownloader(
        serverUrl = "http://127.0.0.1:8081/",
        apiID = "test",
        apiKey = "gKdKLYG2g0-Y1EllI0-W",
        publicKey = PemUtils.loadPublicKey(File(folder, "public_key.pem")),
        privateKey = PemUtils.loadPrivateKey(File(folder, "private_key.pem")),
        certificate = PemUtils.loadCertificate(File(folder, "cacert.pem"))
    )

    val q = Questionnaire().apply {
        url = "https://medical-data-models.org/1234"
        version = "1.0"
    }

    val accessToken = downloader.retrieveAccessToken()

//    downloader.addQuestionnaire("myName2", parser.encodeResourceToString(q), accessToken)
    downloader.addQuestionnaire("GECCO", File("questionnaire.json").readText(), accessToken)
//    q.version = "1.1"
//    q.copyright = "ich bin ein Test!"
//    downloader.updateQuestionnaire("myName2", parser.encodeResourceToString(q), accessToken)
//
//    val q2 = downloader.retrieveQuestionnaireStringByUrlAndVersion(q.url, "1.0", accessToken)
//    println(q2)
//
//    val q3 = downloader.retrieveQuestionnaireStringByUrlAndVersion(q.url, "1.1", accessToken)
//    println(q3)

}