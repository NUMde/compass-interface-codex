import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor
import org.hl7.fhir.r4.model.*
import java.io.File
import java.io.FileWriter


/**
 * This is a sample main function, which shows you the basic usage and how you can use this to create your own converter
 * for your own project.
 */
suspend fun main(args: Array<String>) {
    val ctx = FhirContext.forR4()
    val parser = ctx.newJsonParser().setPrettyPrint(true)

    //Replace with your own credentials
    val downloader = CompassDownloader(
        serverUrl = "http://127.0.0.1:8080/",
        apiID = "test",
        apiKey =  "gKdKLYG2g0-Y1EllI0-W",
        publicKey = PemUtils.loadPublicKey(File("public-key.pem")),
        privateKey = PemUtils.loadPrivateKey(File("private-key.pem")),
        cert = PemUtils.loadCert(File("cacert.pem"))
    )

    //Replace with your own credentials
    val fhirServerBase = "http://example.com/fhir"
    val client = ctx.newRestfulGenericClient(fhirServerBase).apply {
        registerInterceptor(BasicAuthInterceptor("user", "pass"))
    }

    for (queueItem in downloader.retrieveAllQueueItems()) {
        try {
            val decryptedQueueItem = downloader.decryptQueueItem(queueItem)
            val questionnaireResponse = parser.parseResource(decryptedQueueItem.data.bodyAsString) as QuestionnaireResponse

            val logicalModel = toLogicalModel(questionnaireResponse)

            val patientId = IdType(queueItem.SubjectId).withServerBase(fhirServerBase, "Patient")
            val bundle = logicalModelToGeccoProfile(
                logicalModel,
                patientId,
                DateTimeType(queueItem.AbsendeDatum),
                //Validationserverbundle also includes Device and Organization resource as well as original Questionnaire for D4L validation server
//                ValidationServerBundleBuilder(Author(), App(), questionnaire)
                TransactionBundleBuilder()
            )
            //optional - but makes things easier later on
            bundle.id = queueItem.UUID


            //Late mapping scheme - gecco-easy template function
            val hasDryCough = questionnaireResponse["1.2.3"]!!.answerFirstRep.toEnum1<YesNoUnknown>()!! //Attention: this requires Coding.system and Coding.code to be identical - otherwise: use your own when(){} statement
            bundle.addEntry().setResource(Symptom(Reference(patientId), Symptom.DRY_COUGH, hasDryCough, DateTimeType(queueItem.AbsendeDatum)))

            //Late mapping scheme - add by manually creating resource
            bundle.addEntry().setResource(Observation().apply {
                meta = Meta().addProfile("https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/DiagnosticReportLab")
                subject = Reference(patientId)
                status = Observation.ObservationStatus.FINAL
                category = listOf(CodeableConcept(loinc("26436-6", "Laboratory studies (set)")))
                code = CodeableConcept(loinc("1234-7", "sample loinc code"))
                effective = DateTimeType(queueItem.AbsendeDatum)
                value = questionnaireResponse["2.3.4"]!!.answerFirstRep.valueStringType
            })

            //Just an example of what you can do with the final transformation result:
            client.create().resource(bundle).execute() //Uplaod to server
            parser.encodeResourceToWriter(bundle, FileWriter("my-outfile.fhir.json")) //write to file

        } catch (e: Exception) {
            println("Error with queueItem=${queueItem.UUID}: $e")
        }
    }
}


//Helper functions
val QuestionnaireResponse.allItems: List<QRItem>
    get() = this.item + this.item.flatMap { it.allItems }

val QRItem.allItems: List<QRItem>
    get() = this.item + this.item.flatMap { it.allItems }

operator fun QuestionnaireResponse.get(linkId: String) = this.allItems.find { it.linkId == linkId }
