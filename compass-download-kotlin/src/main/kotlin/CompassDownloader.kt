import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import mu.KotlinLogging
import org.bouncycastle.cms.*
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient
import org.bouncycastle.cms.jcajce.JceKeyTransRecipient
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.*
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object PemUtils {
    fun loadPrivateKey(privateKeyFile: File): PrivateKey {
        val privateKeyContent = privateKeyFile.readText().replace("\n", "").replace("\r", "")
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")

        val kf = KeyFactory.getInstance("RSA")
        val keySpecPKCS8 = PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent))
        return kf.generatePrivate(keySpecPKCS8)
    }

    fun loadPublicKey(publicKeyFile: File): RSAPublicKey {
        val publicKeyContent = publicKeyFile.readText().replace("\n", "").replace("\r", "")
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")

        val kf = KeyFactory.getInstance("RSA")
        val keySpecX509 = X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent))
        return kf.generatePublic(keySpecX509) as RSAPublicKey
    }

    fun loadCert(certFile: File): X509Certificate {
        return CertificateFactory.getInstance("X.509").generateCertificate(certFile.inputStream()) as X509Certificate
    }

}


class CompassDownloader(
    private val serverUrl: String = "http://127.0.0.1:8080/",
    private val apiID: String = "test",
    private val apiKey: String = "gKdKLYG2g0-Y1EllI0-W",
    private val privateKey: PrivateKey,
    private val publicKey: RSAPublicKey,
    private val cert: X509Certificate,
) {

    private val log = KotlinLogging.logger {}

    private val client: HttpClient = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(Json {
                prettyPrint = true
                isLenient = true
            })
        }
    }


    init {
        Security.addProvider(BouncyCastleProvider())
    }


    class EncryptionResult(val cipherText: ByteArray, val key: SecretKey, val iv: ByteArray)


    private fun aesEncrypt(plaintext: ByteArray): EncryptionResult {
        val keygen = KeyGenerator.getInstance("AES")
        keygen.init(256)
        val key: SecretKey = keygen.generateKey()
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val ciphertext: ByteArray = cipher.doFinal(plaintext)
        return EncryptionResult(ciphertext, key, cipher.iv)
    }

    private fun aesDecrypt(cypherText: ByteArray, key: ByteArray, iv: ByteArray): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        val ivParameterSpec = IvParameterSpec(iv)
        val secretKeySpec = SecretKeySpec(key, "AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
        return String(cipher.doFinal(cypherText))
    }

    private fun rsaEncrypt(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, this.publicKey)
        return cipher.doFinal(data)
    }

    private fun rsaDecrypt(cypherText: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, this.privateKey)
        return cipher.doFinal(cypherText)
    }


    @Serializable
    data class CredentialsPayload(val ApiID: String, val ApiKey: String, val CurrentDate: String)

    @Serializable
    data class AuthBody(val encrypted_creds: String, val encrypted_key: String, val iv: String)

    @Serializable
    data class TokenResponse(val access_token: String)


    suspend fun retrieveAccessToken(): String {
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss")
        val currentDate = LocalDateTime.now().format(formatter)

        val creds = Json.encodeToString(CredentialsPayload(apiID, apiKey, currentDate))
        log.trace { "retrieveAccessToken(): Secret Message: $creds " }
        val aesEncrypted = aesEncrypt(creds.toByteArray(StandardCharsets.UTF_8))
        val encoder = Base64.getEncoder()
        val encodedCipherText = String(encoder.encode(aesEncrypted.cipherText), StandardCharsets.UTF_8)
        val encodedKey = String(encoder.encode(aesEncrypted.key.encoded), StandardCharsets.UTF_8)
        val encodedIv = String(encoder.encode(aesEncrypted.iv), StandardCharsets.UTF_8)
        log.trace { "retrieveAccessToken(): Encrypted ciphertext = $encodedCipherText  key = $encodedKey  iv = $encodedIv" }

        val rsaEncryptedKey = rsaEncrypt(aesEncrypted.key.encoded)
        val base64RsaKeyString = String(encoder.encode(rsaEncryptedKey), StandardCharsets.UTF_8)
        log.trace { "retrieveAccessToken(): RSA-encrypted and base64-encoded key: $base64RsaKeyString" }

        val response = client.post<TokenResponse> {
            contentType(ContentType.Application.Json)
            url("${serverUrl.removeSuffix("/")}/api/auth/")
            body = AuthBody(encodedCipherText, base64RsaKeyString, encodedIv)
        }
        log.debug { "retrieveAccessToken(): retrieved access token: ${response.access_token}" }
        return response.access_token
    }

    @Serializable
    data class QueuePageResponse(
        val totalEntries: Int,
        val totalPages: Int,
        val currentPage: Int,
        val cTransferList: String
    )

    suspend fun loadQueueItemsByPage(page: Int, access_token: String): QueuePageResponse {
        val pageResponse = client.get<QueuePageResponse> {
            url("${serverUrl.removeSuffix("/")}/api/download")
            parameter("page", page)
            header("Authorization", "Bearer $access_token")
        }
        return pageResponse
    }

    suspend fun retrieveQuestionnaireStringByUrlAndVersion(url: String, version: String, access_token: String): String {
        return client.get<JsonObject> {
            url("${serverUrl.removeSuffix("/")}/api/questionnaire")
            parameter("url", url)
            parameter("version", version)
            header("Authorization", "Bearer $access_token")
        }["body"].toString()
    }


    suspend fun deleteQueueItemsByUuid(uuids: List<String>, access_token: String): Boolean {
        val pageResponse = client.delete<HttpResponse> {
            url("${serverUrl.removeSuffix("/")}/api/download")
            header("Authorization", "Bearer $access_token")
            contentType(ContentType.Application.Json)
            body = uuids
        }
        return pageResponse.status == HttpStatusCode.OK
    }


    suspend fun addQuestionnaire(name: String, questionnaire: String, access_token: String): Boolean {
        val questionnaireJson = Json.parseToJsonElement(questionnaire) as JsonObject

        val pageResponse = client.post<HttpResponse> {
            url("${serverUrl.removeSuffix("/")}/api/questionnaire")
            header("Authorization", "Bearer $access_token")
            contentType(ContentType.Application.Json)
            body = buildJsonObject {
                put("name", name)
                put("url", questionnaireJson["url"]!!.jsonPrimitive.content)
                put("version", questionnaireJson["version"]!!.jsonPrimitive.content)
                put("questionnaire", questionnaireJson)
            }
        }
        return pageResponse.status == HttpStatusCode.OK
    }

    suspend fun updateQuestionnaire(name: String, questionnaire: String, access_token: String): Boolean {
        val questionnaireJson = Json.parseToJsonElement(questionnaire) as JsonObject

        val pageResponse = client.put<HttpResponse> {
            url("${serverUrl.removeSuffix("/")}/api/questionnaire")
            header("Authorization", "Bearer $access_token")
            contentType(ContentType.Application.Json)
            body = buildJsonObject {
                put("name", name)
                put("url", questionnaireJson["url"]!!.jsonPrimitive.content)
                put("version", questionnaireJson["version"]!!.jsonPrimitive.content)
                put("questionnaire", questionnaireJson)
            }
        }
        return pageResponse.status == HttpStatusCode.OK
    }


    fun verifyJWTAndDecode(jwt: String): String {
        val charset = StandardCharsets.UTF_8
        val parts = jwt.split('.')
        val header = parts[0].toByteArray(charset)
        val payload = parts[1].toByteArray(charset)
        val tokenSignature = Base64.getUrlDecoder().decode(parts[2])

        //TODO: Verify header is always {"alg":"RS256"}
        val rsaSignature = Signature.getInstance("SHA256withRSA")
        rsaSignature.initVerify(this.publicKey)
        rsaSignature.update(header)
        rsaSignature.update('.'.code.toByte())
        rsaSignature.update(payload)
        if (!rsaSignature.verify(tokenSignature)) {
            throw SignatureException("JWT cannot be verified!")
        }
        return Base64.getDecoder().decode(payload).toString(StandardCharsets.UTF_8)
    }


    fun encryptPkcs7CMS(data: ByteArray?): ByteArray? {
        val jceKey = JceKeyTransRecipientInfoGenerator(this.cert)

        val cmsEnvelopedDataGenerator = CMSEnvelopedDataGenerator()
        cmsEnvelopedDataGenerator.addRecipientInfoGenerator(jceKey)

        val msg: CMSTypedData = CMSProcessableByteArray(data)
        val encryptor = JceCMSContentEncryptorBuilder(CMSAlgorithm.AES128_CBC).setProvider("BC").build()

        return cmsEnvelopedDataGenerator.generate(msg, encryptor).encoded
    }

    fun decryptPkcs7CMS(encryptedData: ByteArray): ByteArray {
        val envelopedData = CMSEnvelopedData(encryptedData)
        val recipients = envelopedData.recipientInfos.recipients
        val recipientInfo = recipients.iterator().next() as KeyTransRecipientInformation
        val recipient: JceKeyTransRecipient = JceKeyTransEnvelopedRecipient(this.privateKey)
        return recipientInfo.getContent(recipient)
    }

    @Serializable
    data class QueueItem(
        val UUID: String,
        val SubjectId: String,
        val JSON: String,
        val AbsendeDatum: String,
        val ErhaltenDatum: String
    )

    suspend fun retrieveAllQueueItems(): MutableList<QueueItem> {
        val accessToken = retrieveAccessToken()
        val response = loadQueueItemsByPage(1, accessToken)
        val jsonPayload: String = verifyJWTAndDecode(response.cTransferList)

        val list = mutableListOf<QueueItem>()
        list.addAll(Json.decodeFromString(jsonPayload))

        for (i in response.totalPages + 1..response.totalPages) {
            val response = loadQueueItemsByPage(i, accessToken)
            val jsonPayload = verifyJWTAndDecode(response.cTransferList)
            list.addAll(Json.decodeFromString(jsonPayload))
        }
        return list
    }

    fun decryptQueueItems(queueItems: List<QueueItem>): List<Pair<QueueItem, DecryptedQueueItem>> {
        val questionnaires = ArrayList<Pair<QueueItem, DecryptedQueueItem>>(queueItems.size)
        for (item in queueItems) {
            try {
                questionnaires.add(item to decryptQueueItem(item))
            } catch (exception: Exception) {
                log.error { "Cannot decrypt ${item.UUID}: ${exception.message}" }
            }
        }
        return questionnaires
    }

    fun decryptQueueItem(item: QueueItem): DecryptedQueueItem =
        Json.decodeFromString(decryptQueueItemToString(item))

    fun decryptQueueItemToString(item: QueueItem): String {
        val encryptedBytes = Base64.getDecoder().decode(item.JSON)
        val decryptedBytes = decryptPkcs7CMS(encryptedBytes)
        return decryptedBytes.decodeToString()
    }

    @Serializable
    data class DecryptedQueueItem(
        val type: String, //"questionnaire_response"
        val data: DecryptedQueueItemData
    )

    @Serializable
    data class DecryptedQueueItemData(
        val subjectId: String,
        val body: JsonElement
    ) {
        val bodyAsString = when (body) {
            is JsonPrimitive -> body.content // IBM: String
            else -> body.toString() // Data4Life: JSON-Object
        }
    }

}