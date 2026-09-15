package com.example.data.repository

import android.content.Context
import com.example.data.database.AppDatabase
import com.example.data.database.DemoDataSeeder
import com.example.data.model.BoxDistribution
import com.example.data.model.BoxWithDetails
import com.example.data.model.DashboardStats
import com.example.data.model.Item
import com.example.data.model.ItemInBox
import com.example.data.model.ItemPhoto
import com.example.data.model.ItemWithDetails
import com.example.data.model.Location
import com.example.data.model.LocationNode
import com.example.data.model.StorageBox
import com.example.data.model.Tag
import com.example.data.photo.PhotoStorageManager
import com.example.util.StringUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class InventoryRepository(
    private val database: AppDatabase,
    private val photoStorageManager: PhotoStorageManager
) {
    private val itemDao = database.itemDao()
    private val boxDao = database.boxDao()
    private val locationDao = database.locationDao()
    private val tagDao = database.tagDao()
    private val inventoryDao = database.inventoryDao()

    companion object {
        @Volatile
        private var INSTANCE: InventoryRepository? = null

        fun getInstance(context: Context): InventoryRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getInstance(context)
                val photoManager = PhotoStorageManager(context)
                val instance = InventoryRepository(db, photoManager)
                INSTANCE = instance
                instance
            }
        }

        fun formatLocationPath(locationId: Long?, locMap: Map<Long, Location>): String {
            if (locationId == null) return "Non assigné"
            val pathParts = mutableListOf<String>()
            var current: Location? = locMap[locationId]
            val visited = mutableSetOf<Long>()
            while (current != null && !visited.contains(current.id)) {
                visited.add(current.id)
                pathParts.add(0, current.name)
                current = current.parentId?.let { locMap[it] }
            }
            return if (pathParts.isEmpty()) "Non assigné" else pathParts.joinToString(" → ")
        }
    }

    suspend fun checkSeedDemoData() {
        DemoDataSeeder.seedIfEmpty(database)
    }

    suspend fun resetDemoData() {
        DemoDataSeeder.clearAll(database)
        DemoDataSeeder.seed(database)
    }

    suspend fun getLocationPath(locationId: Long?): String {
        if (locationId == null) return "Non assigné"
        val locMap = locationDao.getAllLocationsSync().associateBy { it.id }
        return formatLocationPath(locationId, locMap)
    }

    // --- Items ---
    fun getAllItemsWithDetails(): Flow<List<ItemWithDetails>> {
        val baseFlow = combine(
            itemDao.getAllItems(),
            itemDao.getAllPhotos(),
            itemDao.getAllItemTagRefs(),
            tagDao.getAllTags()
        ) { items, photos, tagRefs, tags ->
            Quadruple(items, photos, tagRefs, tags)
        }

        val boxLocationFlow = combine(
            itemDao.getAllItemBoxQuantities(),
            boxDao.getAllBoxes(),
            locationDao.getAllLocations()
        ) { boxRefs, boxes, locations ->
            Triple(boxRefs, boxes, locations)
        }

        return combine(baseFlow, boxLocationFlow) { (items, photos, tagRefs, tags), (boxRefs, boxes, locations) ->
            val photosByItem = photos.groupBy { it.itemId }
            val tagRefsByItem = tagRefs.groupBy { it.itemId }
            val boxRefsByItem = boxRefs.groupBy { it.itemId }

            val tagMap = tags.associateBy { it.id }
            val boxMap = boxes.associateBy { it.id }
            val locMap = locations.associateBy { it.id }

            items.map { item ->
                val itemPhotos = photosByItem[item.id] ?: emptyList()
                val itemTags = (tagRefsByItem[item.id] ?: emptyList()).mapNotNull { tagMap[it.tagId] }
                val itemBoxes = (boxRefsByItem[item.id] ?: emptyList()).mapNotNull { ref ->
                    val box = boxMap[ref.boxId] ?: return@mapNotNull null
                    val locPath = formatLocationPath(box.locationId, locMap)
                    BoxDistribution(box = box, quantity = ref.quantity, locationPath = locPath)
                }

                ItemWithDetails(
                    item = item,
                    photos = itemPhotos,
                    tags = itemTags,
                    boxDistributions = itemBoxes,
                    totalQuantity = itemBoxes.sumOf { it.quantity }
                )
            }
        }
    }

    fun getItemWithDetails(itemId: Long): Flow<ItemWithDetails?> {
        return getAllItemsWithDetails().map { items ->
            items.find { it.item.id == itemId }
        }
    }

    suspend fun saveItem(
        item: Item,
        tagIds: List<Long>,
        boxQuantities: Map<Long, Int>
    ): Long {
        return itemDao.updateItemWithTagsAndBoxes(item, tagIds, boxQuantities)
    }

    suspend fun deleteItem(itemId: Long) {
        val photos = itemDao.getPhotosForItemSync(itemId)
        photos.forEach { photo ->
            photoStorageManager.deletePhoto(photo.photoPath)
        }
        itemDao.deleteItemById(itemId)
    }

    // --- Photos ---
    suspend fun addPhotoToItem(itemId: Long, photoPath: String, setAsPrimary: Boolean = false): Long {
        val existingPhotos = itemDao.getPhotosForItemSync(itemId)
        val shouldBePrimary = setAsPrimary || existingPhotos.isEmpty()

        if (shouldBePrimary) {
            itemDao.clearPrimaryPhotos(itemId)
        }

        val photoId = itemDao.insertPhoto(
            ItemPhoto(
                itemId = itemId,
                photoPath = photoPath,
                isPrimary = shouldBePrimary
            )
        )

        if (shouldBePrimary) {
            itemDao.updateItemPrimaryPhoto(itemId, photoPath)
        }

        return photoId
    }

    suspend fun setPrimaryPhoto(itemId: Long, photoId: Long, photoPath: String) {
        itemDao.clearPrimaryPhotos(itemId)
        itemDao.setPrimaryPhoto(photoId)
        itemDao.updateItemPrimaryPhoto(itemId, photoPath)
    }

    suspend fun deletePhoto(itemId: Long, photoId: Long, photoPath: String) {
        itemDao.deletePhotoById(photoId)
        photoStorageManager.deletePhoto(photoPath)

        val remaining = itemDao.getPhotosForItemSync(itemId)
        if (remaining.isNotEmpty()) {
            val newPrimary = remaining.first()
            itemDao.setPrimaryPhoto(newPrimary.id)
            itemDao.updateItemPrimaryPhoto(itemId, newPrimary.photoPath)
        } else {
            itemDao.updateItemPrimaryPhoto(itemId, null)
        }
    }

    // --- Boxes ---
    fun getAllBoxesWithDetails(): Flow<List<BoxWithDetails>> {
        return combine(
            boxDao.getAllBoxes(),
            locationDao.getAllLocations(),
            itemDao.getAllItems(),
            itemDao.getAllItemBoxQuantities()
        ) { boxes, locations, items, boxRefs ->
            val locMap = locations.associateBy { it.id }
            val itemMap = items.associateBy { it.id }
            val boxRefsByBox = boxRefs.groupBy { it.boxId }

            boxes.map { box ->
                val location = box.locationId?.let { locMap[it] }
                val locationPath = formatLocationPath(box.locationId, locMap)
                val itemsInBox = (boxRefsByBox[box.id] ?: emptyList()).mapNotNull { ref ->
                    val itm = itemMap[ref.itemId] ?: return@mapNotNull null
                    ItemInBox(item = itm, quantity = ref.quantity)
                }

                BoxWithDetails(
                    box = box,
                    location = location,
                    locationPath = locationPath,
                    items = itemsInBox,
                    itemCount = itemsInBox.size,
                    totalQuantity = itemsInBox.sumOf { it.quantity }
                )
            }
        }
    }

    fun getBoxWithDetails(boxId: Long): Flow<BoxWithDetails?> {
        return getAllBoxesWithDetails().map { boxes ->
            boxes.find { it.box.id == boxId }
        }
    }

    suspend fun saveBox(box: StorageBox): Long {
        return if (box.id == 0L) {
            boxDao.insertBox(box)
        } else {
            boxDao.updateBox(box.copy(updatedAt = System.currentTimeMillis()))
            box.id
        }
    }

    suspend fun countDistinctItemsInBox(boxId: Long): Int {
        return boxDao.countDistinctItemsInBox(boxId)
    }

    suspend fun deleteBox(boxId: Long) {
        boxDao.deleteBoxById(boxId)
    }

    suspend fun reassignBoxAndThenDelete(fromBoxId: Long, toBoxId: Long) {
        boxDao.reassignAllItemsToBox(fromBoxId, toBoxId)
        boxDao.deleteBoxById(fromBoxId)
    }

    suspend fun generateNextBoxCode(): String {
        val lastCode = boxDao.getLastBoxCode()
        val nextNumber = if (lastCode != null && lastCode.startsWith("BOX-")) {
            val numStr = lastCode.removePrefix("BOX-")
            (numStr.toIntOrNull() ?: 0) + 1
        } else {
            val count = boxDao.getBoxCount() + 1
            count
        }
        return "BOX-%04d".format(nextNumber)
    }

    // --- Quantities Manipulation ---
    suspend fun moveQuantity(itemId: Long, fromBoxId: Long, toBoxId: Long, quantity: Int): Boolean {
        return inventoryDao.moveQuantity(itemId, fromBoxId, toBoxId, quantity)
    }

    suspend fun adjustQuantityInBox(itemId: Long, boxId: Long, delta: Int): Int {
        return inventoryDao.adjustQuantityInBox(itemId, boxId, delta)
    }

    // --- Locations ---
    fun getAllLocations(): Flow<List<Location>> = locationDao.getAllLocations()

    suspend fun getAllLocationsSync(): List<Location> = locationDao.getAllLocationsSync()

    fun getLocationTree(): Flow<List<LocationNode>> {
        return combine(
            locationDao.getAllLocations(),
            boxDao.getAllBoxes()
        ) { locations, boxes ->
            val locMap = locations.associateBy { it.id }
            val childrenMap = locations.groupBy { it.parentId }
            val boxesMap = boxes.groupBy { it.locationId }

            fun buildNode(loc: Location, level: Int): LocationNode {
                val fullPath = formatLocationPath(loc.id, locMap)
                val childLocations = childrenMap[loc.id] ?: emptyList()
                val childNodes = childLocations.map { buildNode(it, level + 1) }
                val locBoxes = boxesMap[loc.id] ?: emptyList()

                return LocationNode(
                    location = loc,
                    fullPath = fullPath,
                    level = level,
                    children = childNodes,
                    boxes = locBoxes,
                    totalItemsCount = 0,
                    totalQuantity = 0
                )
            }

            val rootLocations = locations.filter { it.parentId == null }
            rootLocations.map { buildNode(it, 0) }
        }
    }

    suspend fun saveLocation(location: Location): Long {
        return if (location.id == 0L) {
            locationDao.insertLocation(location)
        } else {
            locationDao.updateLocation(location)
            location.id
        }
    }

    suspend fun deleteLocation(locationId: Long) {
        val children = locationDao.getChildLocationsSync(locationId)
        children.forEach { child ->
            locationDao.moveLocation(child.id, null)
        }
        locationDao.deleteLocationById(locationId)
    }

    suspend fun moveLocation(locationId: Long, newParentId: Long?) {
        locationDao.moveLocation(locationId, newParentId)
    }

    // --- Tags ---
    fun getTagsWithCount() = tagDao.getTagsWithCount()

    fun getAllTags() = tagDao.getAllTags()

    suspend fun saveTag(tag: Tag): Long {
        return if (tag.id == 0L) {
            tagDao.insertTag(tag)
        } else {
            tagDao.updateTag(tag)
            tag.id
        }
    }

    suspend fun countItemsWithTag(tagId: Long): Int {
        return tagDao.countItemsWithTag(tagId)
    }

    suspend fun deleteTag(tagId: Long) {
        tagDao.deleteTagById(tagId)
    }

    // --- Dashboard Stats ---
    fun getDashboardStats(): Flow<DashboardStats> {
        val countsFlow = combine(
            itemDao.getAllItems(),
            boxDao.getAllBoxes(),
            locationDao.getAllLocations(),
            tagDao.getAllTags()
        ) { items, boxes, locations, tags ->
            Quadruple(items.size, boxes.size, locations.size, tags.size)
        }

        return combine(countsFlow, itemDao.getTotalInventoryQuantity()) { (itemCount, boxCount, locCount, tagCount), totalQty ->
            DashboardStats(
                totalItems = itemCount,
                totalQuantity = totalQty,
                totalBoxes = boxCount,
                totalLocations = locCount,
                totalTags = tagCount
            )
        }
    }

    // --- Global Search Result ---
    fun search(query: String): Flow<SearchResults> {
        return combine(
            getAllItemsWithDetails(),
            getAllBoxesWithDetails(),
            locationDao.getAllLocations(),
            tagDao.getAllTags()
        ) { items, boxes, locations, tags ->
            if (query.isBlank()) {
                return@combine SearchResults()
            }

            val matchingItems = items.filter { itm ->
                StringUtils.matchesFuzzy(itm.item.name, query) ||
                        StringUtils.matchesFuzzy(itm.item.description, query) ||
                        StringUtils.matchesFuzzy(itm.item.category, query) ||
                        itm.tags.any { StringUtils.matchesFuzzy(it.name, query) } ||
                        itm.boxDistributions.any {
                            StringUtils.matchesFuzzy(it.box.name, query) ||
                                    StringUtils.matchesFuzzy(it.box.code, query) ||
                                    StringUtils.matchesFuzzy(it.box.label, query)
                        }
            }

            val matchingBoxes = boxes.filter { box ->
                StringUtils.matchesFuzzy(box.box.name, query) ||
                        StringUtils.matchesFuzzy(box.box.code, query) ||
                        StringUtils.matchesFuzzy(box.box.label, query) ||
                        StringUtils.matchesFuzzy(box.box.description, query) ||
                        StringUtils.matchesFuzzy(box.locationPath, query) ||
                        box.items.any { StringUtils.matchesFuzzy(it.item.name, query) }
            }

            val matchingLocations = locations.filter { loc ->
                StringUtils.matchesFuzzy(loc.name, query) ||
                        StringUtils.matchesFuzzy(loc.description, query)
            }

            val matchingTags = tags.filter { tag ->
                StringUtils.matchesFuzzy(tag.name, query)
            }

            SearchResults(
                items = matchingItems,
                boxes = matchingBoxes,
                locations = matchingLocations,
                tags = matchingTags
            )
        }
    }
}

data class SearchResults(
    val items: List<ItemWithDetails> = emptyList(),
    val boxes: List<BoxWithDetails> = emptyList(),
    val locations: List<Location> = emptyList(),
    val tags: List<Tag> = emptyList()
) {
    val isEmpty: Boolean get() = items.isEmpty() && boxes.isEmpty() && locations.isEmpty() && tags.isEmpty()
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
