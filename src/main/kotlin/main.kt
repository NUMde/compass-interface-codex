import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
import ca.uhn.fhir.rest.client.api.IGenericClient
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.SystemExitException
import com.xenomachina.argparser.default
import org.hl7.fhir.r4.model.*
import java.io.File
import java.io.FileReader

class MyArgs(parser: ArgParser) {
    val serverUrl by parser.storing("--serverUrl", help = "Compass backend server URL")
    val apiId by parser.storing("--apiId", help = "Compass backend server apiId")
    val apiKey by parser.storing("--apiKey", help = "Compass backend server apiKey")

    val privateKey by parser.storing("--privateKey", help = "Compass backend server private key file") { File(this) }
    val publicKey by parser.storing("--publicKey", help = "Compass backend server public key file") { File(this) }
    val certificate by parser.storing(
        "--certificate",
        help = "Compass backend server certificate key file"
    ) { File(this) }

    val outputDirectory by parser.storing("--outDir", help = "Output directory to write JSON files") { File(this) }
        .default<File?>(null)

    val targetFhirRepository by parser.storing(
        "--targetFhirRepository",
        help = "Target FHIR Repository. Is required, even if you don't want to upload, as it will be used in the internal generated IDs"
    )
    val basicAuth by parser.storing(
        "--basicAuth",
        help = "Credentials for target FHIR repository. Format: username:password"
    ).default<String?>(null)

    val uploadBundle by parser.flagging(
        "--uploadBundle",
        help = "append to upload GECCO profile bundle to /Bundle endpoint of target FHIR repository"
    )

