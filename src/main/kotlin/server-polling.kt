import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
import ca.uhn.fhir.rest.client.api.IGenericClient
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor
import kotlinx.coroutines.delay
import mu.KotlinLogging
import org.hl7.fhir.r4.model.*
import java.time.ZonedDateTime
import kotlin.time.Duration

private fun getEnv(varname: String): String = System.getenv(varname) ?: error("Please set env var '$varname'")
private fun sanitize(s: String): String = s.replace("\\n", "\n").removeSurrounding("'")
data class PollingArgs(
    val serverUrl: String = getEnv("COMPASS_BACKEND_URL"),
    val apiId: String = getEnv("COMPASS_API_ID"),
    val apiKey: String = getEnv("COMPASS_API_KEY"),

    val privateKey: String = sanitize(getEnv("PRIVATE_KEY")),
    val publicKey: String = sanitize(getEnv("PUBLIC_KEY")),
    val certificate: String = sanitize(getEnv("CERTIFICATE")),

    val fhirmapperUrl: String = getEnv("FHIRSERVER"),
    val fhirAuthUser: String? = System.getenv("FHIR_USER"),
    val fhirAuthPassword: String? = System.getenv("FHIR_PASSWORD"),

    val pollingDuration: Duration = Duration.parse(System.getenv("POLLING") ?: "10m")
)


private val log = KotlinLogging.logger {}

suspend fun main() {
    val args = PollingArgs()

    val ctx = FhirContext.forR4()
    val parser = ctx.newJsonParser()
    val client = ctx.newRestfulGenericClient(args.fhirmapperUrl).apply {
        if (args.fhirAuthUser != null) {
            registerInterceptor(BasicAuthInterceptor(args.fhirAuthUser, args.fhirAuthPassword))
        }
    }


    val downloader = CompassDownloader(
        serverUrl = args.serverUrl,
        apiID = args.apiId,
        apiKey = args.apiKey,
        publicKey = PemUtils.loadPublicKey(args.publicKey),
        privateKey = PemUtils.loadPrivateKey(args.privateKey),
        cert = PemUtils.loadCert(args.certificate)
    )

    val questionnaireCache = mutableMapOf<String, Questionnaire>()
    val pollingDuration = args.pollingDuration

    while (true) {
        pollingAction(downloader, questionnaireCache, args.fhirmapperUrl, client, parser)
        println("${ZonedDateTime.now()} Waiting for $pollingDuration")
        delay(pollingDuration)
    }


}

private suspend fun pollingAction(
    downloader: CompassDownloader,
    cache: MutableMap<String, Questionnaire>,
    fhirServerUrl: String,
    client: IGenericClient,
    parser: IParser
) {
    suspend fun retrieveQuestionnaire(qr: QuestionnaireResponse): Questionnaire {
        val canonical = qr.questionnaire
        if (!cache.containsKey(canonical)) {
            val url = canonical.substringBeforeLast("|")
            val version = canonical.substringAfterLast("|")
            log.info { "Retrieving corresponding Questionnaire '${qr.questionnaire}' from server" }
            val questionnaireJson = downloader.retrieveQuestionnaireStringByUrlAndVersion(
                url, version, downloader.retrieveAccessToken()
            )
            println("questionnaireJson = " + questionnaireJson)
            val questionnaire = parser.parseResource(questionnaireJson) as Questionnaire
            cache[canonical] = questionnaire
            log.info { "Retrieving Questionnaire '${qr.questionnaire}' successful" }
        }
        return cache[canonical]!!
    }


    val queueItems = downloader.retrieveAllQueueItems() //TODO: Only retrieve new values, do this regulary
    if (queueItems.isEmpty()) {
        log.info { "No new queue items!" }
        println("No new queue items!")
    }
    for (queueItem in queueItems) {
        try {
            val decryptedQueueItem = downloader.decryptQueueItem(queueItem)

            val qrString = decryptedQueueItem.data.bodyAsString
            val qr = parser.parseResource(qrString) as QuestionnaireResponse

            val questionnaire = retrieveQuestionnaire(qr)

            addExtensions(qr, questionnaire)

            val logicalModel = toLogicalModel(questionnaire, qr)
            val bundleBuilder = TransactionBundleBuilder()
            val bundle = logicalModelToGeccoProfile(
                logicalModel,
                IdType(queueItem.SubjectId).withServerBase(fhirServerUrl, "Patient"),
                DateTimeType(queueItem.AbsendeDatum),
                bundleBuilder
            )
            bundle.id = queueItem.UUID
            bundle.identifier.value = queueItem.UUID

            uploadBundleEntries(bundle, queueItem, client, parser)
        } catch (e: Exception) {
            log.error(e) { "Cannot upload $queueItem" }
            println(e)
            e.printStackTrace()
        }
    }
    val accessToken = downloader.retrieveAccessToken()
    downloader.markCTransferListIdsAsDownloaded(queueItems.map { it.UUID }, accessToken)
}

private fun uploadBundleEntries(
    bundle: Bundle,
    queueItem: QueueItem,
    client: IGenericClient,
    parser: IParser,
) {
    var newPatientReference: Reference? = null //Update patient reference if server decides to change it TODO: Test it
    for (resource in bundle.entry.map { it.resource }) {
        if (resource !is Questionnaire && resource !is Device && resource !is Organization) {
            println("uploading $resource")
            try {
                if (resource is Patient) {
                    val existingPatientBundle = client.search<Bundle>().forResource(Patient::class.java).where(
                        Patient.IDENTIFIER.exactly().systemAndIdentifier(COMPASS_SUBJECT_ID, queueItem.SubjectId)
                    ).execute()
                    if (!existingPatientBundle.hasEntry()) {
                        resource.identifier = listOf(Identifier().apply {
                            system = COMPASS_SUBJECT_ID
                            value = queueItem.SubjectId
                        })
                        val outcome = client.create().resource(resource)
//                        .conditional().where(Patient.IDENTIFIER.exactly().systemAndIdentifier(COMPASS_SUBJECT_ID, queueItem.SubjectId))
                            .execute()
                        newPatientReference = Reference(outcome.id)
                    } else {
                        newPatientReference = Reference(existingPatientBundle.entry.first().resource)
                    }
                    log.info { "Patient reference for '${queueItem.SubjectId}' is '$newPatientReference'" }

                } else {
                    changePatientId(resource, newPatientReference!!)
                    val outcome = client.create().resource(resource).execute()
                }
            } catch (e: Exception) {
                log.error(e) {
                    "Cannot upload $resource (queueItem ${queueItem.UUID}): ${parser.encodeResourceToString(resource)}"
                }
                e.printStackTrace()
            }
        }
    }
}




