package com.spc.nutricoach.data.repository

import com.spc.nutricoach.data.*
import com.spc.nutricoach.data.api.DietaApiService
import com.spc.nutricoach.model.Dieta
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DietaRepository @Inject constructor(
    private val dietaApiService: DietaApiService,
    private val sessionManager: SessionManager
) {
    suspend fun obtenerDietasCliente(clienteId: String, token: String): ApiResponse<List<Dieta>> = safeApiCall {
        dietaApiService.obtenerDietasCliente(clienteId, token)
    }

    suspend fun obtenerDietasPublicas(token: String): ApiResponse<List<Dieta>> = safeApiCall {
        dietaApiService.obtenerDietasPublicas(token)
    }

    suspend fun crearDieta(token: String, request: CrearDietaRequest): ApiResponse<CrearDietaResponse> = safeApiCall {
        dietaApiService.crearDieta(token, request)
    }

    suspend fun modificarDieta(dietaId: String, token: String, request: ModificarDietaRequest): ApiResponse<Unit> = safeApiCall {
        dietaApiService.modificarDieta(dietaId, token, request)
    }

    suspend fun eliminarDieta(dietaId: String, token: String): ApiResponse<Unit> = safeApiCall {
        dietaApiService.eliminarDieta(dietaId, token)
    }

    suspend fun agregarComidaADieta(dietaId: String, token: String, request: AgregarComidaRequest): ApiResponse<Unit> = safeApiCall {
        dietaApiService.agregarComidaADieta(dietaId, token, request)
    }

    suspend fun agregarAlimentoAComida(dietaId: String, comidaIndex: Int, token: String, request: AgregarAlimentoRequest): ApiResponse<Unit> = safeApiCall {
        dietaApiService.agregarAlimentoAComida(dietaId, comidaIndex, token, request)
    }

    suspend fun eliminarComida(dietaId: String, comidaIndex: Int, token: String): ApiResponse<Unit> = safeApiCall {
        dietaApiService.eliminarComida(dietaId, comidaIndex, token)
    }

    suspend fun eliminarAlimento(dietaId: String, comidaIndex: Int, alimentoIndex: Int, token: String): ApiResponse<Unit> = safeApiCall {
        dietaApiService.eliminarAlimento(dietaId, comidaIndex, alimentoIndex, token)
    }

    val session = sessionManager
}
