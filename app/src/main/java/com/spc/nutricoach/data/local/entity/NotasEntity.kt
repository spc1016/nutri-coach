package com.spc.nutricoach.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notas")
data class NotasEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val clienteId: String,
    val titulo: String,
    val contenido: String,
    val fechaCreacion: Long = System.currentTimeMillis()
)
