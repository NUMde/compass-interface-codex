import java.io.File
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

/**
 * Utility class for parsing PEM (Privacy Enhanced Mail) files in Java/Kotlin
 */
object PemUtils {
    fun loadPrivateKey(privateKeyFile: File): PrivateKey = loadPrivateKey(privateKeyFile.readText())
    fun loadPrivateKey(privateKeyFile: String): PrivateKey {
        val privateKeyContent = privateKeyFile
            .replace("\n", "")
            .replace("\r", "")
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")

        val keySpecPKCS8 = PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent))
        return KeyFactory.getInstance("RSA").generatePrivate(keySpecPKCS8)
    }

    fun loadPublicKey(publicKeyFile: File): RSAPublicKey = loadPublicKey(publicKeyFile.readText())

    fun loadPublicKey(publicKey: String): RSAPublicKey {
        val publicKeyContent = publicKey.replace("\n", "").replace("\r", "")
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")

        val keySpecX509 = X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent))
        return KeyFactory.getInstance("RSA").generatePublic(keySpecX509) as RSAPublicKey
    }

    fun loadCertificate(certFile: File): X509Certificate {
        return CertificateFactory.getInstance("X.509").generateCertificate(certFile.inputStream()) as X509Certificate
    }

    fun loadCertificate(certFile: String): X509Certificate {
        return CertificateFactory.getInstance("X.509")
            .generateCertificate(certFile.byteInputStream()) as X509Certificate
    }

}