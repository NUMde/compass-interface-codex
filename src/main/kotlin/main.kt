import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.SystemExitException
import com.xenomachina.argparser.default
import org.hl7.fhir.r4.model.*
import java.io.File
import java.io.FileReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

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
        help = "append to upload resources of GECCO profile bundle to their corresponding endpoints of target FHIR repository"
    )

    val uploadQuestionnaireResponses by parser.flagging(
        "--uploadQuestionnaireResponses",
        help = "append to upload resources of GECCO profile bundle to their corresponding endpoints of target FHIR repository"
    )

    val questionnairesFolder by parser.storing(
        "--questionnairesFolder",
        help = "Folder, where the Questionnaires are located, if you don't want to download them from compass-numapp-backend"
    ) { File(this) }.default<File?>(null)

    val simple by parser.flagging(
        "--noComposition",
        help = "Do not use document-Bundle containing Composition, Device, Organization and Questionnaire resource required for compass-num-conformance-checker"
    )
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

    fun findInFolder(folder:File, url: String, version: String): Questionnaire? {
        print(" from folder... ")
        for (file in folder.listFiles()) {
            val questionnaire = parser.parseResource(FileReader(file)) as Questionnaire
            if (questionnaire.url == url && questionnaire.version == version) {
                return questionnaire
            }
        }
        return null
    }
    suspend fun retrieveQuestionnaire(qr: QuestionnaireResponse): Questionnaire {
        val canonical = qr.questionnaire
        if (!cache.containsKey(canonical)) {
            print("  Retrieving corresponding Questionnaire '${qr.questionnaire}'")
            val url = canonical.substringBeforeLast("|")
            val version = canonical.substringAfterLast("|")
            if (parsedArgs.questionnairesFolder != null) {
                cache[canonical] = findInFolder(parsedArgs.questionnairesFolder!!, url, version) ?: throw Exception("Cannot find Questionnaire '${qr.questionnaire}' in folder!")
            } else {
                print(" from server... ")
                val questionnaireJson = downloader.retrieveQuestionnaireStringByUrlAndVersion(url, version, downloader.retrieveAccessToken())
                val questionnaire = parser.parseResource(questionnaireJson) as Questionnaire
                cache[canonical] = questionnaire
            }
            println("SUCCESS")
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

            val questionnaire = retrieveQuestionnaire(qr)

            print("  Adding extensions from Questionnaire to QuestionnaireResponse...")
            addExtensions(qr, questionnaire)
            println("SUCCESS")

            if (parsedArgs.uploadQuestionnaireResponses) {
                print("  Uploading QuestionnaireResponse... ")
                client.create().resource(qr).execute()
                println("SUCCESS")
            }

            val logicalModel = toLogicalModel(qr)
            print("  Mapping to GECCO resources... ")
            val bundleBuilder = if (parsedArgs.simple) {
                TransactionBundleBuilder()
            } else {
                ValidationServerBundleBuilder(Author(), App(), questionnaire)
            }
            val bundle = logicalModelToGeccoProfile(
                logicalModel,
                IdType(queueItem.SubjectId).withServerBase(fhirServerBase, "Patient"),
                DateTimeType(queueItem.AbsendeDatum),
                bundleBuilder
            )
            bundle.id = queueItem.UUID
            bundle.identifier.value = queueItem.UUID
            println("SUCCESS")


            if (parsedArgs.outputDirectory != null) {
                print("  Writing to files... ")
                parsedArgs.outputDirectory!!.mkdirs()
                parser.encodeResourceToWriter(qr, OutputStreamWriter(File(parsedArgs.outputDirectory!!, "${queueItem.SubjectId}-${queueItem.UUID}-qr.json").outputStream(),StandardCharsets.UTF_8))
                parser.encodeResourceToWriter(bundle, OutputStreamWriter(File(parsedArgs.outputDirectory!!, "${queueItem.SubjectId}-${queueItem.UUID}-gecco-bundle.json").outputStream(), StandardCharsets.UTF_8))
                println("SUCCESS")
            }

            if (parsedArgs.uploadBundle) {
                print("  Uploading entire Bundle to FHIR repository... ")
                client.update().resource(bundle).execute()
                println("SUCCESS")
            }


            if (parsedArgs.uploadBundleEntries) {
                println("  Uploading Bundle entries to FHIR repository... ")
                for ((index, entry) in bundle.entry.withIndex()) {
                    if (entry.resource !is Questionnaire && entry.resource !is Device && entry.resource !is Organization) {
                        try {
                            printAndFlush("    $index: ${entry.resource.meta.profile}")
                            client.create().resource(entry.resource).execute()
                            println(" DONE")
                        } catch (e: Exception) {
                            println()
                            println("Cannot upload ${entry.resource}: $e")
                            println(parser.encodeResourceToString(entry.resource))
                            println()
                        }
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
    } catch (e: Exception) {
        println("FAILED")
        throw e
    }
    println("SUCCESS")
}

fun printAndFlush(message: String) {
    print(message)
    System.out.flush()
}