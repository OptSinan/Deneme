package Helpers

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentActivity
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Date
import java.util.concurrent.Executor


internal class SdkHelpers {


    private val authenticators =
        BiometricManager.Authenticators.BIOMETRIC_WEAK // or BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL

    // Biyometrik doğrulama işlemini gerçekleştirme işlevi
    fun performBiometricAuthentication(
        permissionRequestMessage: String, context: Context, callback: (Boolean) -> Unit
    ) {

        if (checkBiometricSupport(context)) {

            lateinit var biometricPrompt: BiometricPrompt
            val executor: Executor = ContextCompat.getMainExecutor(context)
            val promptInfo = BiometricPrompt.PromptInfo.Builder().apply {
                setTitle("Biyometrik Kimlik Doğrulama")
                setSubtitle(permissionRequestMessage)
                setNegativeButtonText("İptal")
                setConfirmationRequired(true)
                setAllowedAuthenticators(authenticators)
            }.build()

            val authenticationCallback = object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)

                    Toast.makeText(
                        context,
                        "  CODE ${errorCode} ERROR  ${errString}    ",
                        Toast.LENGTH_SHORT
                    ).show()
                    biometricPrompt.cancelAuthentication()
                    // Kimlik doğrulama hatası
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)

                    Toast.makeText(
                        context, "Biyometrik doğrulama başarılı oldu.", Toast.LENGTH_SHORT
                    ).show()
                    callback(true)
                    biometricPrompt.cancelAuthentication()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()

                    Toast.makeText(
                        context, "Biyometrik doğrulama başarısız oldu.", Toast.LENGTH_SHORT
                    ).show()
                    callback(false)
                    biometricPrompt.cancelAuthentication()
                }
            }
            biometricPrompt = BiometricPrompt(context as FragmentActivity, executor, authenticationCallback)
            biometricPrompt.authenticate(promptInfo)

        }
    }


    // Biyometrik doğrulama destekliyor mu ? kontrol  işlevi
    fun checkBiometricSupport(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)

        val result = when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true // Cihaz biyometrik kimlik doğrulamayı destekliyor
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                //Parmak izi eklemeye veya yüz izi ekleme  sayfasına yönlendiren kod
                var enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(
                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        authenticators
                    )
                }
                startActivity(context, enrollIntent, null)
                Toast.makeText(
                    context,
                    "Lütfen biyometrik iz ekleyiniz.",
                    Toast.LENGTH_SHORT
                ).show()
                false
            }  // Hiçbir biyometrik kimlik verisi kaydedilmemiş
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> false // Cihazda biyometrik donanım yok
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> false // Biyometrik donanım kullanılamıyor

            else -> false
        }

        return result
    }

    fun generatePinCode(customerNo: Int, secret: String, time: Double): String {
        val seconds = (Date().time / 1000).toDouble()
        val counter = (seconds / time).toInt()
        val hashStr = "$customerNo$secret$counter"
        val hashBytes = hashStr.toByteArray(StandardCharsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-1")
        val hashedBytes = digest.digest(hashBytes)
        val offset = hashedBytes[19].toInt() and 0xf
        val truncatedHash =
            (hashedBytes[offset].toInt() and 0x7f shl 24) or (hashedBytes[offset + 1].toInt() and 0xff shl 16) or (hashedBytes[offset + 2].toInt() and 0xff shl 8) or (hashedBytes[offset + 3].toInt() and 0xff)
        val otp = (truncatedHash % 1e6).toInt()
        val strOtp = String.format("%06d", otp)
        return strOtp
    }
}