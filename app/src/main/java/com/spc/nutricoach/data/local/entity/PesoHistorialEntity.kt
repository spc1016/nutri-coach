package com.spc.nutricoach.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "peso_historial")
data class PesoHistorialEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val clienteId: String,
    val rutinaId: String,
    val exerciseKey: String,
    val peso: String,
    val fecha: Long = System.currentTimeMillis()
)
