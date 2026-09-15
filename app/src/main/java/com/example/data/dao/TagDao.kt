package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Tag
import kotlinx.coroutines.flow.Flow

data class TagWithCount(
    val id: Long,
    val name: String,
    val colorHex: String,
    val itemCount: Int
)

@Dao
interface TagDao {

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<Tag>>

    @Query("SELECT * FROM tags ORDER BY name ASC")
    suspend fun getAllTagsSync(): List<Tag>

    @Query("""
        SELECT t.id, t.name, t.colorHex, COUNT(ref.itemId) as itemCount
        FROM tags t
        LEFT JOIN item_tag_cross_ref ref ON t.id = ref.tagId
        GROUP BY t.id
        ORDER BY t.name ASC
    """)
    fun getTagsWithCount(): Flow<List<TagWithCount>>

    @Query("SELECT * FROM tags WHERE id = :id")
    fun getTagById(id: Long): Flow<Tag?>

    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getTagByIdSync(id: Long): Tag?

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): Tag?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: Tag): Long

    @Update
    suspend fun updateTag(tag: Tag)

    @Delete
    suspend fun deleteTag(tag: Tag)

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteTagById(id: Long)

    @Query("SELECT COUNT(*) FROM item_tag_cross_ref WHERE tagId = :tagId")
    suspend fun countItemsWithTag(tagId: Long): Int

    @Query("SELECT COUNT(*) FROM tags")
    suspend fun getTagCount(): Int
}
