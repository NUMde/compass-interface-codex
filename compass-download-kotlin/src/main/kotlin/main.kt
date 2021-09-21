import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import java.nio.file.Paths

suspend fun main(args: Array<String>) {
    val privateKeyFile = Paths.get(ClassLoader.getSystemResource("private_key.pem").toURI()).toFile()
    val publicKeyFile = Paths.get(ClassLoader.getSystemResource("public_key.pem").toURI()).toFile()
    val certFile = Paths.get(ClassLoader.getSystemResource("cacert.pem").toURI()).toFile()
    val downloader = CompassDownloader(
        publicKey = PemUtils.loadPublicKey(publicKeyFile),
        privateKey = PemUtils.loadPrivateKey(privateKeyFile),
        cert = PemUtils.loadCert(certFile)
    )
    val queueItems = downloader.retrieveAllQueueItems()
    for (queueItem in queueItems) {
        try {
            val decryptedQueueItem = downloader.decryptQueueItem(queueItem)
            val qr = decryptedQueueItem.data.bodyAsString
            println(qr)
        } catch (e: Exception) {
            println("Could not decrypt ${queueItem.UUID}: $e")
        }
    }
}