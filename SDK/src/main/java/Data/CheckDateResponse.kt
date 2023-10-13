package Data

import java.io.Serializable

data class CheckDateResponse(
    val data: ResponseData,
    val success: Boolean,
    val message: String?,
    val statusCode: Int
) : Serializable

data class ResponseData(
    val dateIsValid: Boolean,
    val totalSeconds: Double
) : Serializable