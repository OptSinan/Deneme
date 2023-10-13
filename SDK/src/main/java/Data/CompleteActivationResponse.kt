package Data

import java.io.Serializable

data class CompleteActivationResponse(
    val data: Data,
    val success: Boolean,
    val message: String?,
    val statusCode: Int
) : Serializable

data class Data(
    val title: String,
    val secretKey: String,
    val creationDate: Int
) : Serializable