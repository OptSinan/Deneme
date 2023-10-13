package Data

import java.io.Serializable

data class BeginActivationResponse(
    val data: String?,
    val success: Boolean,
    val message: String,
    val statusCode: Int
) : Serializable
