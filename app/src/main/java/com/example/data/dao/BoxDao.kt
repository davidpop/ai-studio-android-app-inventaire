package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.ItemBoxCrossRef
import com.example.data.model.StorageBox
import kotlinx.coroutines.flow.Flow

@Dao
interface BoxDao {

    @Query("SELECT * FROM boxes ORDER BY name ASC")
    fun getAllBoxes(): Flow<List<StorageBox>>

    @Query("SELECT * FROM boxes ORDER BY name ASC")
    suspend fun getAllBoxesSync(): List<StorageBox>

    @Query("SELECT * FROM boxes WHERE id = :id")
    fun getBoxById(id: Long): Flow<StorageBox?>

    @Query("SELECT * FROM boxes WHERE id = :id")
    suspend fun getBoxByIdSync(id: Long): StorageBox?

    @Query("SELECT * FROM boxes WHERE locationId = :locationId ORDER BY name ASC")
    fun getBoxesByLocation(locationId: Long): Flow<List<StorageBox>>

    @Query("SELECT * FROM boxes WHERE locationId = :locationId ORDER BY name ASC")
    suspend fun getBoxesByLocationSync(locationId: Long): List<StorageBox>

    @Query("SELECT * FROM boxes ORDER BY updatedAt DESC LIMIT :limit")
    fun getRecentBoxes(limit: Int = 5): Flow<List<StorageBox>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBox(box: StorageBox): Long

    @Update
    suspend fun updateBox(box: StorageBox)

    @Delete
    suspend fun deleteBox(box: StorageBox)

    @Query("DELETE FROM boxes WHERE id = :id")
    suspend fun deleteBoxById(id: Long)

    @Query("SELECT COUNT(*) FROM item_box_quantities WHERE boxId = :boxId AND quantity > 0")
    suspend fun countDistinctItemsInBox(boxId: Long): Int

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM item_box_quantities WHERE boxId = :boxId")
    suspend fun countTotalQuantityInBox(boxId: Long): Int

    @Query("SELECT * FROM item_box_quantities WHERE boxId = :boxId AND quantity > 0")
    fun getItemQuantitiesForBox(boxId: Long): Flow<List<ItemBoxCrossRef>>

    @Query("SELECT * FROM item_box_quantities WHERE boxId = :boxId AND quantity > 0")
    suspend fun getItemQuantitiesForBoxSync(boxId: Long): List<ItemBoxCrossRef>

    @Query("SELECT COUNT(*) FROM boxes")
    suspend fun getBoxCount(): Int

    @Query("SELECT code FROM boxes ORDER BY id DESC LIMIT 1")
    suspend fun getLastBoxCode(): String?

    @Transaction
    suspend fun reassignAllItemsToBox(fromBoxId: Long, toBoxId: Long) {
        val items = getItemQuantitiesForBoxSync(fromBoxId)
        for (item in items) {
            val existing = getItemQuantitySync(item.itemId, toBoxId)
            val newQty = (existing?.quantity ?: 0) + item.quantity
            insertOrUpdateBoxQuantity(ItemBoxCrossRef(item.itemId, toBoxId, newQty))
            deleteBoxQuantity(item.itemId, fromBoxId)
        }
    }

    @Query("SELECT * FROM item_box_quantities WHERE itemId = :itemId AND boxId = :boxId")
    suspend fun getItemQuantitySync(itemId: Long, boxId: Long): ItemBoxCrossRef?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBoxQuantity(ref: ItemBoxCrossRef)

    @Query("DELETE FROM item_box_quantities WHERE itemId = :itemId AND boxId = :boxId")
    suspend fun deleteBoxQuantity(itemId: Long, boxId: Long)
}
