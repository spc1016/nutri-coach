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
    suspend fun obtenerDietasCliente(clienteId: String): ApiResponse<List<Dieta>> = safeApiCall {
        dietaApiService.obtenerDietasCliente(clienteId)
    }

    suspend fun obtenerDietasPublicas(): ApiResponse<List<Dieta>> = safeApiCall {
        dietaApiService.obtenerDietasPublicas()
    }

    suspend fun obtenerDietaPorId(dietaId: String): ApiResponse<Dieta> = safeApiCall {
        dietaApiService.obtenerDietaPorId(dietaId)
    }

    suspend fun crearDieta(request: CrearDietaRequest): ApiResponse<CrearDietaResponse> = safeApiCall {
        dietaApiService.crearDieta(request)
    }

    suspend fun modificarDieta(dietaId: String, request: ModificarDietaRequest): ApiResponse<Unit> = safeApiCall {
        dietaApiService.modificarDieta(dietaId, request)
    }

    suspend fun eliminarDieta(dietaId: String): ApiResponse<Unit> = safeApiCall {
        dietaApiService.eliminarDieta(dietaId)
    }

    suspend fun agregarComidaADieta(dietaId: String, request: AgregarComidaRequest): ApiResponse<Unit> = safeApiCall {
        dietaApiService.agregarComidaADieta(dietaId, request)
    }

    suspend fun agregarAlimentoAComida(dietaId: String, comidaIndex: Int, request: AgregarAlimentoRequest): ApiResponse<Unit> = safeApiCall {
        dietaApiService.agregarAlimentoAComida(dietaId, comidaIndex, request)
    }

    suspend fun eliminarComida(dietaId: String, comidaIndex: Int): ApiResponse<Unit> = safeApiCall {
        dietaApiService.eliminarComida(dietaId, comidaIndex)
    }

    suspend fun eliminarAlimento(dietaId: String, comidaIndex: Int, alimentoIndex: Int): ApiResponse<Unit> = safeApiCall {
        dietaApiService.eliminarAlimento(dietaId, comidaIndex, alimentoIndex)
    }

    val session = sessionManager
}
