package com.spc.nutricoach.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Extensión del Context para crear/acceder al DataStore (singleton por proceso)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

class SessionManager(private val context: Context) {

    private fun createEncryptedPrefs(ctx: Context): android.content.SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            "secure_session_prefs",
            masterKeyAlias,
            ctx,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val encryptedPrefs = try {
        createEncryptedPrefs(context)
    } catch (e: Exception) {
        e.printStackTrace()
        // Limpiar el archivo de preferencias corrupto físicamente
        try {
            val sharedPrefsFile = java.io.File(context.filesDir.parent, "shared_prefs/secure_session_prefs.xml")
            if (sharedPrefsFile.exists()) {
                sharedPrefsFile.delete()
            }
        } catch (fileEx: Exception) {
            fileEx.printStackTrace()
        }
        // Intentar crearlo de nuevo tras limpiar el archivo
        try {
            createEncryptedPrefs(context)
        } catch (e2: Exception) {
            e2.printStackTrace()
            // Fallback a SharedPreferences comunes para evitar el crash de la app si el Keystore está roto
            context.getSharedPreferences("secure_session_prefs_fallback", Context.MODE_PRIVATE)
        }
    }

    companion object {
        private val KEY_ROLE  = stringPreferencesKey("user_role")
        private val KEY_CLIENT_ID = stringPreferencesKey("client_id")
        private val KEY_EMAIL = stringPreferencesKey("user_email")
    }

    suspend fun saveSession(token: String, role: String, clienteId: String, email: String) {
        // Persistir el token de forma segura en EncryptedSharedPreferences
        encryptedPrefs.edit().putString("auth_token", token).apply()

        // El resto de la información no sensible permanece en DataStore
        context.dataStore.edit { prefs ->
            prefs[KEY_ROLE]  = role
            prefs[KEY_CLIENT_ID] = clienteId
            prefs[KEY_EMAIL] = email
        }
    }

    suspend fun clearSession() {
        // Limpiar el token cifrado
        encryptedPrefs.edit().remove("auth_token").apply()

        // Limpiar metadatos en DataStore
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_ROLE)
            prefs.remove(KEY_CLIENT_ID)
            prefs.remove(KEY_EMAIL)
        }
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map {
        !encryptedPrefs.getString("auth_token", null).isNullOrBlank()
    }

    suspend fun getToken(): String? = encryptedPrefs.getString("auth_token", null)

    suspend fun getRole(): String? = context.dataStore.data.first()[KEY_ROLE]

    suspend fun getClienteId(): String? = context.dataStore.data.first()[KEY_CLIENT_ID]

    suspend fun getEmail(): String? = context.dataStore.data.first()[KEY_EMAIL]
    
    val userEmailFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_EMAIL]
    }

    val clienteIdFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_CLIENT_ID]
    }
}
