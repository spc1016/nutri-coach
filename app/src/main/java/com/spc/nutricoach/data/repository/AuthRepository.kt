package com.spc.nutricoach.data.repository

import com.spc.nutricoach.data.*
import com.spc.nutricoach.data.api.AuthApiService
import com.spc.nutricoach.data.api.ClienteApiService
import com.spc.nutricoach.model.Cliente
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val clienteApiService: ClienteApiService,
    private val sessionManager: SessionManager
) {
    suspend fun login(request: LoginRequest): ApiResponse<LoginApiResponse> = safeApiCall {
        authApiService.login(request)
    }

    suspend fun sendVerificationCode(request: SendCodeRequest): ApiResponse<MessageResponse> = safeApiCall {
        authApiService.sendVerificationCode(request)
    }

    suspend fun verifyCode(request: VerifyCodeRequest): ApiResponse<RegistroApiResponse> = safeApiCall {
        authApiService.verifyCode(request)
    }

    suspend fun resendCode(request: ResendCodeRequest): ApiResponse<MessageResponse> = safeApiCall {
        authApiService.resendCode(request)
    }

    suspend fun obtenerCliente(clienteId: String): ApiResponse<Cliente> = safeApiCall {
        clienteApiService.obtenerCliente(clienteId)
    }

    suspend fun modificarCliente(clienteId: String, request: ModificarClienteRequest): ApiResponse<Unit> = safeApiCall {
        clienteApiService.modificarCliente(clienteId, request)
    }

    suspend fun obtenerTodosClientes(): ApiResponse<List<Cliente>> = safeApiCall {
        clienteApiService.obtenerTodosClientes()
    }

    suspend fun seguirUsuario(userId: String): ApiResponse<Unit> = safeApiCall {
        clienteApiService.seguirUsuario(userId)
    }

    suspend fun dejarDeSeguirUsuario(userId: String): ApiResponse<Unit> = safeApiCall {
        clienteApiService.dejarDeSeguirUsuario(userId)
    }

    suspend fun obtenerSeguidores(userId: String): ApiResponse<List<Cliente>> = safeApiCall {
        clienteApiService.obtenerSeguidores(userId)
    }

    suspend fun obtenerSeguidos(userId: String): ApiResponse<List<Cliente>> = safeApiCall {
        clienteApiService.obtenerSeguidos(userId)
    }

    val session = sessionManager
}
