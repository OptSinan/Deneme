package Services.Network

import Data.AccountListRequest
import Data.AccountListResponse
import Data.BeginActivationRequest
import Data.BeginActivationResponse
import Data.CheckDateRequest
import Data.CheckDateResponse
import Data.CompleteActivationRequest
import Data.CompleteActivationResponse
import Data.GetPinCodeRequest
import Data.GetPinCodeResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

  internal interface ApiService {

    @POST("/Pass/check-date")
    fun chechDate(@Body checkDateRequest: CheckDateRequest): Call<CheckDateResponse>

    @POST("/otpRegister")
    fun beginActivation(@Body beginActivationRequestBody: BeginActivationRequest): Call<BeginActivationResponse>

    @POST("/otpRegister")
    fun completeActivation(@Body completeActivationRequestBody: CompleteActivationRequest): Call<CompleteActivationResponse>

    @POST("/apicall/anonymous/SP_TOTP_AKTIVASYON_MUSTERI_VERSIYON_KONTROL")
    fun accountList(@Body accountListRequestBody: AccountListRequest): Call<AccountListResponse>

    @POST("/apicall/anonymous/OPTIMUS_PASS_TOKEN_GECERLILIK_SANIYE_BUL")
    fun getPinCode(@Body getPinCodeRequestBody: GetPinCodeRequest): Call<GetPinCodeResponse>
}






