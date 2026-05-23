package com.spc.nutricoach.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spc.nutricoach.data.NutriCoachApiClient
import com.spc.nutricoach.data.SessionManager
import com.spc.nutricoach.model.Rutina
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import com.spc.nutricoach.data.RoutineTrackerManager
import retrofit2.HttpException
import java.io.IOException

class RutinaViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val routineTrackerManager = RoutineTrackerManager(application)

    var rutinas by mutableStateOf<List<Rutina>>(emptyList())
        private set

    var rutinasPublicas by mutableStateOf<List<Rutina>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var lastLoadedClientId by mutableStateOf<String?>(null)
        private set

    fun cargarRutinas(force: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            val clienteId = sessionManager.getClienteId()
            if (!force && rutinas.isNotEmpty() && lastLoadedClientId == clienteId && !clienteId.isNullOrBlank()) {
                return@launch
            }
            isLoading = true
            error = null
            if (lastLoadedClientId != clienteId) {
                rutinas = emptyList()
            }
            loadRutinas(clienteId)
        }
    }

    private suspend fun loadRutinas(clienteId: String?) {
        try {
            if (clienteId.isNullOrBlank()) {
                error = "No se encontró el ID del cliente"
                isLoading = false
                return
            }
            val token = sessionManager.getToken()
            if (token.isNullOrBlank()) {
                error = "No hay sesión activa"
                isLoading = false
                return
            }
            val resultado =
                NutriCoachApiClient.service.obtenerRutinasCliente(clienteId, "Bearer $token")
            Log.d("RUTINAS", "Rutinas obtenidas: ${resultado.size}")
            rutinas = resultado
            lastLoadedClientId = clienteId
        } catch (e: HttpException) {
            Log.e("RUTINAS_ERROR", "HTTP ${e.code()}: ${e.message()}")
            error = "Error del servidor (${e.code()})"
        } catch (e: IOException) {
            Log.e("RUTINAS_ERROR", "Error de red: ${e.message}", e)
            error = "Error de conexión"
        } catch (e: Exception) {
            Log.e("RUTINAS_ERROR", "Error inesperado: ${e.message}", e)
            error = "Error inesperado: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    fun cargarRutinasPublicas(force: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!force && rutinasPublicas.isNotEmpty()) {
                return@launch
            }
            isLoading = true
            error = null
            try {
                val token = sessionManager.getToken()
                if (token.isNullOrBlank()) {
                    error = "No hay sesión activa"
                    return@launch
                }
                val resultado = NutriCoachApiClient.service.obtenerRutinasPublicas("Bearer $token")
                Log.d("RUTINAS_PUBLICAS", "Rutinas públicas obtenidas: ${resultado.size}")
                rutinasPublicas = resultado
            } catch (e: HttpException) {
                Log.e("RUTINAS_PUB_ERROR", "HTTP ${e.code()}: ${e.message()}")
                error = "Error del servidor (${e.code()})"
            } catch (e: IOException) {
                Log.e("RUTINAS_PUB_ERROR", "Error de red: ${e.message}", e)
                error = "Error de conexión"
            } catch (e: Exception) {
                Log.e("RUTINAS_PUB_ERROR", "Error inesperado: ${e.message}", e)
                error = "Error inesperado: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun cargarRutinaPorId(rutinaId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // If it's already in the lists, no need to fetch
                if (rutinas.any { it.id == rutinaId } || rutinasPublicas.any { it.id == rutinaId }) {
                    return@launch
                }
                
                val token = sessionManager.getToken()
                if (token.isNullOrBlank()) return@launch
                
                isLoading = true
                val rutina = NutriCoachApiClient.service.obtenerRutinaPorId(rutinaId, "Bearer $token")
                
                // Add it to rutinasPublicas so it can be viewed
                rutinasPublicas = rutinasPublicas + rutina
            } catch (e: Exception) {
                Log.e("RUTINAS_API", "Error al cargar rutina por ID", e)
            } finally {
                isLoading = false
            }
        }
    }
    fun getExerciseWeightFlow(rutinaId: String, exerciseKey: String): Flow<String?> {
        val clienteId = lastLoadedClientId ?: return kotlinx.coroutines.flow.flowOf(null)
        return routineTrackerManager.getExerciseWeightFlow(clienteId, rutinaId, exerciseKey)
    }

    fun saveExerciseWeight(rutinaId: String, exerciseKey: String, weight: String) {
        viewModelScope.launch {
            val clienteId = sessionManager.getClienteId() ?: return@launch
            routineTrackerManager.saveExerciseWeight(clienteId, rutinaId, exerciseKey, weight)
        }
    }

    fun crearRutina(nombre: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val clienteId = sessionManager.getClienteId()
                if (clienteId.isNullOrBlank()) {
                    onResult(false, "No se encontró el ID del cliente")
                    return@launch
                }

                val token = sessionManager.getToken()
                if (token.isNullOrBlank()) {
                    onResult(false, "No hay sesión activa")
                    return@launch
                }

                val request = com.spc.nutricoach.data.CrearRutinaRequest(
                    nombre = nombre,
                    cliente_id = clienteId
                )

                NutriCoachApiClient.service.crearRutina("Bearer $token", request)

                // Recargar rutinas para mostrar la nueva
                loadRutinas(clienteId)
                onResult(true, null)
            } catch (e: HttpException) {
                onResult(false, "Error del servidor (${e.code()})")
            } catch (e: IOException) {
                onResult(false, "Error de conexión")
            } catch (e: Exception) {
                onResult(false, "Error inesperado: ${e.message}")
            }
        }
    }

    fun agregarDia(rutinaId: String, nombre: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = sessionManager.getToken()
                if (token.isNullOrBlank()) {
                    onResult(false, "No hay sesión activa")
                    return@launch
                }

                val request = com.spc.nutricoach.data.AgregarDiaRequest(nombre = nombre)
                NutriCoachApiClient.service.agregarDiaARutina(rutinaId, "Bearer $token", request)

                // Recargar rutinas
                val clienteId = sessionManager.getClienteId()
                loadRutinas(clienteId)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }

    fun agregarEjercicio(
        rutinaId: String,
        diaIndex: Int,
        nombre: String,
        series: Int,
        repeticiones: Int,
        descanso: Int,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Comprobación de duplicados local (sin espacios ni mayúsculas)
                val normalizeRegex = "\\s".toRegex()
                val normalizedNewName = nombre.lowercase().replace(normalizeRegex, "")
                
                val rutina = rutinas.find { it.id == rutinaId }
                val dia = rutina?.dias?.getOrNull(diaIndex)
                val yaExiste = dia?.ejercicios?.any { 
                    it.nombreSnapshot.lowercase().replace(normalizeRegex, "") == normalizedNewName 
                } ?: false
                
                if (yaExiste) {
                    onResult(false, "El ejercicio '$nombre' ya está registrado en este día")
                    return@launch
                }

                val token = sessionManager.getToken()
                if (token.isNullOrBlank()) {
                    onResult(false, "No hay sesión activa")
                    return@launch
                }

                val request = com.spc.nutricoach.data.AgregarEjercicioRequest(
                    ejercicio_id = "000000000000000000000000",
                    nombre_snapshot = nombre,
                    series = series,
                    repeticiones = repeticiones,
                    descanso_segundos = descanso
                )

                NutriCoachApiClient.service.agregarEjercicioADia(
                    rutinaId,
                    diaIndex,
                    "Bearer $token",
                    request
                )

                // Recargar rutinas
                val clienteId = sessionManager.getClienteId()
                loadRutinas(clienteId)
                onResult(true, null)
            } catch (e: Exception) {
                val errorMsg = if (e is retrofit2.HttpException) {
                    try {
                        val body = e.response()?.errorBody()?.string()
                        if (body != null && body.contains("error")) {
                            val json = kotlinx.serialization.json.Json.parseToJsonElement(body)
                            json.let { it as? kotlinx.serialization.json.JsonObject }?.get("error")?.let { it as? kotlinx.serialization.json.JsonPrimitive }?.content
                        } else null
                    } catch (ex: Exception) {
                        null
                    } ?: "Error del servidor (${e.code()})"
                } else {
                    e.message
                }
                onResult(false, errorMsg ?: "Error desconocido")
            }
        }
    }

    fun toggleRutinaPublica(
        rutinaId: String,
        isPublica: Boolean,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = sessionManager.getToken()
                if (token.isNullOrBlank()) {
                    onResult(false, "No hay sesión activa")
                    return@launch
                }

                val request = com.spc.nutricoach.data.ModificarRutinaRequest(publica = isPublica)
                NutriCoachApiClient.service.modificarRutina(rutinaId, "Bearer $token", request)

                // Actualizar estado local para evitar parpadeos
                rutinas =
                    rutinas.map { if (it.id == rutinaId) it.copy(publica = isPublica) else it }
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }

    fun modificarNombreRutina(rutinaId: String, nombre: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = sessionManager.getToken()
                if (token.isNullOrBlank()) return@launch
                val request = com.spc.nutricoach.data.ModificarRutinaRequest(nombre = nombre)
                NutriCoachApiClient.service.modificarRutina(rutinaId, "Bearer $token", request)
                rutinas = rutinas.map { if (it.id == rutinaId) it.copy(nombre = nombre) else it }
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }

    fun eliminarRutina(rutinaId: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = sessionManager.getToken()
                if (token.isNullOrBlank()) return@launch
                NutriCoachApiClient.service.eliminarRutina(rutinaId, "Bearer $token")
                rutinas = rutinas.filterNot { it.id == rutinaId }
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }

    fun eliminarDia(rutinaId: String, diaIndex: Int, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = sessionManager.getToken()
                if (token.isNullOrBlank()) return@launch
                NutriCoachApiClient.service.eliminarDia(rutinaId, diaIndex, "Bearer $token")
                val clienteId = sessionManager.getClienteId()
                loadRutinas(clienteId)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }

    fun eliminarEjercicio(rutinaId: String, diaIndex: Int, ejercicioIndex: Int, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = sessionManager.getToken()
                if (token.isNullOrBlank()) return@launch
                NutriCoachApiClient.service.eliminarEjercicio(rutinaId, diaIndex, ejercicioIndex, "Bearer $token")
                val clienteId = sessionManager.getClienteId()
                loadRutinas(clienteId)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }

    fun clonarRutinaPorId(rutinaId: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.Main) {
            val result = kotlinx.coroutines.withContext(Dispatchers.IO) {
                try {
                    val clienteId = sessionManager.getClienteId()
                    if (clienteId.isNullOrBlank()) {
                        return@withContext Pair(false, "No se encontró el ID del cliente")
                    }
                    val token = sessionManager.getToken()
                    if (token.isNullOrBlank()) {
                        return@withContext Pair(false, "No hay sesión activa")
                    }

                    // 1. Obtener la rutina original
                    val original =
                        NutriCoachApiClient.service.obtenerRutinaPorId(rutinaId, "Bearer $token")

                    // 2. Crear una copia para el usuario actual
                    val request = com.spc.nutricoach.data.CrearRutinaRequest(
                        nombre = original.nombre,
                        cliente_id = clienteId,
                        dias = original.dias,
                        activa = true,
                        publica = false
                    )
                    NutriCoachApiClient.service.crearRutina("Bearer $token", request)

                    // 3. Recargar rutinas del usuario
                    loadRutinas(clienteId)
                    Pair(true, null)
                } catch (e: HttpException) {
                    Pair(false, "Error del servidor (${e.code()})")
                } catch (e: IOException) {
                    Pair(false, "Error de conexión")
                } catch (e: Exception) {
                    Pair(false, "Error: ${e.message}")
                }
            }
            onResult(result.first, result.second)
        }
    }

    var historialEntrenamientos by mutableStateOf<List<com.spc.nutricoach.data.EntrenamientoLog>>(emptyList())
        private set

    var isLoadingHistorial by mutableStateOf(false)
        private set

    var errorHistorial by mutableStateOf<String?>(null)
        private set

    fun cargarHistorialEntrenamientos() {
        viewModelScope.launch(Dispatchers.IO) {
            isLoadingHistorial = true
            errorHistorial = null
            try {
                val token = sessionManager.getToken()
                val clienteId = sessionManager.getClienteId()
                if (token.isNullOrBlank() || clienteId.isNullOrBlank()) {
                    errorHistorial = "No se encontró sesión o ID de cliente"
                    return@launch
                }
                
                val resultado = NutriCoachApiClient.service.obtenerHistorialEntrenamientos(clienteId, "Bearer $token")
                Log.d("HISTORIAL", "Historial obtenido: ${resultado.size}")
                historialEntrenamientos = resultado
            } catch (e: Exception) {
                Log.e("HISTORIAL_ERROR", "Error al cargar historial", e)
                errorHistorial = e.message ?: "Error al recuperar el historial"
            } finally {
                isLoadingHistorial = false
            }
        }
    }
}
