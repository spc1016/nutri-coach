package com.spc.nutricoach.data.api

import com.spc.nutricoach.data.LoginRequest
import com.spc.nutricoach.data.LoginApiResponse
import com.spc.nutricoach.data.SendCodeRequest
import com.spc.nutricoach.data.MessageResponse
import com.spc.nutricoach.data.VerifyCodeRequest
import com.spc.nutricoach.data.RegistroApiResponse
import com.spc.nutricoach.data.ResendCodeRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginApiResponse

    @POST("register/send-code")
    suspend fun sendVerificationCode(@Body request: SendCodeRequest): MessageResponse

    @POST("register/verify-code")
    suspend fun verifyCode(@Body request: VerifyCodeRequest): RegistroApiResponse

    @POST("register/resend-code")
    suspend fun resendCode(@Body request: ResendCodeRequest): MessageResponse
}
