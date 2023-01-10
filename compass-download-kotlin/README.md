# compass-download-kotlin
This is a clone of [compass-numapp-downloader](https://github.com/NUMde/compass-numapp-downloader), written in Kotlin 
instead of Python. This is why the interface component does not depend on a Python installation.

## IMPORTANT HINT
You need to use **at least Java 8 Update 161, Java 9 or newer**, due to the fact that older versions cannot use AES with
more than
128 Bit due to legal reasons.

## Example usage
To load the certificate, public and private key files in the sample code below, put them under `src/main/resources`:
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

You can also use the downloader object to add or update questionnaires:

```kotlin
val questionnaire = Questionnaire().apply {
    url = "https://my-questionnaire-url.de/"
    version = "1.0"
    // [...]
}

val accessToken = downloader.retrieveAccessToken()

val parser = FhirContext.forR4().newJsonParser()
downloader.addQuestionnaire("myName", parser.encodeResourceToString(q), accessToken)
//or: downloader.updateQuestionnaire(...) if you want to update
val jsonString =
    downloader.retrieveQuestionnaireStringByUrlAndVersion("https://my-questionnaire-url.de/", "1.0", accessToken)

```
