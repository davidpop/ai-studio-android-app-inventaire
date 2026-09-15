package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "item_box_quantities",
    primaryKeys = ["itemId", "boxId"],
    foreignKeys = [
        ForeignKey(
            entity = Item::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StorageBox::class,
            parentColumns = ["id"],
            childColumns = ["boxId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["boxId"]),
        Index(value = ["itemId"])
    ]
)
data class ItemBoxCrossRef(
    val itemId: Long,
    val boxId: Long,
    val quantity: Int
)
