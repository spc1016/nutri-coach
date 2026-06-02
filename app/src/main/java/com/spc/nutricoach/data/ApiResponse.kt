package com.spc.nutricoach.data

import retrofit2.HttpException
import java.io.IOException

sealed interface ApiResponse<out T> {
    data class Success<out T>(val data: T) : ApiResponse<T>
    data class Error(val code: Int, val message: String) : ApiResponse<Nothing>
    data class Exception(val throwable: Throwable) : ApiResponse<Nothing>
}

suspend fun <T> safeApiCall(apiCall: suspend () -> T): ApiResponse<T> {
    return try {
        ApiResponse.Success(apiCall())
    } catch (e: HttpException) {
        val errorMsg = try {
            e.response()?.errorBody()?.string() ?: e.message()
        } catch (ex: kotlin.Exception) {
            e.message()
        }
        ApiResponse.Error(e.code(), errorMsg)
    } catch (e: IOException) {
        ApiResponse.Exception(e)
    } catch (e: Exception) {
        ApiResponse.Exception(e)
    }
}
