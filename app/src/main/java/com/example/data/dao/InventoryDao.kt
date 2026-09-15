package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.ItemBoxCrossRef

@Dao
interface InventoryDao {

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM item_box_quantities")
    suspend fun getTotalInventoryQuantity(): Int

    @Query("SELECT COUNT(*) FROM items")
    suspend fun getTotalItemsCount(): Int

    @Query("SELECT COUNT(*) FROM boxes")
    suspend fun getTotalBoxesCount(): Int

    @Query("SELECT COUNT(*) FROM locations")
    suspend fun getTotalLocationsCount(): Int

    @Query("SELECT COUNT(*) FROM tags")
    suspend fun getTotalTagsCount(): Int

    @Query("SELECT * FROM item_box_quantities WHERE itemId = :itemId AND boxId = :boxId")
    suspend fun getBoxQuantity(itemId: Long, boxId: Long): ItemBoxCrossRef?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBoxQuantity(ref: ItemBoxCrossRef)

    @Query("DELETE FROM item_box_quantities WHERE itemId = :itemId AND boxId = :boxId")
    suspend fun deleteBoxQuantity(itemId: Long, boxId: Long)

    @Transaction
    suspend fun moveQuantity(itemId: Long, fromBoxId: Long, toBoxId: Long, moveQty: Int): Boolean {
        if (moveQty <= 0) return false
        val sourceRef = getBoxQuantity(itemId, fromBoxId) ?: return false
        if (sourceRef.quantity < moveQty) return false

        val newSourceQty = sourceRef.quantity - moveQty
        if (newSourceQty > 0) {
            insertOrUpdateBoxQuantity(ItemBoxCrossRef(itemId, fromBoxId, newSourceQty))
        } else {
            deleteBoxQuantity(itemId, fromBoxId)
        }

        val targetRef = getBoxQuantity(itemId, toBoxId)
        val newTargetQty = (targetRef?.quantity ?: 0) + moveQty
        insertOrUpdateBoxQuantity(ItemBoxCrossRef(itemId, toBoxId, newTargetQty))
        return true
    }

    @Transaction
    suspend fun adjustQuantityInBox(itemId: Long, boxId: Long, delta: Int): Int {
        val current = getBoxQuantity(itemId, boxId)
        val currentQty = current?.quantity ?: 0
        val newQty = currentQty + delta
        if (newQty <= 0) {
            deleteBoxQuantity(itemId, boxId)
            return 0
        } else {
            insertOrUpdateBoxQuantity(ItemBoxCrossRef(itemId, boxId, newQty))
            return newQty
        }
    }
}
