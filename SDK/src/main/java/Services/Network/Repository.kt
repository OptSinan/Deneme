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

class Repository(private val apiService: ApiService) {

    fun chechDate(checkDateRequest: CheckDateRequest): Call<CheckDateResponse> {
        return apiService.chechDate(checkDateRequest)
    }

    fun beginActivation(beginActivationRequest: BeginActivationRequest): Call<BeginActivationResponse> {
        return apiService.beginActivation(beginActivationRequest)
    }

    fun completeActivation(completeActivationRequestBody: CompleteActivationRequest): Call<CompleteActivationResponse> {
        return apiService.completeActivation(completeActivationRequestBody)
    }

    fun accountList(accountListRequestBody: AccountListRequest): Call<AccountListResponse> {
        return apiService.accountList(accountListRequestBody)
    }

    fun getPinCode(getPinCodeRequestBody: GetPinCodeRequest): Call<GetPinCodeResponse> {
        return apiService.getPinCode(getPinCodeRequestBody)
    }

}

