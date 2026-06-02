package com.spc.nutricoach.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spc.nutricoach.data.*
import com.spc.nutricoach.data.repository.RutinaRepository
import com.spc.nutricoach.model.Rutina
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RutinaViewModel @Inject constructor(
    private val rutinaRepository: RutinaRepository
) : ViewModel() {

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
            val clienteId = rutinaRepository.session.getClienteId()
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
            val token = rutinaRepository.session.getToken()
            if (token.isNullOrBlank()) {
                error = "No hay sesión activa"
                isLoading = false
                return
            }
            
            when (val response = rutinaRepository.obtenerRutinasCliente(clienteId)) {
                is ApiResponse.Success -> {
                    Log.d("RUTINAS", "Rutinas obtenidas: ${response.data.size}")
                    rutinas = response.data
                    lastLoadedClientId = clienteId
                    error = null
                }
                is ApiResponse.Error -> {
                    Log.e("RUTINAS_ERROR", "Error HTTP ${response.code}: ${response.message}")
                    error = "Error del servidor (${response.code})"
                }
                is ApiResponse.Exception -> {
                    Log.e("RUTINAS_ERROR", "Excepción de red", response.throwable)
                    error = "Error de conexión"
                }
            }
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
                val token = rutinaRepository.session.getToken()
                if (token.isNullOrBlank()) {
                    error = "No hay sesión activa"
                    return@launch
                }
                
                when (val response = rutinaRepository.obtenerRutinasPublicas()) {
                    is ApiResponse.Success -> {
                        Log.d("RUTINAS_PUBLICAS", "Rutinas públicas obtenidas: ${response.data.size}")
                        rutinasPublicas = response.data
                        error = null
                    }
                    is ApiResponse.Error -> {
                        Log.e("RUTINAS_PUB_ERROR", "Error HTTP ${response.code}: ${response.message}")
                        error = "Error del servidor (${response.code})"
                    }
                    is ApiResponse.Exception -> {
                        Log.e("RUTINAS_PUB_ERROR", "Excepción de red", response.throwable)
                        error = "Error de conexión"
                    }
                }
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
                if (rutinas.any { it.id == rutinaId } || rutinasPublicas.any { it.id == rutinaId }) {
                    return@launch
                }
                
                val token = rutinaRepository.session.getToken()
                if (token.isNullOrBlank()) return@launch
                
                isLoading = true
                when (val response = rutinaRepository.obtenerRutinaPorId(rutinaId)) {
                    is ApiResponse.Success -> {
                        rutinasPublicas = rutinasPublicas + response.data
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("RUTINAS_API", "Error al cargar rutina por ID", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun getExerciseWeightFlow(rutinaId: String, exerciseKey: String): Flow<String?> {
        val clienteId = lastLoadedClientId ?: return kotlinx.coroutines.flow.flowOf(null)
        return rutinaRepository.getExerciseWeightFlow(clienteId, rutinaId, exerciseKey)
    }

    fun saveExerciseWeight(rutinaId: String, exerciseKey: String, weight: String) {
        viewModelScope.launch {
            val clienteId = rutinaRepository.session.getClienteId() ?: return@launch
            rutinaRepository.saveExerciseWeight(clienteId, rutinaId, exerciseKey, weight)
        }
    }

    fun crearRutina(nombre: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val clienteId = rutinaRepository.session.getClienteId()
                if (clienteId.isNullOrBlank()) {
                    onResult(false, "No se encontró el ID del cliente")
                    return@launch
                }

                val token = rutinaRepository.session.getToken()
                if (token.isNullOrBlank()) {
                    onResult(false, "No hay sesión activa")
                    return@launch
                }

                val request = CrearRutinaRequest(
                    nombre = nombre,
                    cliente_id = clienteId
                )

                when (val response = rutinaRepository.crearRutina(request)) {
                    is ApiResponse.Success -> {
                        loadRutinas(clienteId)
                        onResult(true, null)
                    }
                    is ApiResponse.Error -> {
                        onResult(false, "Error del servidor (${response.code}): ${response.message}")
                    }
                    is ApiResponse.Exception -> {
                        onResult(false, "Error de conexión")
                    }
                }
            } catch (e: Exception) {
                onResult(false, "Error inesperado: ${e.message}")
            }
        }
    }

    fun agregarDia(rutinaId: String, nombre: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = rutinaRepository.session.getToken()
                if (token.isNullOrBlank()) {
                    onResult(false, "No hay sesión activa")
                    return@launch
                }

                val request = AgregarDiaRequest(nombre = nombre)
                when (val response = rutinaRepository.agregarDiaARutina(rutinaId, request)) {
                    is ApiResponse.Success -> {
                        val clienteId = rutinaRepository.session.getClienteId()
                        loadRutinas(clienteId)
                        onResult(true, null)
                    }
                    is ApiResponse.Error -> {
                        onResult(false, "Error: ${response.message}")
                    }
                    is ApiResponse.Exception -> {
                        onResult(false, "Error de conexión")
                    }
                }
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
                val normalizedNewName = com.spc.nutricoach.util.ExerciseNormalizer.normalize(nombre)
                
                val rutina = rutinas.find { it.id == rutinaId }
                val dia = rutina?.dias?.getOrNull(diaIndex)
                val yaExiste = dia?.ejercicios?.any { 
                    com.spc.nutricoach.util.ExerciseNormalizer.normalize(it.nombreSnapshot) == normalizedNewName 
                } ?: false
                
                if (yaExiste) {
                    onResult(false, "El ejercicio '$nombre' ya está registrado en este día")
                    return@launch
                }

                val token = rutinaRepository.session.getToken()
                if (token.isNullOrBlank()) {
                    onResult(false, "No hay sesión activa")
                    return@launch
                }

                val clienteId = rutinaRepository.session.getClienteId()
                val existingNames = if (!clienteId.isNullOrBlank()) {
                    when (val historyResponse = rutinaRepository.obtenerHistorialEntrenamientos(clienteId)) {
                        is ApiResponse.Success -> historyResponse.data.flatMap { it.ejercicios }.map { it.nombre_snapshot }.distinct()
                        else -> emptyList()
                    }
                } else {
                    emptyList()
                }
                val canonicalName = com.spc.nutricoach.util.ExerciseNormalizer.getCanonicalName(nombre, existingNames)

                val request = AgregarEjercicioRequest(
                    ejercicio_id = "000000000000000000000000",
                    nombre_snapshot = canonicalName,
                    series = series,
                    repeticiones = repeticiones,
                    descanso_segundos = descanso
                )

                when (val response = rutinaRepository.agregarEjercicioADia(rutinaId, diaIndex, request)) {
                    is ApiResponse.Success -> {
                        val clienteId = rutinaRepository.session.getClienteId()
                        loadRutinas(clienteId)
                        onResult(true, null)
                    }
                    is ApiResponse.Error -> {
                        onResult(false, "Error: ${response.message}")
                    }
                    is ApiResponse.Exception -> {
                        onResult(false, "Error de conexión")
                    }
                }
            } catch (e: Exception) {
                onResult(false, e.message ?: "Error desconocido")
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
                val token = rutinaRepository.session.getToken()
                if (token.isNullOrBlank()) {
                    onResult(false, "No hay sesión activa")
                    return@launch
                }

                val request = ModificarRutinaRequest(publica = isPublica)
                when (val response = rutinaRepository.modificarRutina(rutinaId, request)) {
                    is ApiResponse.Success -> {
                        rutinas = rutinas.map { if (it.id == rutinaId) it.copy(publica = isPublica) else it }
                        onResult(true, null)
                    }
                    is ApiResponse.Error -> {
                        onResult(false, "Error: ${response.message}")
                    }
                    is ApiResponse.Exception -> {
                        onResult(false, "Error de conexión")
                    }
                }
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }

    fun modificarNombreRutina(rutinaId: String, nombre: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = rutinaRepository.session.getToken() ?: return@launch
                val request = ModificarRutinaRequest(nombre = nombre)
                when (val response = rutinaRepository.modificarRutina(rutinaId, request)) {
                    is ApiResponse.Success -> {
                        rutinas = rutinas.map { if (it.id == rutinaId) it.copy(nombre = nombre) else it }
                        onResult(true, null)
                    }
                    is ApiResponse.Error -> {
                        onResult(false, "Error: ${response.message}")
                    }
                    is ApiResponse.Exception -> {
                        onResult(false, "Error de conexión")
                    }
                }
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }

    fun eliminarRutina(rutinaId: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = rutinaRepository.session.getToken() ?: return@launch
                when (val response = rutinaRepository.eliminarRutina(rutinaId)) {
                    is ApiResponse.Success -> {
                        rutinas = rutinas.filterNot { it.id == rutinaId }
                        onResult(true, null)
                    }
                    is ApiResponse.Error -> {
                        onResult(false, "Error: ${response.message}")
                    }
                    is ApiResponse.Exception -> {
                        onResult(false, "Error de conexión")
                    }
                }
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }

    fun eliminarDia(rutinaId: String, diaIndex: Int, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = rutinaRepository.session.getToken() ?: return@launch
                when (val response = rutinaRepository.eliminarDia(rutinaId, diaIndex)) {
                    is ApiResponse.Success -> {
                        val clienteId = rutinaRepository.session.getClienteId()
                        loadRutinas(clienteId)
                        onResult(true, null)
                    }
                    is ApiResponse.Error -> {
                        onResult(false, "Error: ${response.message}")
                    }
                    is ApiResponse.Exception -> {
                        onResult(false, "Error de conexión")
                    }
                }
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }

    fun eliminarEjercicio(rutinaId: String, diaIndex: Int, ejercicioIndex: Int, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = rutinaRepository.session.getToken() ?: return@launch
                when (val response = rutinaRepository.eliminarEjercicio(rutinaId, diaIndex, ejercicioIndex)) {
                    is ApiResponse.Success -> {
                        val clienteId = rutinaRepository.session.getClienteId()
                        loadRutinas(clienteId)
                        onResult(true, null)
                    }
                    is ApiResponse.Error -> {
                        onResult(false, "Error: ${response.message}")
                    }
                    is ApiResponse.Exception -> {
                        onResult(false, "Error de conexión")
                    }
                }
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }

    fun clonarRutinaPorId(rutinaId: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.Main) {
            val result = kotlinx.coroutines.withContext(Dispatchers.IO) {
                try {
                    val clienteId = rutinaRepository.session.getClienteId()
                    if (clienteId.isNullOrBlank()) {
                        return@withContext Pair(false, "No se encontró el ID del cliente")
                    }
                    val token = rutinaRepository.session.getToken()
                    if (token.isNullOrBlank()) {
                        return@withContext Pair(false, "No hay sesión activa")
                    }

                    when (val response = rutinaRepository.obtenerRutinaPorId(rutinaId)) {
                        is ApiResponse.Success -> {
                            val request = CrearRutinaRequest(
                                nombre = response.data.nombre,
                                cliente_id = clienteId,
                                dias = response.data.dias,
                                activa = true,
                                publica = false
                            )
                            when (val cloneResponse = rutinaRepository.crearRutina(request)) {
                                is ApiResponse.Success -> {
                                    loadRutinas(clienteId)
                                    Pair(true, null)
                                }
                                is ApiResponse.Error -> Pair(false, "Error al clonar (${cloneResponse.code}): ${cloneResponse.message}")
                                is ApiResponse.Exception -> Pair(false, "Error de red al clonar")
                            }
                        }
                        is ApiResponse.Error -> Pair(false, "Error del servidor (${response.code}): ${response.message}")
                        is ApiResponse.Exception -> Pair(false, "Error de red")
                    }
                } catch (e: Exception) {
                    Pair(false, "Error: ${e.message}")
                }
            }
            onResult(result.first, result.second)
        }
    }

    var historialEntrenamientos by mutableStateOf<List<EntrenamientoLog>>(emptyList())
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
                val token = rutinaRepository.session.getToken()
                val clienteId = rutinaRepository.session.getClienteId()
                if (token.isNullOrBlank() || clienteId.isNullOrBlank()) {
                    errorHistorial = "No se encontró sesión o ID de cliente"
                    return@launch
                }
                
                when (val response = rutinaRepository.obtenerHistorialEntrenamientos(clienteId)) {
                    is ApiResponse.Success -> {
                        Log.d("HISTORIAL", "Historial obtenido: ${response.data.size}")
                        historialEntrenamientos = response.data
                        errorHistorial = null
                    }
                    is ApiResponse.Error -> {
                        errorHistorial = "Error del servidor (${response.code})"
                    }
                    is ApiResponse.Exception -> {
                        errorHistorial = "Error de conexión"
                    }
                }
            } catch (e: Exception) {
                Log.e("HISTORIAL_ERROR", "Error al cargar historial", e)
                errorHistorial = e.message ?: "Error al recuperar el historial"
            } finally {
                isLoadingHistorial = false
            }
        }
    }
}
