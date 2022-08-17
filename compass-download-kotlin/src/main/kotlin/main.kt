import java.nio.file.Paths

suspend fun main(args: Array<String>) {
    val privateKeyFile = Paths.get(ClassLoader.getSystemResource("private_key.pem").toURI()).toFile()
    val publicKeyFile = Paths.get(ClassLoader.getSystemResource("public_key.pem").toURI()).toFile()
    val certFile = Paths.get(ClassLoader.getSystemResource("cacert.pem").toURI()).toFile()
    val downloader = CompassDownloader(
        serverUrl = "http://127.0.0.1:8080/",
        apiID = "test",
        apiKey = "gKdKLYG2g0-Y1EllI0-W",
        publicKey = PemUtils.loadPublicKey(publicKeyFile),
        privateKey = PemUtils.loadPrivateKey(privateKeyFile),
        cert = PemUtils.loadCert(certFile)
    )
    val queueItems = downloader.retrieveAllQueueItems()
    var foo = listOf<String>()
    for (queueItem in queueItems) {
        try {

            val decryptedQueueItem = downloader.decryptQueueItem(queueItem)
            val qr = decryptedQueueItem.data.bodyAsString
            foo += queueItem.UUID
            println(qr)
        } catch (e: Exception) {
            println("Could not decrypt ${queueItem.UUID}: $e")
        }
    }

    val accessToken = downloader.retrieveAccessToken()
    println(foo)
    downloader.markCTransferListIdsAsDownloaded(foo, accessToken)

}