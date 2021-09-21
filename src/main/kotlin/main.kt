import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.SystemExitException
import com.xenomachina.argparser.default
import org.hl7.fhir.r4.model.*
import java.io.File
import java.io.FileWriter

class MyArgs(parser: ArgParser) {
    val serverUrl by parser.storing(
        "--serverUrl",
        help = "Compass backend server URL")

    val apiId by parser.storing(
        "--apiId",
        help = "Compass backend server apiId")

    val apiKey by parser.storing(
        "--apiKey",
        help = "Compass backend server apiKey")

    val privateKey by parser.storing(
        "--privateKey",
        help = "Compass backend server private key file") { File(this) }

    val publicKey by parser.storing(
        "--publicKey",
        help = "Compass backend server public key file"){ File(this) }

    val certificate by parser.storing(
        "--certificate",
        help = "Compass backend server certificate key file"){ File(this) }

    val outputDirectory by parser.storing(
        "--outDir",
        help = "Output directory to write JSON files"){ File(this) }.default<File?>(null)

    val targetFhirRepository by parser.storing(
         "--targetFhirRepository",
        help = "Target FHIR Repository. Is required, even if you don't want to upload, as it will be used in the internal generated IDs")

    val basicAuth by parser.storing(
        "--basicAuth",
        help = "Credentials for target FHIR repository. Format: username:password").default<String?>(null)

    val uploadBundle by parser.flagging(
        "--uploadBundle",
        help = "append to upload GECCO profile bundle to /Bundle endpoint of target FHIR repository")

    val uploadBundleEntries by parser.flagging(
        "--uploadBundleEntries",
        help = "append to upload resources of GECCO profile bundle to their corresponding endpoints of target FHIR repository")

    val uploadQuestionnaires by parser.flagging(
        "--uploadQuestionnaires",
        help = "append to upload resources of GECCO profile bundle to their corresponding endpoints of target FHIR repository")

    val uploadQuestionnaireResponses by parser.flagging(
        "--uploadQuestionnaireResponses",
        help = "append to upload resources of GECCO profile bundle to their corresponding endpoints of target FHIR repository")
}





suspend fun main(args: Array<String>) {
    val parsedArgs = try {
        ArgParser(args).parseInto(::MyArgs)
    } catch (e: SystemExitException) {
        e.printAndExit("compass-interface-codex",System.getenv("COLUMNS")?.toInt() ?: 119)
    }

    val ctx = FhirContext.forR4()
    val parser = ctx.newJsonParser().setPrettyPrint(true)
    val fhirServerBase = parsedArgs.targetFhirRepository
    val client by lazy { // lazy => don't throw error if client is not used
        ctx.newRestfulGenericClient(fhirServerBase).apply {
            parsedArgs.basicAuth?.let{ registerInterceptor(BasicAuthInterceptor(it.substringBeforeLast(":"), it.substringAfterLast(":"))) }
        }
    }


    val downloader = CompassDownloader(
        serverUrl = parsedArgs.serverUrl,
        apiID = parsedArgs.apiId,
        apiKey =  parsedArgs.apiKey,
        publicKey = PemUtils.loadPublicKey(parsedArgs.publicKey),
        privateKey = PemUtils.loadPrivateKey(parsedArgs.privateKey),
        cert = PemUtils.loadCert(parsedArgs.certificate)
    )

    val cache = mutableMapOf<String, Questionnaire>()
    suspend fun retrieveQuestionnaire(qr: QuestionnaireResponse): Questionnaire {
        val canonical = qr.questionnaire
        if (!cache.containsKey(canonical)) {
            val questionnaireJson = downloader.retrieveQuestionnaireStringByUrlAndVersion(canonical.substringBeforeLast("|"), canonical.substringAfterLast("|"), downloader.retrieveAccessToken())
            val questionnaire = parser.parseResource(questionnaireJson) as Questionnaire
            cache[canonical] = questionnaire
        }
        return cache[canonical]!!
    }

    val queueItems = downloader.retrieveAllQueueItems()
    for (queueItem in queueItems) {
        try {
            println("Processing ${queueItem.UUID}:")
            print("  Decrypting... ")
            val decryptedQueueItem = downloader.decryptQueueItem(queueItem)
            println("SUCCESS")

            print("  Parsing FHIR QuestionnaireResponse... ")
            val qrString = decryptedQueueItem.data.bodyAsString
            val qr = parser.parseResource(qrString) as QuestionnaireResponse
            println("SUCCESS")

            print("  Retrieving corresponding Questionnaire and adding extensions... ")
            val questionnaire = retrieveQuestionnaire(qr)
            addExtensions(qr, questionnaire)
            println("SUCCESS")

            if (parsedArgs.uploadQuestionnaireResponses) {
                print("  Uploading QuestionnaireResponse... ")
                client.create().resource(qr).execute()
                println("SUCCESS")
            }

            val logicalModel = toLogicalModel(qr)
            println("  Mapping to GECCO Profile... ")
            val bundle = logicalModelToGeccoProfile(
                logicalModel,
                IdType(queueItem.SubjectId).withServerBase(fhirServerBase, "Patient"),
                DateTimeType(queueItem.AbsendeDatum),
                ValidationServerBundleBuilder(Author(), App(), questionnaire)
            )
            bundle.id = queueItem.UUID
            bundle.identifier.value = queueItem.UUID
            println("SUCCESS")


            if (parsedArgs.outputDirectory != null) {
                print("  Writing to files... ")
                parsedArgs.outputDirectory!!.mkdirs()
                parser.encodeResourceToWriter(qr, FileWriter(File(parsedArgs.outputDirectory!!, "${queueItem.SubjectId}-${queueItem.UUID}-qr.json")))
                parser.encodeResourceToWriter(bundle, FileWriter(File(parsedArgs.outputDirectory!!, "${queueItem.SubjectId}-${queueItem.UUID}-gecco-bundle.json")))
                println("SUCCESS")
            }

            if (parsedArgs.uploadBundle) {
                print("  Uploading entire Bundle to FHIR repository... ")
                client.update().resource(bundle).execute()
                println("SUCCESS")
            }


            if (parsedArgs.uploadBundleEntries) {
                print("  Uploading Bundle entries to FHIR repository... ")
                for(entry in bundle.entry) {
                    if(entry.resource !is Questionnaire && entry.resource !is Device && entry.resource !is Organization) {
                        client.create().resource(entry.resource).execute()
                    }
                }
                println("SUCCESS")
            }
            println()
        } catch (e: Exception) {
            println("ERROR \n    $e\n")
        }
    }


    if (parsedArgs.uploadQuestionnaires) {
        print("POSTing Questionnaires to FHIR repository... ")
        for ((url, q) in cache) {
            client.create().resource(q).conditional()
                .where(Questionnaire.URL.matches().value(q.url))
                .and(Questionnaire.VERSION.exactly().code(q.version))
                .execute()
        }
        println("SUCCESS")
    }
}

fun wrapProgress(message: String, block: () -> Unit) {
    print("  $message...")
    try {
        block()
    } catch (e: Exception){
        println("FAILED")
        throw e
    }
    println("SUCCESS")
}