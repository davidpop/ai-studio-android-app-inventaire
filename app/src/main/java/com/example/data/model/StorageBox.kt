package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "boxes",
    indices = [Index(value = ["code"], unique = true), Index(value = ["locationId"])]
)
data class StorageBox(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val code: String, // e.g., "BOX-0001", "B01"
    val name: String,
    val label: String, // e.g., "Bricolage", "Câbles"
    val description: String? = null,
    val locationId: Long? = null,
    val photoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
