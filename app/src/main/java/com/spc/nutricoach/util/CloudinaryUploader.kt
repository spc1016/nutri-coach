package com.spc.nutricoach.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

object CloudinaryUploader {
    
    // Configurar OkHttpClient con tiempos de espera ampliados (30 segundos) para evitar falsos negativos en conexiones lentas
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun uploadImage(
        context: Context,
        imageUri: Uri,
        cloudName: String,
        uploadPreset: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            // 1. Comprimir y optimizar la imagen antes de subirla
            // Esto reduce radicalmente el tamaño del archivo (de 10MB a ~250KB), ahorrando datos y previniendo Timeouts.
            val bytes = compressImage(context, imageUri) ?: return@withContext null

            // Generar un nombre único basado en un timestamp
            val timestamp = System.currentTimeMillis()
            val fileName = "nutricoach_post_$timestamp.jpg"

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    fileName,
                    bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                )
                .addFormDataPart("upload_preset", uploadPreset)
                .build()

            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val jsonString = response.body?.string() ?: ""
                    val jsonObject = JSONObject(jsonString)
                    val secureUrl = jsonObject.optString("secure_url", null)
                    Log.d("CloudinaryUploader", "Subida exitosa a Cloudinary: $secureUrl")
                    secureUrl
                } else {
                    val errorBody = response.body?.string() ?: ""
                    Log.e("CloudinaryUploader", "Fallo al subir a Cloudinary: ${response.code} - $errorBody")
                    null
                }
            }
        } catch (e: IOException) {
            Log.e("CloudinaryUploader", "Error de red durante la subida a Cloudinary (posible timeout)", e)
            null
        } catch (e: Exception) {
            Log.e("CloudinaryUploader", "Error inesperado durante la subida", e)
            null
        }
    }

    /**
     * Comprime la imagen a una resolución máxima de 1280px y calidad JPEG del 80%.
     * Corrige además cualquier rotación incorrecta proveniente de la cámara.
     */
    private fun compressImage(context: Context, imageUri: Uri): ByteArray? {
        return try {
            val contentResolver = context.contentResolver
            
            // 1. Obtener dimensiones originales sin cargar la imagen entera en memoria (previene OutOfMemoryError)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            contentResolver.openInputStream(imageUri).use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }
            
            val originalWidth = options.outWidth
            val originalHeight = options.outHeight
            if (originalWidth <= 0 || originalHeight <= 0) return null
            
            // 2. Calcular inSampleSize para escalar la imagen a un tamaño razonable (máx 1280px)
            val targetMaxDim = 1280
            var inSampleSize = 1
            if (originalWidth > targetMaxDim || originalHeight > targetMaxDim) {
                val halfWidth = originalWidth / 2
                val halfHeight = originalHeight / 2
                while ((halfWidth / inSampleSize) >= targetMaxDim || (halfHeight / inSampleSize) >= targetMaxDim) {
                    inSampleSize *= 2
                }
            }
            
            // 3. Cargar el Bitmap con el tamaño escalado
            val scaleOptions = BitmapFactory.Options().apply {
                inSampleSize = inSampleSize
            }
            val scaledBitmap = contentResolver.openInputStream(imageUri).use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, scaleOptions)
            } ?: return null
            
            // 4. Corregir rotación EXIF (muy común en fotos sacadas con la cámara del dispositivo)
            val rotatedBitmap = corregirRotacionBitmap(context, imageUri, scaledBitmap)

            // 5. Comprimir a formato JPEG con un 80% de calidad
            val outputStream = ByteArrayOutputStream()
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val compressedBytes = outputStream.toByteArray()
            
            // Liberar memoria
            if (rotatedBitmap != scaledBitmap) {
                rotatedBitmap.recycle()
            }
            scaledBitmap.recycle()
            
            Log.d("CloudinaryUploader", "Imagen comprimida. Tamaño original: ${originalWidth}x${originalHeight}. Tamaño comprimido: ${compressedBytes.size / 1024} KB")
            compressedBytes
        } catch (e: Exception) {
            Log.e("CloudinaryUploader", "Error comprimiendo imagen", e)
            null
        }
    }

    /**
     * Lee los metadatos EXIF de la imagen para restaurar su orientación original si fue rotada por el sensor físico.
     */
    private fun corregirRotacionBitmap(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return bitmap
            val exifInterface = ExifInterface(inputStream)
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            inputStream.close()
            
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                else -> return bitmap
            }
            
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            Log.e("CloudinaryUploader", "Error al corregir rotación EXIF", e)
            bitmap
        }
    }
}
