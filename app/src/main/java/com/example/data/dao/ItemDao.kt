package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.Item
import com.example.data.model.ItemBoxCrossRef
import com.example.data.model.ItemPhoto
import com.example.data.model.ItemTagCrossRef
import com.example.data.model.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Query("SELECT * FROM items ORDER BY updatedAt DESC")
    fun getAllItems(): Flow<List<Item>>

    @Query("SELECT * FROM items ORDER BY updatedAt DESC LIMIT :limit")
    fun getRecentItems(limit: Int = 5): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE id = :id")
    fun getItemById(id: Long): Flow<Item?>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getItemByIdSync(id: Long): Item?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: Item): Long

    @Update
    suspend fun updateItem(item: Item)

    @Delete
    suspend fun deleteItem(item: Item)

    @Query("DELETE FROM items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("SELECT * FROM items ORDER BY updatedAt DESC")
    suspend fun getAllItemsSync(): List<Item>

    @Query("SELECT * FROM item_tag_cross_ref")
    suspend fun getAllItemTagRefsSync(): List<ItemTagCrossRef>

    @Query("SELECT * FROM item_box_quantities")
    suspend fun getAllItemBoxQuantitiesSync(): List<ItemBoxCrossRef>

    @Query("SELECT * FROM item_photos ORDER BY isPrimary DESC, createdAt DESC")
    fun getAllPhotos(): Flow<List<ItemPhoto>>

    @Query("SELECT * FROM item_tag_cross_ref")
    fun getAllItemTagRefs(): Flow<List<ItemTagCrossRef>>

    @Query("SELECT * FROM item_box_quantities")
    fun getAllItemBoxQuantities(): Flow<List<ItemBoxCrossRef>>

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM item_box_quantities")
    fun getTotalInventoryQuantity(): Flow<Int>

    // Photos
    @Query("SELECT * FROM item_photos WHERE itemId = :itemId ORDER BY isPrimary DESC, createdAt DESC")
    fun getPhotosForItem(itemId: Long): Flow<List<ItemPhoto>>

    @Query("SELECT * FROM item_photos WHERE itemId = :itemId ORDER BY isPrimary DESC, createdAt DESC")
    suspend fun getPhotosForItemSync(itemId: Long): List<ItemPhoto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: ItemPhoto): Long

    @Delete
    suspend fun deletePhoto(photo: ItemPhoto)

    @Query("DELETE FROM item_photos WHERE id = :photoId")
    suspend fun deletePhotoById(photoId: Long)

    @Query("UPDATE item_photos SET isPrimary = 0 WHERE itemId = :itemId")
    suspend fun clearPrimaryPhotos(itemId: Long)

    @Query("UPDATE item_photos SET isPrimary = 1 WHERE id = :photoId")
    suspend fun setPrimaryPhoto(photoId: Long)

    @Query("UPDATE items SET primaryPhotoUri = :photoUri, updatedAt = :updatedAt WHERE id = :itemId")
    suspend fun updateItemPrimaryPhoto(itemId: Long, photoUri: String?, updatedAt: Long = System.currentTimeMillis())

    // Tags
    @Query("""
        SELECT t.* FROM tags t
        INNER JOIN item_tag_cross_ref ref ON t.id = ref.tagId
        WHERE ref.itemId = :itemId
        ORDER BY t.name ASC
    """)
    fun getTagsForItem(itemId: Long): Flow<List<Tag>>

    @Query("""
        SELECT t.* FROM tags t
        INNER JOIN item_tag_cross_ref ref ON t.id = ref.tagId
        WHERE ref.itemId = :itemId
        ORDER BY t.name ASC
    """)
    suspend fun getTagsForItemSync(itemId: Long): List<Tag>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertItemTagRef(ref: ItemTagCrossRef)

    @Query("DELETE FROM item_tag_cross_ref WHERE itemId = :itemId AND tagId = :tagId")
    suspend fun deleteItemTagRef(itemId: Long, tagId: Long)

    @Query("DELETE FROM item_tag_cross_ref WHERE itemId = :itemId")
    suspend fun clearTagsForItem(itemId: Long)

    // Quantities & Boxes
    @Query("SELECT * FROM item_box_quantities WHERE itemId = :itemId")
    fun getBoxQuantitiesForItem(itemId: Long): Flow<List<ItemBoxCrossRef>>

    @Query("SELECT * FROM item_box_quantities WHERE itemId = :itemId")
    suspend fun getBoxQuantitiesForItemSync(itemId: Long): List<ItemBoxCrossRef>

    @Query("SELECT * FROM item_box_quantities WHERE itemId = :itemId AND boxId = :boxId")
    suspend fun getBoxQuantity(itemId: Long, boxId: Long): ItemBoxCrossRef?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBoxQuantity(ref: ItemBoxCrossRef)

    @Query("DELETE FROM item_box_quantities WHERE itemId = :itemId AND boxId = :boxId")
    suspend fun deleteBoxQuantity(itemId: Long, boxId: Long)

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM item_box_quantities WHERE itemId = :itemId")
    fun getTotalQuantityForItem(itemId: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM item_box_quantities WHERE itemId = :itemId")
    suspend fun getTotalQuantityForItemSync(itemId: Long): Int

    @Transaction
    suspend fun updateItemWithTagsAndBoxes(
        item: Item,
        tagIds: List<Long>,
        boxQuantities: Map<Long, Int> // boxId -> quantity
    ): Long {
        val itemId = if (item.id == 0L) {
            insertItem(item)
        } else {
            updateItem(item.copy(updatedAt = System.currentTimeMillis()))
            item.id
        }

        // Update tags
        clearTagsForItem(itemId)
        tagIds.forEach { tagId ->
            insertItemTagRef(ItemTagCrossRef(itemId, tagId))
        }

        // Update box quantities
        // First delete boxes not in map or quantity <= 0
        val currentBoxes = getBoxQuantitiesForItemSync(itemId)
        currentBoxes.forEach { current ->
            if (!boxQuantities.containsKey(current.boxId) || (boxQuantities[current.boxId] ?: 0) <= 0) {
                deleteBoxQuantity(itemId, current.boxId)
            }
        }
        boxQuantities.forEach { (boxId, qty) ->
            if (qty > 0) {
                insertOrUpdateBoxQuantity(ItemBoxCrossRef(itemId, boxId, qty))
            }
        }

        return itemId
    }
}