    val uploadBundleEntries by parser.flagging(
        "--uploadBundleEntries",
        help = "append to upload resources of GECCO profile bundle to their corresponding endpoints of target FHIR repository"
    )

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

const val COMPASS_SUBJECT_ID = "https://num-compass.science/fhir/NamingSystem/CompassSubjectId"

/**
 * CLI (Command line interface) entry point
 */
suspend fun main(args: Array<String>) {
    val parsedArgs = try {
        ArgParser(args).parseInto(::MyArgs)
    } catch (e: SystemExitException) {
        e.printAndExit("compass-interface-codex", System.getenv("COLUMNS")?.toInt() ?: 119)
    }

    val ctx = FhirContext.forR4()
    val parser = ctx.newJsonParser().setPrettyPrint(true)
    val fhirServerBase = parsedArgs.targetFhirRepository
    val client by lazy { // lazy => don't throw error if client is not used
        ctx.newRestfulGenericClient(fhirServerBase).apply {
            parsedArgs.basicAuth?.let {
                registerInterceptor(
                    BasicAuthInterceptor(it.substringBeforeLast(":"), it.substringAfterLast(":"))
                )
            }
        }
    }


    val downloader = CompassDownloader(
        serverUrl = parsedArgs.serverUrl,
        apiID = parsedArgs.apiId,
        apiKey = parsedArgs.apiKey,
        publicKey = PemUtils.loadPublicKey(parsedArgs.publicKey),
        privateKey = PemUtils.loadPrivateKey(parsedArgs.privateKey),
        certificate = PemUtils.loadCertificate(parsedArgs.certificate)
    )

    val cache = mutableMapOf<String, Questionnaire>()

    fun findInFolder(folder: File, url: String, version: String): Questionnaire? {
        print(" from folder... ")
        for (file in folder.listFiles() ?: error("Cannot read Questionnaire files in folder '$folder'!")) {
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
            printAndFlush("  Retrieving corresponding Questionnaire '${qr.questionnaire}'")
            val url = canonical.substringBeforeLast("|")
            val version = canonical.substringAfterLast("|")
            if (parsedArgs.questionnairesFolder != null) {
                cache[canonical] = findInFolder(parsedArgs.questionnairesFolder!!, url, version)
                    ?: error("Cannot find Questionnaire '${qr.questionnaire}' in folder!")
            } else {
                printAndFlush(" from server... ")
                val questionnaireJson = downloader.retrieveQuestionnaireStringByUrlAndVersion(
                    url,
                    version,
                    downloader.retrieveAccessToken()
                )
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
            printAndFlush("  Decrypting... ")
            val decryptedQueueItem = downloader.decryptQueueItem(queueItem)
            println("SUCCESS")

            printAndFlush("  Parsing FHIR QuestionnaireResponse... ")
            val qrString = decryptedQueueItem.data.bodyAsString
            val qr = parser.parseResource(qrString) as QuestionnaireResponse
            println("SUCCESS")

            val questionnaire = retrieveQuestionnaire(qr)

            printAndFlush("  Adding extensions from Questionnaire to QuestionnaireResponse...")
            copyExtensions(qr, questionnaire)
            println("SUCCESS")

            if (parsedArgs.uploadQuestionnaireResponses) {
                printAndFlush("  Uploading QuestionnaireResponse... ")
                client.create().resource(qr).execute()
                println("SUCCESS")
            }

            val logicalModel = toLogicalModel(questionnaire, qr, getRenderersForGecco())
            printAndFlush("  Mapping to GECCO resources... ")
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
                printAndFlush("  Writing to files... ")
                val outDir = parsedArgs.outputDirectory!!
                outDir.mkdirs()
                parser.encodeResourceToWriter(
                    qr, File(outDir, "${queueItem.SubjectId}-${queueItem.UUID}-qr.json").writer(),
                )
                parser.encodeResourceToWriter(
                    bundle, File(outDir, "${queueItem.SubjectId}-${queueItem.UUID}-gecco-bundle.json").writer(),
                )
                println("SUCCESS")
            }

            if (parsedArgs.uploadBundle) {
                printAndFlush("  Uploading entire Bundle to FHIR repository... ")
                client.update().resource(bundle).execute()
                println("SUCCESS")
            }

            if (parsedArgs.uploadBundleEntries) {
                println("  Uploading Bundle entries to FHIR repository... ")
                uploadBundleEntries(bundle, queueItem, client, parser)
                println("  SUCCESS")
            }
            println()
        } catch (e: Exception) {
            println("ERROR \n    $e\n")
        }
    }


    if (parsedArgs.uploadQuestionnaires) {
        printAndFlush("POSTing Questionnaires to FHIR repository... ")
        for ((_, q) in cache) {
            client.create().resource(q).conditional()
                .where(Questionnaire.URL.matches().value(q.url))
                .and(Questionnaire.VERSION.exactly().code(q.version))
                .execute()
        }
        println("SUCCESS")
    }
}

/**
 * Upload all Bundle entries individually with POST requests because fhirbridge does not support overall transaction
 */
private fun uploadBundleEntries(bundle: Bundle, queueItem: QueueItem, client: IGenericClient, parser: IParser) {
    var newPatientReference: Reference? = null //Update patient reference if server decides to change it
    for ((index, resource) in bundle.entry.map { it.resource }.withIndex()) {
        if (resource !is Questionnaire && resource !is Device && resource !is Organization) {
            try {
                printAndFlush("    $index: ${resource.meta.profile.joinToString(", ") { it.value }}")
                if (resource is Patient) {
                    resource.identifier.add(Identifier().apply {
                        system = COMPASS_SUBJECT_ID
                        value = queueItem.SubjectId
                    })
                    val outcome = client.create().resource(resource).conditional().where(
                        Patient.IDENTIFIER.exactly().systemAndIdentifier(COMPASS_SUBJECT_ID, queueItem.SubjectId)
                    ).execute()
                    print(" => " + outcome.id)
                    newPatientReference = Reference(outcome.id)
                } else {
                    changePatientId(resource, newPatientReference!!)
                    val outcome = client.create().resource(resource).execute()
                    print(" => " + outcome.id)
                }
                println(" DONE")
            } catch (e: Exception) {
                println()
                println("Cannot upload ${resource}: $e")
                println(parser.encodeResourceToString(resource))
                println()
            }
        }
    }
}

fun changePatientId(resource: Resource, newPatientId: Reference) {
    when (resource) {
        is Observation -> resource.subject = newPatientId
        is Condition -> resource.subject = newPatientId
        is Immunization -> resource.patient = newPatientId
        is Consent -> resource.patient = newPatientId
        is Procedure -> resource.subject = newPatientId
        is QuestionnaireResponse -> resource.subject = newPatientId
        is MedicationStatement -> resource.subject = newPatientId
        is DiagnosticReport -> resource.subject = newPatientId
        else -> error("Unknown resource type '${resource.resourceType}'. Cannot change subject/patient reference!")
    }

}

private fun printAndFlush(message: String) {
    print(message)
    System.out.flush()
}