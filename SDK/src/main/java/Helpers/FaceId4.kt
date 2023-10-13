package Helpers

import android.content.Context
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.hardware.biometrics.BiometricPrompt.AuthenticationCallback
import android.os.CancellationSignal
import android.widget.Toast

class BiometricAuthentication(context: Context) {
    private val biometricManager: BiometricManager = context.getSystemService(BiometricManager::class.java)
    private val biometricPrompt: BiometricPrompt
    private  var  cancellationSignal: CancellationSignal

    private lateinit var callback : AuthenticationCallback

    init {
        cancellationSignal= CancellationSignal()
         callback = object : AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                // Biyometrik doğrulama başarılı oldu
               Toast.makeText(context,"başarılı",Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // Biyometrik doğrulama hatası oluştu
                Toast.makeText(context,"error",Toast.LENGTH_SHORT).show()
            }
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(context,"failed",Toast.LENGTH_SHORT).show()
            }
        }
        biometricPrompt = BiometricPrompt.Builder(context)
            .setTitle("Biyometrik Doğrulama")
            .setSubtitle("Yüz Tanıma")
            .setDescription("Lütfen yüzünüzü doğrulayın")
            .setNegativeButton("Vazgeç", context.mainExecutor, { _, _ -> })
            .build()

      //  biometricPrompt.authenticate(cancellationSignal, context.mainExecutor, callback)
    }

    fun startAuthentication(context:Context) {
        if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
            biometricPrompt.authenticate(cancellationSignal, context.mainExecutor,callback   )
        }
    }

    fun stopAuthentication() {
        cancellationSignal.cancel()
    }
}
