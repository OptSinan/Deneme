// bu kullanılacak
import Data.AccountListRequest
import Data.BeginActivationRequest
import Data.CheckDateRequest
import Data.CheckDateResponse
import Data.CompleteActivationRequest
import Data.GetPinCodeRequest
import Helpers.SdkHelpers
import Helpers.SecureDataManager
import Services.Network.ServiceManager
import androidx.fragment.app.FragmentActivity
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class OptimusPassSDK private constructor() {

    companion object {
        var sharedInstance = OptimusPassSDK()
    }

    private var apiURL: String = ""
    private var initCompleted: Boolean = false
    private var isAuthenticated: Boolean = false
    private var username: String = ""
    private var password: String = ""
    private var identityNo: String = ""
    private var phoneNumber: String = ""
    private var deviceType: String = "A"
    private var deviceId: String = ""
    private var mobileAppVersion: String? = ""
    lateinit var serviceManager: ServiceManager

    private var expirationMessage =
        "Her hesap sadece bir cihaz ile eşleştirilebilir. Bu hesap başka bir cihaz ile eşleştirildi yada aktif değil. Yeniden tanımlama yapılması gerekmektedir."

    fun init(apiURL: String): OptPassResult {
        try {
            // API URL'nin geçerli bir URI olup olmadığını kontrol eder.
            val uri = URI(apiURL)
            if (uri.scheme == null || uri.host == null) {
                return OptPassResult(false, "apiURL formatı geçerli değil!")
            }

            this.apiURL = apiURL
            this.initCompleted = true

            serviceManager = ServiceManager(this.apiURL)

            return OptPassResult(true, null)
        } catch (e: Exception) {
            return OptPassResult(false, "apiURL formatı geçerli değil!")
        }
    }

    suspend fun authenticate(
        permissionRequestMessage: String,
        context: FragmentActivity,
    ): OptPassResult {

        if (!initCompleted) {
            throw InitNotCompletedException("Bu API çağrısından önce init fonksiyonu çalıştırılmalıdır!")
        }

        isAuthenticated = suspendCoroutine<Boolean> { continuation ->
            SdkHelpers().performBiometricAuthentication(
                permissionRequestMessage, context
            ) { result ->
                continuation.resume(result)
            }
        }

        return if (isAuthenticated) {
            OptPassResult(success = true, errorMessage = null)
        } else {
            OptPassResult(success = false, errorMessage = "Biyometrik doğrulama işlemi başarısız!")
        }
    }

    suspend fun checkDateSkew(): SkewResult {
        if (isAuthenticated) { //düzelt
            throw NotAuthenticatedException("Biyometrik doğrulama yapılmadı! İşleme devam edebilmek için authenticate fonksiyonu çalıştırılmalıdır!")
        }

        val currentDate = Date()
        // ISO 8601 formatında bir zaman dizesine dönüştürün
        val iso8601Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        iso8601Format.timeZone = TimeZone.getTimeZone("UTC")
        val phoneDate = iso8601Format.format(currentDate)

        lateinit var skewResult: SkewResult

        // APİ  http://metro.optimusyazilim.com.tr:9009/

        var response = suspendCoroutine<CheckDateResponse?> { continuation ->
            ServiceManager("http://metro.optimusyazilim.com.tr:9009").handleChechDate(
                CheckDateRequest(phoneDate)
            ) { it, errorMessage ->

                if (it != null) {
                    val totalSeconds = it.data.totalSeconds

                    println("sinan" + it.data.totalSeconds)
                    skewResult = SkewResult(true, null, totalSeconds)
                } else {
                    skewResult = SkewResult(
                        false,
                        "Cihaz saati doğrulanamadı! Bu işlem geçersiz giriş kodu üretilmesine sebep olabilir.",
                        0.0
                    )
                }
                continuation.resume(it)
            }
        }
        return skewResult
    }

    suspend fun beginActivation(
        username: String,
        password: String,
        identityNo: String,
        phoneNumber: String,
        deviceId: String,
        mobileAppVersion: String?
    ): OptPassResult {
        // isAuthenticated kontrolü
        if (isAuthenticated) {   //  ünlemi kaldırdın kontrol et
            throw NotAuthenticatedException("Kullanıcı oturumu açık değil.")
        }

        // Kullanıcıdan gelen verileri sakla
        this.username = username
        this.password = password
        this.identityNo = identityNo
        this.phoneNumber = phoneNumber
        this.deviceId = deviceId
        this.mobileAppVersion = mobileAppVersion
        lateinit var optPassResult: OptPassResult

        var response = suspendCoroutine<OptPassResult?> { continuation ->
            ServiceManager("http://metro.optimusyazilim.com.tr:9009").handleBeginActivation(
                BeginActivationRequest(
                    this.username,
                    this.password,
                    this.identityNo,
                    this.phoneNumber,
                    this.deviceId,
                    this.deviceType,
                    this.mobileAppVersion
                )
            ) { it, errorMessage ->

                if (it != null) {

                    if (it.success) {
                        optPassResult = OptPassResult(true, null)
                    } else {
                        optPassResult = OptPassResult(false, it.message)
                    }
                } else {
                    optPassResult = OptPassResult(false, errorMessage)
                }

                continuation.resume(optPassResult)
            }

        }
        return optPassResult
    }

    suspend fun completeActivation(context: FragmentActivity, smsCode: String): OptPassResult {
        // isAuthenticated kontrolü
        if (isAuthenticated) {  // ünlemi kaldırdını onu kontrol et
            throw NotAuthenticatedException("Kullanıcı kimlik doğrulaması yapılmamış.")
        }

        lateinit var optPassResult: OptPassResult

        var response = suspendCoroutine<OptPassResult?> { continuation ->
            ServiceManager("http://metro.optimusyazilim.com.tr:9009").handleCompleteActivation(
                CompleteActivationRequest(
                    this.username,
                    this.password,
                    this.identityNo,
                    this.phoneNumber,
                    smsCode,
                    this.deviceId,
                    this.deviceType,
                    this.mobileAppVersion
                )
            ) { it, errorMessage ->

                if (it != null) {
                    if (it.success) {
                        SecureDataManager(context).encryptCustomerData(
                            this.username,
                            it.data.title,
                            it.data.secretKey,
                            it.data.creationDate,
                        )
                        optPassResult = OptPassResult(true, null)
                    } else {
                        optPassResult = OptPassResult(false, it.message)
                    }
                } else {
                    optPassResult = OptPassResult(false, errorMessage)
                }
                continuation.resume(optPassResult)
            }
        }
        return optPassResult
    }

    // isExpired=true olduğunda expirationMessage parametresi de sabit bir değer taşır. Mesaj
//değişikliği istenirse SDK’yı implemente eden UI projesinde yapılır.      ---------------------->>> bu kısmı en son yap   bunun için parametre eklenmeli
    suspend fun accountList(context: FragmentActivity): AccountListResult {
        if (isAuthenticated) {   // BUNU !   DEĞİŞTİRDİM SONRA DÜZELT
            throw NotAuthenticatedException("Kullanıcı kimlik doğrulaması yapılmamış.")
        }

        var accountListResult: AccountListResult
        val localAccountList = SecureDataManager(context).getAccountlist()
        val payload = localAccountList.joinToString(";") { "${it.customerNo}:${it.creationDate}" }

        val response = suspendCoroutine<AccountListResult> { continuation ->
            ServiceManager("http://metro.optimusyazilim.com.tr:9009").handleAccountList(
                AccountListRequest(payload)
            ) { it, errorMessage ->
                if (it != null) {
                    // tip dönüşümlerini hepsini kontrol et
                    if (it.success) {

                        var accountList = mutableListOf<AccountListItem>()
                        val r1List = it?.data?.R1

                        if (localAccountList.isNotEmpty()) {
                            for (localItem in localAccountList) {
                                val matchingCustomerItem =
                                    r1List?.find { it.MUSTERI_NO == localItem.customerNo.toInt() }

                                val accountListItem = if (matchingCustomerItem != null) {
                                    // Eşleşme var, AccountListItem oluştur ve değerleri doldur
                                    AccountListItem(
                                        customerNo = localItem.customerNo.toInt(),
                                        title = localItem.title ?: "",
                                        isExpired = true,
                                        expirationMessage = expirationMessage
                                    )

                                } else {

                                    AccountListItem(
                                        customerNo = localItem.customerNo.toInt(),
                                        title = localItem.title ?: "",
                                        isExpired = false,
                                        expirationMessage = null
                                    )

                                }
                                accountList.add(accountListItem)
                            }
                            accountListResult = AccountListResult(
                                true, null, accountList
                            )
                        } else {
                            accountListResult = AccountListResult(
                                true, null, emptyList()
                            )
                        }
                    } else {
                        accountListResult = AccountListResult(
                            false, it.message, emptyList()
                        )
                    }
                } else {
                    accountListResult = AccountListResult(
                        false, errorMessage, emptyList()
                    )
                }
                continuation.resume(accountListResult)
            }
        }
        println("payload    =   " + payload)
        println("SDK İÇİ Local Account LİST" + localAccountList.toString())
        return response
    }

    suspend fun getPinCode(customerNo: Int, context: FragmentActivity): GetPinSDKResponse? {

        if (isAuthenticated) {   // BUNU !   DEĞİŞTİRDİM SONRA DÜZELT
            throw NotAuthenticatedException("Kullanıcı kimlik doğrulaması yapılmamış.")
        }

        val localAccountSecretKey =
            SecureDataManager(context).getCustomerSecretKey(customerNo.toString())   //string donusumunde sıkıntı cıkabilir kontrol ekle

        var pinSDKResponse: GetPinSDKResponse

        var response = suspendCoroutine<GetPinSDKResponse> { continuation ->
            ServiceManager("http://metro.optimusyazilim.com.tr:9009").handleGetPinCode(
                GetPinCodeRequest(customerNo)
            ) { it, errorMessage ->

                if (it != null) {
                    if (it.success) {
                        if (it.data.ReturnValue == 0) {
                            val pinCode = SdkHelpers().generatePinCode(
                                customerNo, localAccountSecretKey!!, it.data.SURE.toDouble()
                            )  //   !!   bunu kullandın kontrol et patlatabilir

                            pinSDKResponse = GetPinSDKResponse(true, null, pinCode, it.data.SURE)

                        } else {
                            pinSDKResponse = GetPinSDKResponse(false, it.data.errorMessage, "", 0)
                        }
                    } else {
                        pinSDKResponse = GetPinSDKResponse(false, it.message, "", 0)
                    }
                } else {
                    pinSDKResponse = GetPinSDKResponse(false, errorMessage, "", 0)
                }
                continuation.resume(pinSDKResponse)
            }
        }

        return response
    }

    fun removeAccount(customerNo: String, context: FragmentActivity): OptPassResult {
        if (isAuthenticated) {   // BUNU !   DEĞİŞTİRDİM SONRA DÜZELT
            throw NotAuthenticatedException("Kullanıcı kimlik doğrulaması yapılmamış.")
        }

        try {
            SecureDataManager(context).removeAcount(customerNo)
            return OptPassResult(success = true, errorMessage = null)
        } catch (e: Exception) {
            return OptPassResult(
                success = false, errorMessage = "Hesap silinirken hata oluştu. İşlem başarısız."
            )
        }
    }

    // bu deneme amaclı silinecek gerek yok
    fun customerSecretKeyDecrypted(context: FragmentActivity) {

        val result = SecureDataManager(context).getCustomerSecretKey("3")
        println("sdk deneme içi " + result.toString())
    }
}

data class OptPassResult(val success: Boolean, val errorMessage: String?)

data class SkewResult(val success: Boolean, val errorMessage: String?, val skew: Double)

data class AccountListResult(
    val success: Boolean, val errorMessage: String?, val accounts: List<AccountListItem>
)

data class AccountListItem(
    val customerNo: Int, val title: String, val isExpired: Boolean, val expirationMessage: String?
)

data class GetPinSDKResponse(
    val success: Boolean, val errorMessage: String?, val pinCode: String, val remainingSeconds: Int
)

class InitNotCompletedException(message: String) : Exception(message)

class NotAuthenticatedException(message: String) : Exception(message)

