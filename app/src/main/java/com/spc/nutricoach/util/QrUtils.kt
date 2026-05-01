package com.spc.nutricoach.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set

object QrUtils {

    private const val QR_PREFIX = "nutricoach://rutina/"

    /**
     * Genera un Bitmap con el código QR para la rutina dada.
     */
    fun generateQrBitmap(rutinaId: String, size: Int = 512): Bitmap {
        val content = "$QR_PREFIX$rutinaId"
        val hints = mapOf(
            EncodeHintType.MARGIN to 1,
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )
        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val bitmap = createBitmap(size, size)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap[x, y] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }
        return bitmap
    }

    /**
     * Extrae el ID de rutina de un contenido QR escaneado.
     * Devuelve null si el formato no coincide.
     */
    fun parseRutinaId(qrContent: String): String? {
        return if (qrContent.startsWith(QR_PREFIX)) {
            qrContent.removePrefix(QR_PREFIX).takeIf { it.isNotBlank() }
        } else {
            null
        }
    }
}
