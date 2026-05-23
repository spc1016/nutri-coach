package com.spc.nutricoach.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spc.nutricoach.data.NutriCoachApiClient
import com.spc.nutricoach.data.SendCodeRequest
import com.spc.nutricoach.data.VerifyCodeRequest
import com.spc.nutricoach.data.ResendCodeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

enum class RegistroStep { FORM, VERIFICATION }

class RegistroViewModel(application: Application) : AndroidViewModel(application) {

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

    private fun isValidGmail(email: String): Boolean {
        return email.matches(Regex("^[a-zA-Z0-9._%+-]+@gmail\\.com$"))
    }

    fun sendCode() {
        if (nombre.isBlank() || email.isBlank() || password.isBlank()) {
            statusMessage = "Nombre, email y contraseña son obligatorios"
            isError = true
            return
        }
        if (!isValidGmail(email.trim())) {
            statusMessage = "Introduce un email de Gmail válido (@gmail.com)"
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
                val response = NutriCoachApiClient.service.sendVerificationCode(request)
                Log.d("REGISTRO", "Código enviado: ${response.message}")
                statusMessage = "Código enviado a ${email.trim()}"
                isError = false
                currentStep = RegistroStep.VERIFICATION
                startResendCooldown()
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string() ?: ""
                Log.e("REGISTRO_ERROR", "HTTP ${e.code()}: $errorBody")
                statusMessage = when (e.code()) {
                    400 -> if (errorBody.contains("registrado")) "Este email ya está registrado"
                           else if (errorBody.contains("gmail")) "Introduce un email de Gmail válido"
                           else "Datos inválidos"
                    500 -> "Error al enviar el correo. Inténtalo de nuevo."
                    else -> "Error del servidor (${e.code()})"
                }
                isError = true
            } catch (e: IOException) {
                Log.e("REGISTRO_ERROR", "Error de conexión/red en sendCode", e)
                statusMessage = "Error de conexión: ${e.localizedMessage ?: "Comprueba tu red"}"
                isError = true
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
                val response = NutriCoachApiClient.service.verifyCode(request)
                Log.d("REGISTRO", "Registro OK - id: ${response.id}")
                statusMessage = "¡Registro exitoso! Ya puedes iniciar sesión."
                isError = false
                registroSuccess = true
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string() ?: ""
                Log.e("REGISTRO_ERROR", "HTTP ${e.code()}: $errorBody")
                statusMessage = when {
                    errorBody.contains("expirado") -> "El código ha expirado. Solicita uno nuevo."
                    errorBody.contains("incorrecto") || errorBody.contains("inválido") -> "Código incorrecto. Inténtalo de nuevo."
                    else -> "Error del servidor (${e.code()})"
                }
                isError = true
            } catch (e: IOException) {
                Log.e("REGISTRO_ERROR", "Error de conexión/red en verifyCode", e)
                statusMessage = "Error de conexión: ${e.localizedMessage ?: "Comprueba tu red"}"
                isError = true
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
                val response = NutriCoachApiClient.service.resendCode(request)
                Log.d("REGISTRO", "Código reenviado: ${response.message}")
                statusMessage = "Nuevo código enviado a ${email.trim()}"
                isError = false
                verificationCode = ""
                startResendCooldown()
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string() ?: ""
                Log.e("REGISTRO_ERROR", "HTTP ${e.code()}: $errorBody")
                statusMessage = "Error al reenviar código (${e.code()})"
                isError = true
            } catch (e: IOException) {
                Log.e("REGISTRO_ERROR", "Error de conexión/red en resendCode", e)
                statusMessage = "Error de conexión: ${e.localizedMessage ?: "Comprueba tu red"}"
                isError = true
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
