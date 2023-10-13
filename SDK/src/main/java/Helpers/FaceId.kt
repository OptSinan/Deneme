import Helpers.SecureDataManager
import android.app.KeyguardManager
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.CryptoObject
import androidx.fragment.app.FragmentActivity
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class FaceId(private val context: Context) {
    companion object {
        const val KEY_ALIAS = "my_secret_key"
        const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    }

    private val promptInfo: BiometricPrompt.PromptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Biometric login for my app")
        .setSubtitle("Log in using your biometric credential")
        .setNegativeButtonText("İptal")
        .build()

    fun authenticateWithFaceId(fragmentActivity: FragmentActivity) {
        if (isDeviceSecure()) {
            val cipher = getCipherForBiometrics()
            if (cipher != null) {
                val biometricPrompt = BiometricPrompt(fragmentActivity, context.mainExecutor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            enterApplication()
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                        }

                        override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence
                        ) {
                            super.onAuthenticationError(errorCode, errString)

                            Toast.makeText(
                                context,
                                "  kod ${errorCode.toString()} ERROr  ${errString.toString()}    ",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    })

                biometricPrompt.authenticate(promptInfo, CryptoObject(cipher))
            }
        } else {
            // Cihaz güvende değil, gerektiği şekilde ele alın.
        }
    }

    private fun getCipherForBiometrics(): Cipher? {
        return try {
            val cipher = Cipher.getInstance(SecureDataManager.TRANSFORMATION)

            val key = generateAndStoreSecretKeyIfNotExists("alias")

            cipher.init(Cipher.ENCRYPT_MODE, key)
            cipher
        } catch (e: KeyPermanentlyInvalidatedException) {
            null
        } catch (e: Exception) {
            throw RuntimeException("Failed to init Cipher ${e.toString()} ", e)
        }
    }

    private fun isDeviceSecure(): Boolean {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return keyguardManager.isDeviceSecure
    }


    private fun generateAndStoreSecretKeyIfNotExists(alias: String): SecretKey {
        // Anahtarın zaten var olup olmadığını kontrol et
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        if (keyStore.containsAlias(alias)) {
            // Anahtar zaten varsa, mevcut anahtarı döndür
            val keyEntry = keyStore.getEntry(alias, null) as KeyStore.SecretKeyEntry
            return keyEntry.secretKey
        } else {
            // Anahtar yoksa yeni bir anahtar üret
            val keyGenerator =
                KeyGenerator.getInstance(SecureDataManager.ALGORITHM, "AndroidKeyStore")
            val builder = KeyGenParameterSpec.Builder(
                alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).setBlockModes(SecureDataManager.BLOCK_MODE)
                .setEncryptionPaddings(SecureDataManager.PADDING)

            keyGenerator.init(builder.build())
            val secretKey: SecretKey = keyGenerator.generateKey()

            return secretKey
        }
    }

    private fun enterApplication() {
        // Biyometrik kimlik doğrulama başarılı, uygulamanıza devam edin.
    }


}
