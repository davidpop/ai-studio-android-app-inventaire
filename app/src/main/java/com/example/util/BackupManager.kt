package com.example.util

import android.content.Context
import com.example.data.database.AppDatabase
import com.example.data.model.Item
import com.example.data.model.ItemBoxCrossRef
import com.example.data.model.ItemPhoto
import com.example.data.model.ItemTagCrossRef
import com.example.data.model.Location
import com.example.data.model.StorageBox
import com.example.data.model.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class BackupManager(
    private val context: Context,
    private val database: AppDatabase
) {
    suspend fun exportToJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        // Locations
        val locations = database.locationDao().getAllLocationsSync()
        val locArray = JSONArray()
        locations.forEach { loc ->
            val obj = JSONObject().apply {
                put("id", loc.id)
                put("name", loc.name)
                put("parentId", loc.parentId ?: JSONObject.NULL)
                put("description", loc.description ?: JSONObject.NULL)
            }
            locArray.put(obj)
        }
        root.put("locations", locArray)

        // Boxes
        val boxes = database.boxDao().getAllBoxesSync()
        val boxArray = JSONArray()
        boxes.forEach { box ->
            val obj = JSONObject().apply {
                put("id", box.id)
                put("code", box.code)
                put("name", box.name)
                put("label", box.label)
                put("description", box.description ?: JSONObject.NULL)
                put("locationId", box.locationId ?: JSONObject.NULL)
                put("photoUri", box.photoUri ?: JSONObject.NULL)
            }
            boxArray.put(obj)
        }
        root.put("boxes", boxArray)

        // Tags
        val tags = database.tagDao().getAllTagsSync()
        val tagArray = JSONArray()
        tags.forEach { tag ->
            val obj = JSONObject().apply {
                put("id", tag.id)
                put("name", tag.name)
                put("colorHex", tag.colorHex)
            }
            tagArray.put(obj)
        }
        root.put("tags", tagArray)

        // Items
        val items = database.itemDao().getAllItemsSync()
        val itemArray = JSONArray()
        items.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("description", item.description ?: JSONObject.NULL)
                put("category", item.category ?: JSONObject.NULL)
                put("primaryPhotoUri", item.primaryPhotoUri ?: JSONObject.NULL)
            }
            itemArray.put(obj)
        }
        root.put("items", itemArray)

        // Item-Box Quantities
        val boxRefs = database.itemDao().getAllItemBoxQuantitiesSync()
        val qtyArray = JSONArray()
        boxRefs.forEach { ref ->
            val obj = JSONObject().apply {
                put("itemId", ref.itemId)
                put("boxId", ref.boxId)
                put("quantity", ref.quantity)
            }
            qtyArray.put(obj)
        }
        root.put("boxQuantities", qtyArray)

        // Item-Tag Refs
        val tagRefs = database.itemDao().getAllItemTagRefsSync()
        val refArray = JSONArray()
        tagRefs.forEach { ref ->
            val obj = JSONObject().apply {
                put("itemId", ref.itemId)
                put("tagId", ref.tagId)
            }
            refArray.put(obj)
        }
        root.put("tagRefs", refArray)

        root.toString(2)
    }

    suspend fun importFromJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)

            // Parse locations
            val locArray = root.optJSONArray("locations") ?: JSONArray()
            val locations = mutableListOf<Location>()
            for (i in 0 until locArray.length()) {
                val obj = locArray.getJSONObject(i)
                locations.add(
                    Location(
                        id = obj.getLong("id"),
                        name = obj.getString("name"),
                        parentId = if (obj.isNull("parentId")) null else obj.getLong("parentId"),
                        description = if (obj.isNull("description")) null else obj.getString("description")
                    )
                )
            }

            // Parse boxes
            val boxArray = root.optJSONArray("boxes") ?: JSONArray()
            val boxes = mutableListOf<StorageBox>()
            for (i in 0 until boxArray.length()) {
                val obj = boxArray.getJSONObject(i)
                boxes.add(
                    StorageBox(
                        id = obj.getLong("id"),
                        code = obj.getString("code"),
                        name = obj.getString("name"),
                        label = obj.getString("label"),
                        description = if (obj.isNull("description")) null else obj.getString("description"),
                        locationId = if (obj.isNull("locationId")) null else obj.getLong("locationId"),
                        photoUri = if (obj.isNull("photoUri")) null else obj.getString("photoUri")
                    )
                )
            }

            // Parse tags
            val tagArray = root.optJSONArray("tags") ?: JSONArray()
            val tags = mutableListOf<Tag>()
            for (i in 0 until tagArray.length()) {
                val obj = tagArray.getJSONObject(i)
                tags.add(
                    Tag(
                        id = obj.getLong("id"),
                        name = obj.getString("name"),
                        colorHex = obj.optString("colorHex", "#3B82F6")
                    )
                )
            }

            // Parse items
            val itemArray = root.optJSONArray("items") ?: JSONArray()
            val items = mutableListOf<Item>()
            for (i in 0 until itemArray.length()) {
                val obj = itemArray.getJSONObject(i)
                items.add(
                    Item(
                        id = obj.getLong("id"),
                        name = obj.getString("name"),
                        description = if (obj.isNull("description")) null else obj.getString("description"),
                        category = if (obj.isNull("category")) null else obj.getString("category"),
                        primaryPhotoUri = if (obj.isNull("primaryPhotoUri")) null else obj.getString("primaryPhotoUri")
                    )
                )
            }

            // Parse boxQuantities
            val qtyArray = root.optJSONArray("boxQuantities") ?: JSONArray()
            val boxRefs = mutableListOf<ItemBoxCrossRef>()
            for (i in 0 until qtyArray.length()) {
                val obj = qtyArray.getJSONObject(i)
                boxRefs.add(
                    ItemBoxCrossRef(
                        itemId = obj.getLong("itemId"),
                        boxId = obj.getLong("boxId"),
                        quantity = obj.getInt("quantity")
                    )
                )
            }

            // Parse tagRefs
            val refArray = root.optJSONArray("tagRefs") ?: JSONArray()
            val tagRefs = mutableListOf<ItemTagCrossRef>()
            for (i in 0 until refArray.length()) {
                val obj = refArray.getJSONObject(i)
                tagRefs.add(
                    ItemTagCrossRef(
                        itemId = obj.getLong("itemId"),
                        tagId = obj.getLong("tagId")
                    )
                )
            }

            // Write all to database
            database.clearAllTables()
            locations.forEach { database.locationDao().insertLocation(it) }
            boxes.forEach { database.boxDao().insertBox(it) }
            tags.forEach { database.tagDao().insertTag(it) }
            items.forEach { database.itemDao().insertItem(it) }
            boxRefs.forEach { database.itemDao().insertOrUpdateBoxQuantity(it) }
            tagRefs.forEach { database.itemDao().insertItemTagRef(it) }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
