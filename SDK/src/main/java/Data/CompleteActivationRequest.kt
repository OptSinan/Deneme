package Data

import java.io.Serializable

data class CompleteActivationRequest(
    val customerNo: String,
    val password: String,
    val identityNo: String,
    val phone: String,
    val smsCode: String,
    val deviceId: String,
    val deviceType: String = "A", // Varsayılan ANDROID değer "A"
    val mobileAppVersion: String?
) : Serializable
