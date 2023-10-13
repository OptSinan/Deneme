package Data

import java.io.Serializable

data class BeginActivationRequest(
    val customerNo: String?,
    val password: String?,
    val identityNo: String?,
    val phone: String?,
    val deviceId: String?,
    val deviceType: String = "A", // Sabit değer AND için
    val mobileAppVersion: String?
) : Serializable