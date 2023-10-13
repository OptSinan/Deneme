package Data

import java.io.Serializable

data class DataResp(
    val R1: List<R1List>,
    val Output: List<Output>
) : Serializable

data class R1List(
    val MUSTERI_NO: Int,
    val KAYIT_ANI: Long
) : Serializable

data class Output(
    val ReturnValue: Int
) : Serializable

data class AccountListResponse(
    val data: DataResp,
    val success: Boolean,
    val message: String?,
    val statusCode: Int
) : Serializable
