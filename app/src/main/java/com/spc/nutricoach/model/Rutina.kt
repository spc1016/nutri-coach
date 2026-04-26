package com.spc.nutricoach.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonPrimitive

object FlexibleStringSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FlexibleString", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: String) = encoder.encodeString(value)
    override fun deserialize(decoder: Decoder): String {
        return if (decoder is JsonDecoder) {
            decoder.decodeJsonElement().jsonPrimitive.content
        } else {
            decoder.decodeString()
        }
    }
}

object FlexibleNullableStringSerializer : KSerializer<String?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FlexibleNullableString", PrimitiveKind.STRING)
    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: String?) {
        if (value != null) encoder.encodeString(value) else encoder.encodeNull()
    }
    override fun deserialize(decoder: Decoder): String? {
        return if (decoder is JsonDecoder) {
            val element = decoder.decodeJsonElement()
            if (element is kotlinx.serialization.json.JsonNull) null else element.jsonPrimitive.content
        } else {
            decoder.decodeString()
        }
    }
}

@Serializable
data class Ejercicio(
    @SerialName("ejercicio_id") val ejercicioId: String? = null,
    @SerialName("nombre_snapshot") val nombreSnapshot: String = "",
    val series: Int = 0,
    @Serializable(with = FlexibleStringSerializer::class) val repeticiones: String = "",
    @Serializable(with = FlexibleNullableStringSerializer::class) @SerialName("rir_objetivo") val rir: String? = null,
    @Serializable(with = FlexibleNullableStringSerializer::class) @SerialName("descanso_segundos") val descanso: String? = null,
    val notas: String? = null
)

@Serializable
data class Dia(
    val nombre: String = "",
    val orden: Int = 0,
    val enfoque: String? = null,
    val ejercicios: List<Ejercicio> = emptyList()
)

@Serializable
data class Rutina(
    @SerialName("_id") val id: String = "",
    @SerialName("cliente_id") val clienteId: String = "",
    val nombre: String = "",
    @SerialName("fecha_asignacion") val fechaAsignacion: String? = null,
    val activa: Boolean = true,
    val publica: Boolean = false,
    @SerialName("notas_generales") val notasGenerales: String? = null,
    val dias: List<Dia> = emptyList()
)
