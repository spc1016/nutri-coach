package com.spc.nutricoach.data.repository

import com.spc.nutricoach.data.*
import com.spc.nutricoach.data.api.RutinaApiService
import com.spc.nutricoach.data.local.dao.PesoHistorialDao
import com.spc.nutricoach.data.local.entity.PesoHistorialEntity
import com.spc.nutricoach.model.Rutina
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RutinaRepository @Inject constructor(
    private val rutinaApiService: RutinaApiService,
    private val pesoHistorialDao: PesoHistorialDao,
    private val sessionManager: SessionManager
) {
    suspend fun obtenerRutinasCliente(clienteId: String, token: String): ApiResponse<List<Rutina>> = safeApiCall {
        rutinaApiService.obtenerRutinasCliente(clienteId, token)
    }

    suspend fun registrarEntrenamiento(clienteId: String, token: String, request: EntrenamientoLog): ApiResponse<RegistroApiResponse> = safeApiCall {
        rutinaApiService.registrarEntrenamiento(clienteId, token, request)
    }

    suspend fun obtenerHistorialEntrenamientos(clienteId: String, token: String): ApiResponse<List<EntrenamientoLog>> = safeApiCall {
        rutinaApiService.obtenerHistorialEntrenamientos(clienteId, token)
    }

    suspend fun obtenerRutinasPublicas(token: String): ApiResponse<List<Rutina>> = safeApiCall {
        rutinaApiService.obtenerRutinasPublicas(token)
    }

    suspend fun obtenerRutinaPorId(rutinaId: String, token: String): ApiResponse<Rutina> = safeApiCall {
        rutinaApiService.obtenerRutinaPorId(rutinaId, token)
    }

    suspend fun crearRutina(token: String, request: CrearRutinaRequest): ApiResponse<CrearRutinaResponse> = safeApiCall {
        rutinaApiService.crearRutina(token, request)
    }

    suspend fun agregarDiaARutina(rutinaId: String, token: String, request: AgregarDiaRequest): ApiResponse<Unit> = safeApiCall {
        rutinaApiService.agregarDiaARutina(rutinaId, token, request)
    }

    suspend fun agregarEjercicioADia(rutinaId: String, diaIndex: Int, token: String, request: AgregarEjercicioRequest): ApiResponse<Unit> = safeApiCall {
        rutinaApiService.agregarEjercicioADia(rutinaId, diaIndex, token, request)
    }

    suspend fun modificarRutina(rutinaId: String, token: String, request: ModificarRutinaRequest): ApiResponse<Unit> = safeApiCall {
        rutinaApiService.modificarRutina(rutinaId, token, request)
    }

    suspend fun eliminarRutina(rutinaId: String, token: String): ApiResponse<Unit> = safeApiCall {
        rutinaApiService.eliminarRutina(rutinaId, token)
    }

    suspend fun eliminarDia(rutinaId: String, diaIndex: Int, token: String): ApiResponse<Unit> = safeApiCall {
        rutinaApiService.eliminarDia(rutinaId, diaIndex, token)
    }

    suspend fun eliminarEjercicio(rutinaId: String, diaIndex: Int, ejercicioIndex: Int, token: String): ApiResponse<Unit> = safeApiCall {
        rutinaApiService.eliminarEjercicio(rutinaId, diaIndex, ejercicioIndex, token)
    }

    fun getExerciseWeightFlow(clienteId: String, rutinaId: String, exerciseKey: String): Flow<String?> {
        return pesoHistorialDao.obtenerUltimoPeso(clienteId, rutinaId, exerciseKey)
    }

    suspend fun saveExerciseWeight(clienteId: String, rutinaId: String, exerciseKey: String, weight: String) {
        val entity = PesoHistorialEntity(
            clienteId = clienteId,
            rutinaId = rutinaId,
            exerciseKey = exerciseKey,
            peso = weight
        )
        pesoHistorialDao.insertarPeso(entity)
    }

    val session = sessionManager
}
