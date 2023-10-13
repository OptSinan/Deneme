package Data

import java.io.Serializable

data class GetPinCodeResponse(
    val data: DataPin,
    val success: Boolean,
    val message: String?,
    val statusCode: Int
) : Serializable

data class DataPin(
    val SURE: Int,
    val ReturnValue: Int,
    val errorMessage: String,
    val errorUniqueCode: Int
) : Serializable
