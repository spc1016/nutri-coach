package com.spc.nutricoach.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spc.nutricoach.data.*
import com.spc.nutricoach.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class RegistroStep { FORM, VERIFICATION }

@HiltViewModel
class RegistroViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Form fields (step 1)
    var nombre by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")

    // Verification field (step 2)
    var verificationCode by mutableStateOf("")

    // UI state
    var currentStep by mutableStateOf(RegistroStep.FORM)
        private set
    var statusMessage by mutableStateOf("")
        private set
    var isError by mutableStateOf(false)
        private set
    var registroSuccess by mutableStateOf(false)
        private set
    var isLoading by mutableStateOf(false)
        private set

    // Resend cooldown
    var canResend by mutableStateOf(true)
        private set
    var resendCountdown by mutableStateOf(0)
        private set

    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"))
    }

    fun sendCode() {
        if (nombre.isBlank() || email.isBlank() || password.isBlank()) {
            statusMessage = "Nombre, email y contraseña son obligatorios"
            isError = true
            return
        }
        if (!isValidEmail(email.trim())) {
            statusMessage = "Introduce un email válido"
            isError = true
            return
        }
        if (password.length < 6) {
            statusMessage = "La contraseña debe tener al menos 6 caracteres"
            isError = true
            return
        }

        isLoading = true
        statusMessage = ""
        isError = false
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = SendCodeRequest(
                    nombre = nombre.trim(),
                    email = email.trim().lowercase(),
                    password = password
                )
                
                when (val response = authRepository.sendVerificationCode(request)) {
                    is ApiResponse.Success -> {
                        Log.d("REGISTRO", "Código enviado: ${response.data.message}")
                        statusMessage = "Código enviado a ${email.trim()}"
                        isError = false
                        currentStep = RegistroStep.VERIFICATION
                        startResendCooldown()
                    }
                    is ApiResponse.Error -> {
                        Log.e("REGISTRO_ERROR", "Error HTTP ${response.code}: ${response.message}")
                        statusMessage = when (response.code) {
                            400 -> if (response.message.contains("registrado")) "Este email ya está registrado"
                                   else if (response.message.contains("gmail") || response.message.contains("email")) "Introduce un email válido"
                                   else "Datos inválidos"
                            500 -> "Error al enviar el correo. Inténtalo de nuevo."
                            else -> "Error del servidor (${response.code})"
                        }
                        isError = true
                    }
                    is ApiResponse.Exception -> {
                        Log.e("REGISTRO_ERROR", "Excepción de red", response.throwable)
                        statusMessage = "Error de conexión: Comprueba tu red"
                        isError = true
                    }
                }
            } catch (e: Exception) {
                Log.e("REGISTRO_ERROR", "Error inesperado en sendCode", e)
                statusMessage = "Error inesperado: ${e.message}"
                isError = true
            } finally {
                isLoading = false
            }
        }
    }

    fun verifyCode() {
        if (verificationCode.isBlank() || verificationCode.length != 6) {
            statusMessage = "Introduce el código de 6 dígitos"
            isError = true
            return
        }

        isLoading = true
        statusMessage = ""
        isError = false
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = VerifyCodeRequest(
                    email = email.trim().lowercase(),
                    code = verificationCode.trim()
                )
                
                when (val response = authRepository.verifyCode(request)) {
                    is ApiResponse.Success -> {
                        Log.d("REGISTRO", "Registro OK - id: ${response.data.id}")
                        statusMessage = "¡Registro exitoso! Ya puedes iniciar sesión."
                        isError = false
                        registroSuccess = true
                    }
                    is ApiResponse.Error -> {
                        Log.e("REGISTRO_ERROR", "Error HTTP ${response.code}: ${response.message}")
                        statusMessage = when {
                            response.message.contains("expirado") -> "El código ha expirado. Solicita uno nuevo."
                            response.message.contains("incorrecto") || response.message.contains("inválido") -> "Código incorrecto. Inténtalo de nuevo."
                            else -> "Error del servidor (${response.code})"
                        }
                        isError = true
                    }
                    is ApiResponse.Exception -> {
                        Log.e("REGISTRO_ERROR", "Excepción de red", response.throwable)
                        statusMessage = "Error de conexión: Comprueba tu red"
                        isError = true
                    }
                }
            } catch (e: Exception) {
                Log.e("REGISTRO_ERROR", "Error inesperado en verifyCode", e)
                statusMessage = "Error inesperado: ${e.message}"
                isError = true
            } finally {
                isLoading = false
            }
        }
    }

    fun resendCode() {
        if (!canResend) return

        isLoading = true
        statusMessage = ""
        isError = false
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = ResendCodeRequest(email = email.trim().lowercase())
                
                when (val response = authRepository.resendCode(request)) {
                    is ApiResponse.Success -> {
                        Log.d("REGISTRO", "Código reenviado: ${response.data.message}")
                        statusMessage = "Nuevo código enviado a ${email.trim()}"
                        isError = false
                        verificationCode = ""
                        startResendCooldown()
                    }
                    is ApiResponse.Error -> {
                        Log.e("REGISTRO_ERROR", "Error HTTP ${response.code}: ${response.message}")
                        statusMessage = "Error al reenviar código (${response.code})"
                        isError = true
                    }
                    is ApiResponse.Exception -> {
                        Log.e("REGISTRO_ERROR", "Excepción de red", response.throwable)
                        statusMessage = "Error de conexión: Comprueba tu red"
                        isError = true
                    }
                }
            } catch (e: Exception) {
                Log.e("REGISTRO_ERROR", "Error inesperado en resendCode", e)
                statusMessage = "Error inesperado: ${e.message}"
                isError = true
            } finally {
                isLoading = false
            }
        }
    }

    fun goBackToForm() {
        currentStep = RegistroStep.FORM
        verificationCode = ""
        statusMessage = ""
        isError = false
    }

    private fun startResendCooldown() {
        canResend = false
        resendCountdown = 60
        viewModelScope.launch {
            while (resendCountdown > 0) {
                delay(1000)
                resendCountdown--
            }
            canResend = true
        }
    }
}
