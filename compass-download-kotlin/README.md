# compass-dlkot

## IMPORTANT HINT
You need to use **at least Java 8 Update 161, Java 9 or newer**, due to the fact that older versionen cannot use AES with more than 
128 Bit due to legal reasons.

## Example
To load the certificate, public and private key files in the sample code below, put them under `src/main/resources`
```kotlin
suspend fun main(args: Array<String>) {
    val privateKeyFile = Paths.get(ClassLoader.getSystemResource("private_key.pem").toURI()).toFile()
    val publicKeyFile = Paths.get(ClassLoader.getSystemResource("public_key.pem").toURI()).toFile()
    val certFile = Paths.get(ClassLoader.getSystemResource("cacert.pem").toURI()).toFile()
    val downloader = CompassDownloader(
        publicKey = PemUtils.loadPublicKey(publicKeyFile),
        privateKey = PemUtils.loadPrivateKey(privateKeyFile),
        cert = PemUtils.loadCert(certFile)
    )
    val decryptQueueItems = downloader.decryptQueueItems(downloader.getAllQueueItems())
    println(decryptQueueItems)
}
```
