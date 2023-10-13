import android.content.Context
import android.hardware.biometrics.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricPromptManager(private val context: Context) {

    private val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Biyometrik Kimlik Doğrulama")
        .setSubtitle("Parmak izi veya yüz tanıma ile giriş yapın")
        .setNegativeButtonText("Vazgeç")
        .build()

    private val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onSucceeded()
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            onFailed()
        }
    }

    fun startAuthentication() {

            if (isBiometricSupported()) {
                val biometricPrompt = BiometricPrompt(
                    context as FragmentActivity,
                    ContextCompat.getMainExecutor(context),
                    callback
                )
                biometricPrompt.authenticate(promptInfo)
            }

    }

    private fun onSucceeded() {
        // Doğrulama başarılı oldu, istediğiniz işlemleri burada gerçekleştirin.
    }

    private fun onFailed() {
        // Doğrulama başarısız oldu, gerekli işlemleri burada yapabilirsiniz.
    }

    private fun onBiometricNotAvailable() {
        // Cihaz biyometrik doğrulamayı desteklemiyor, uygun bir hata mesajı gösterin
    }

    fun isBiometricSupported(): Boolean {

        val biometricManager = context.getSystemService(BiometricManager::class.java)
        var result = when (biometricManager?.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->  false
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->  false
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> false
            else ->  false
        }
        return result
    }


}
