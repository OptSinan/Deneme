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
import retrofit2.Callback
import retrofit2.Response

internal  class ServiceManager(baseUrl: String) {

    private val baseService: BaseService = BaseService(baseUrl)
    private val apiService: ApiService = baseService.createService(ApiService::class.java)
    val repository: Repository = Repository(apiService)

    fun <T> executeCall(call: Call<T>, callback: (Result<T>) -> Unit) {
        call.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    callback(Result.Success(data))
                } else {
                    val errorBody = response.errorBody()
                    val errorMessage = errorBody?.string() ?: "Unknown error"
                    callback(Result.Error(errorMessage))
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                callback(Result.Error(t.message ?: "An error occurred"))
            }
        })
    }

    sealed class Result<out T> {
        data class Success<out T>(val data: T?) : Result<T>()
        data class Error(val errorMessage: String) : Result<Nothing>()
    }

    fun handleChechDate(
        checkDateRequest: CheckDateRequest,
        callback: (CheckDateResponse?, errorMessage: String?) -> Unit
    ) {
        val call: Call<CheckDateResponse> = repository.chechDate(checkDateRequest)
        executeCall(call) { result ->
            when (result) {
                is Result.Success -> {
                    val data = result.data
                    // Başarılı sonuç işleme
                    println("Result DATE APİ = " + data?.toString())
                    callback(data, null)
                }

                is Result.Error -> {
                    val errorMessage = result.errorMessage
                    // Hata durumu işleme
                    println("APİ ERROR MESAGE =" + errorMessage)
                    callback(null, errorMessage)
                }
            }
        }
    }

    fun handleBeginActivation(
        beginActivationRequest: BeginActivationRequest,
        callback: (BeginActivationResponse?, errorMessage: String?) -> Unit
    ) {
        val call: Call<BeginActivationResponse> = repository.beginActivation(beginActivationRequest)
        executeCall(call) { result ->
            when (result) {
                is Result.Success -> {
                    val data = result.data
                    // Başarılı sonuç işleme
                    println("Result begin APİ =" + data?.toString())
                    callback(data, null)
                }

                is Result.Error -> {
                    val errorMessage = result.errorMessage
                    // Hata durumu işleme
                    println("APİ ERROR MESAGE " + errorMessage)
                    callback(null, errorMessage)
                }
            }
        }
    }

    fun handleCompleteActivation(
        completeActivationRequest: CompleteActivationRequest,
        callback: (CompleteActivationResponse?, errorMessage: String?) -> Unit
    ) {
        val call: Call<CompleteActivationResponse> =
            repository.completeActivation(completeActivationRequest)
        executeCall(call) { result ->
            when (result) {
                is Result.Success -> {
                    val data = result.data
                    // Başarılı sonuç işleme
                    println("Result COMPLETE APİ =" + data?.toString())
                    callback(data, null)
                }

                is Result.Error -> {
                    val errorMessage = result.errorMessage
                    // Hata durumu işleme
                    println("APİ ERROR MESAGE =" + errorMessage)
                    callback(null, errorMessage)
                }
            }
        }
    }

    fun handleAccountList(
        accountListRequest: AccountListRequest,
        callback: (AccountListResponse?, errorMessage: String?) -> Unit
    ) {
        val call: Call<AccountListResponse> = repository.accountList(accountListRequest)
        executeCall(call) { result ->
            when (result) {
                is Result.Success -> {
                    val data = result.data
                    // Başarılı sonuç işleme
                    println("Result  AccountList APİ = " + data?.toString())
                    callback(data, null)
                }

                is Result.Error -> {
                    val errorMessage = result.errorMessage
                    // Hata durumu işleme
                    println("APİ ERROR MESAGE " + errorMessage)
                    callback(null, errorMessage)
                }
            }
        }
    }

    fun handleGetPinCode(
        getPinCodeRequestBody: GetPinCodeRequest,
        callback: (GetPinCodeResponse?, errorMessage: String?) -> Unit
    ) {
        val call: Call<GetPinCodeResponse> = repository.getPinCode(getPinCodeRequestBody)
        executeCall(call) { result ->
            when (result) {
                is Result.Success -> {
                    val data = result.data
                    // Başarılı sonuç işleme
                    println("Result  APİ PIN code =   " + data?.toString())
                    callback(data, null)
                }

                is Result.Error -> {
                    val errorMessage = result.errorMessage
                    // Hata durumu işleme
                    println(" APİ ERROR MESAGE " + errorMessage)
                    callback(null, errorMessage)
                }
            }
        }
    }
}



