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
    suspend fun obtenerRutinasCliente(clienteId: String): ApiResponse<List<Rutina>> = safeApiCall {
        rutinaApiService.obtenerRutinasCliente(clienteId)
    }

    suspend fun registrarEntrenamiento(clienteId: String, request: EntrenamientoLog): ApiResponse<RegistroApiResponse> = safeApiCall {
        rutinaApiService.registrarEntrenamiento(clienteId, request)
    }

    suspend fun obtenerHistorialEntrenamientos(clienteId: String): ApiResponse<List<EntrenamientoLog>> = safeApiCall {
        rutinaApiService.obtenerHistorialEntrenamientos(clienteId)
    }

    suspend fun obtenerRutinasPublicas(): ApiResponse<List<Rutina>> = safeApiCall {
        rutinaApiService.obtenerRutinasPublicas()
    }

    suspend fun obtenerRutinaPorId(rutinaId: String): ApiResponse<Rutina> = safeApiCall {
        rutinaApiService.obtenerRutinaPorId(rutinaId)
    }

    suspend fun crearRutina(request: CrearRutinaRequest): ApiResponse<CrearRutinaResponse> = safeApiCall {
        rutinaApiService.crearRutina(request)
    }

    suspend fun agregarDiaARutina(rutinaId: String, request: AgregarDiaRequest): ApiResponse<Unit> = safeApiCall {
        rutinaApiService.agregarDiaARutina(rutinaId, request)
    }

    suspend fun agregarEjercicioADia(rutinaId: String, diaIndex: Int, request: AgregarEjercicioRequest): ApiResponse<Unit> = safeApiCall {
        rutinaApiService.agregarEjercicioADia(rutinaId, diaIndex, request)
    }

    suspend fun modificarRutina(rutinaId: String, request: ModificarRutinaRequest): ApiResponse<Unit> = safeApiCall {
        rutinaApiService.modificarRutina(rutinaId, request)
    }

    suspend fun eliminarRutina(rutinaId: String): ApiResponse<Unit> = safeApiCall {
        rutinaApiService.eliminarRutina(rutinaId)
    }

    suspend fun eliminarDia(rutinaId: String, diaIndex: Int): ApiResponse<Unit> = safeApiCall {
        rutinaApiService.eliminarDia(rutinaId, diaIndex)
    }

    suspend fun eliminarEjercicio(rutinaId: String, diaIndex: Int, ejercicioIndex: Int): ApiResponse<Unit> = safeApiCall {
        rutinaApiService.eliminarEjercicio(rutinaId, diaIndex, ejercicioIndex)
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
