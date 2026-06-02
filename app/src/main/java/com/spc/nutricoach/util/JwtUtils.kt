package com.spc.nutricoach.util

import android.util.Base64
import org.json.JSONObject

object JwtUtils {

    /**
     * Extrae el campo "sub" (sujeto / ID de usuario) de un token JWT.
     * Esto asume que el token tiene formato estandar Header.Payload.Signature
     */
    fun extraerIdDeToken(token: String): String {
        val partes = token.split(".")
        if (partes.size < 2) {
            throw IllegalArgumentException("Token JWT con formato inválido")
        }
        
        var base64Payload = partes[1]
            .replace('-', '+')
            .replace('_', '/')
        
        // Agregar padding necesario para Base64
        while (base64Payload.length % 4 != 0) {
            base64Payload += "="
        }
        
        val payloadBytes = Base64.decode(base64Payload, Base64.DEFAULT)
        val payload = String(payloadBytes, kotlin.text.Charsets.UTF_8)
        val json = JSONObject(payload)
        
        return json.getString("sub")
    }
}
