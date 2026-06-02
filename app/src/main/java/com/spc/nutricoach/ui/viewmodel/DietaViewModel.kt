package com.spc.nutricoach.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spc.nutricoach.data.*
import com.spc.nutricoach.data.repository.DietaRepository
import com.spc.nutricoach.model.Comida
import com.spc.nutricoach.model.Dieta
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DietaViewModel @Inject constructor(
    private val dietaRepository: DietaRepository,
    private val dietTrackerManager: DietTrackerManager
) : ViewModel() {

    val completedMealsFlow = dietTrackerManager.completedMealsFlow

    var dietas by mutableStateOf<List<Dieta>>(emptyList())
        private set

    var dietasPublicas by mutableStateOf<List<Dieta>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var lastLoadedClientId by mutableStateOf<String?>(null)
        private set

    fun cargarDietas(force: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            val clienteId = dietaRepository.session.getClienteId()
            if (!force && dietas.isNotEmpty() && lastLoadedClientId == clienteId && !clienteId.isNullOrBlank()) {
                return@launch
            }
            isLoading = true
            error = null
            if (lastLoadedClientId != clienteId) {
                dietas = emptyList()
            }
            loadDietas(clienteId)
        }
    }

    suspend fun loadDietas(clienteId: String?) {
        try {
            if (clienteId.isNullOrBlank()) {
                error = "No se encontró el ID del cliente"
                isLoading = false
                return
            }
            val token = dietaRepository.session.getToken()
            if (token.isNullOrBlank()) {
                error = "No hay sesión activa"
                isLoading = false
                return
            }
            
            when (val response = dietaRepository.obtenerDietasCliente(clienteId)) {
                is ApiResponse.Success -> {
                    Log.d("DIETAS", "Dietas obtenidas: ${response.data.size}")
                    dietas = response.data
                    lastLoadedClientId = clienteId
                    error = null
                }
                is ApiResponse.Error -> {
                    Log.e("DIETAS_ERROR", "Error HTTP ${response.code}: ${response.message}")
                    error = "Error del servidor (${response.code})"
                }
                is ApiResponse.Exception -> {
                    Log.e("DIETAS_ERROR", "Excepción de red", response.throwable)
                    error = "Error de conexión"
                }
            }
        } catch (e: Exception) {
            Log.e("DIETAS_ERROR", "Error inesperado: ${e.message}", e)
            error = "Error inesperado: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    fun toggleMeal(dietaId: String, comidaNombre: String, isCompleted: Boolean) {
        viewModelScope.launch {
            val clienteId = dietaRepository.session.getClienteId() ?: return@launch
            val key = "${clienteId}_${dietaId}_${comidaNombre}"
            if (isCompleted) {
                dietTrackerManager.markMealCompleted(key)
            } else {
                dietTrackerManager.unmarkMealCompleted(key)
            }
        }
    }

    fun clearDietMeals(dietaId: String, comidas: List<Comida>) {
        viewModelScope.launch {
            val clienteId = dietaRepository.session.getClienteId() ?: return@launch
            comidas.forEach { comida ->
                val key = "${clienteId}_${dietaId}_${comida.nombre}"
                dietTrackerManager.unmarkMealCompleted(key)
            }
        }
    }

    fun cargarDietasPublicas(force: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!force && dietasPublicas.isNotEmpty()) {
                return@launch
            }
            isLoading = true
            error = null
            try {
                val token = dietaRepository.session.getToken()
                if (token.isNullOrBlank()) {
                    error = "No hay sesión activa"
                    return@launch
                }
                
                when (val response = dietaRepository.obtenerDietasPublicas()) {
                    is ApiResponse.Success -> {
                        Log.d("DIETAS_PUBLICAS", "Dietas públicas obtenidas: ${response.data.size}")
                        dietasPublicas = response.data
                        error = null
                    }
                    is ApiResponse.Error -> {
                        Log.e("DIETAS_PUB_ERROR", "Error HTTP ${response.code}: ${response.message}")
                        error = "Error del servidor (${response.code})"
                    }
                    is ApiResponse.Exception -> {
                        Log.e("DIETAS_PUB_ERROR", "Excepción de red", response.throwable)
                        error = "Error de conexión"
                    }
                }
            } catch (e: Exception) {
                Log.e("DIETAS_PUB_ERROR", "Error inesperado: ${e.message}", e)
                error = "Error inesperado: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun cargarDietaPorId(dietaId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (dietas.any { it.id == dietaId } || dietasPublicas.any { it.id == dietaId }) {
                    return@launch
                }

                val token = dietaRepository.session.getToken()
                if (token.isNullOrBlank()) return@launch

                isLoading = true
                when (val response = dietaRepository.obtenerDietaPorId(dietaId)) {
                    is ApiResponse.Success -> {
                        dietasPublicas = dietasPublicas + response.data
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("DIETAS_API", "Error al cargar dieta por ID", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun clonarDietaPorId(dietaId: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.Main) {
            val result = kotlinx.coroutines.withContext(Dispatchers.IO) {
                try {
                    val clienteId = dietaRepository.session.getClienteId()
                    if (clienteId.isNullOrBlank()) {
                        return@withContext Pair(false, "No se encontró el ID del cliente")
                    }
                    val token = dietaRepository.session.getToken()
                    if (token.isNullOrBlank()) {
                        return@withContext Pair(false, "No hay sesión activa")
                    }

                    when (val response = dietaRepository.obtenerDietaPorId(dietaId)) {
                        is ApiResponse.Success -> {
                            val request = CrearDietaRequest(
                                nombre = response.data.nombre,
                                cliente_id = clienteId,
                                kcal_objetivo = response.data.kcalObjetivo ?: 0,
                                comidas = response.data.comidas,
                                activa = true,
                                publica = false
                            )
                            when (val cloneResponse = dietaRepository.crearDieta(request)) {
                                is ApiResponse.Success -> {
                                    loadDietas(clienteId)
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

    fun crearDieta(nombre: String, kcalObjetivo: Int, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val clienteId = dietaRepository.session.getClienteId()
                if (clienteId.isNullOrBlank()) {
                    onResult(false, "No se encontró el ID del cliente")
                    return@launch
                }

                val token = dietaRepository.session.getToken()
                if (token.isNullOrBlank()) {
                    onResult(false, "No hay sesión activa")
                    return@launch
                }

                val request = CrearDietaRequest(
                    nombre = nombre,
                    cliente_id = clienteId,
                    kcal_objetivo = kcalObjetivo
                )

                when (val response = dietaRepository.crearDieta(request)) {
                    is ApiResponse.Success -> {
                        loadDietas(clienteId)
                        onResult(true, null)
                    }
                    is ApiResponse.Error -> {
                        onResult(false, "Error del servidor (${response.code})")
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

    fun agregarComida(dietaId: String, nombre: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = dietaRepository.session.getToken()
                if (token.isNullOrBlank()) {
                    onResult(false, "No hay sesión activa")
                    return@launch
                }

                val request = AgregarComidaRequest(nombre = nombre)
                when (val response = dietaRepository.agregarComidaADieta(dietaId, request)) {
                    is ApiResponse.Success -> {
                        val clienteId = dietaRepository.session.getClienteId()
                        loadDietas(clienteId)
                        onResult(true, null)
                    }
                    is ApiResponse.Error -> {
                        onResult(false, "Error del servidor (${response.code}): ${response.message}")
                    }
                    is ApiResponse.Exception -> {
                        onResult(false, "Error de conexión: ${response.throwable.message}")
                    }
                }
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }

    fun agregarAlimento(
        dietaId: String,
        comidaIndex: Int,
        nombre: String,
        cantidad: Double,
        unidad: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = dietaRepository.session.getToken()
                if (token.isNullOrBlank()) {
                    onResult(false, "No hay sesión activa")
                    return@launch
                }

                val request = AgregarAlimentoRequest(
                    alimento_id = "000000000000000000000000",
                    nombre_snapshot = nombre,
                    cantidad = cantidad,
                    unidad = unidad
                )

                when (val response = dietaRepository.agregarAlimentoAComida(dietaId, comidaIndex, request)) {
                    is ApiResponse.Success -> {
                        val clienteId = dietaRepository.session.getClienteId()
                        loadDietas(clienteId)
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

    fun toggleDietaPublica(
        dietaId: String,
        isPublica: Boolean,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = dietaRepository.session.getToken()
                if (token.isNullOrBlank()) {
                    onResult(false, "No hay sesión activa")
                    return@launch
                }

                val request = ModificarDietaRequest(publica = isPublica)
                when (val response = dietaRepository.modificarDieta(dietaId, request)) {
                    is ApiResponse.Success -> {
                        dietas = dietas.map { if (it.id == dietaId) it.copy(publica = isPublica) else it }
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

    fun modificarNombreDieta(dietaId: String, nombre: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = dietaRepository.session.getToken() ?: return@launch
                val request = ModificarDietaRequest(nombre = nombre)
                when (val response = dietaRepository.modificarDieta(dietaId, request)) {
                    is ApiResponse.Success -> {
                        dietas = dietas.map { if (it.id == dietaId) it.copy(nombre = nombre) else it }
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

    fun eliminarDieta(dietaId: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = dietaRepository.session.getToken() ?: return@launch
                when (val response = dietaRepository.eliminarDieta(dietaId)) {
                    is ApiResponse.Success -> {
                        dietas = dietas.filterNot { it.id == dietaId }
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

    fun eliminarComida(dietaId: String, comidaIndex: Int, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = dietaRepository.session.getToken() ?: return@launch
                when (val response = dietaRepository.eliminarComida(dietaId, comidaIndex)) {
                    is ApiResponse.Success -> {
                        val clienteId = dietaRepository.session.getClienteId()
                        loadDietas(clienteId)
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

    fun eliminarAlimento(dietaId: String, comidaIndex: Int, alimentoIndex: Int, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = dietaRepository.session.getToken() ?: return@launch
                when (val response = dietaRepository.eliminarAlimento(dietaId, comidaIndex, alimentoIndex)) {
                    is ApiResponse.Success -> {
                        val clienteId = dietaRepository.session.getClienteId()
                        loadDietas(clienteId)
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
}
