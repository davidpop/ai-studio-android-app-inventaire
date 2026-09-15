package com.example.data.model

data class BoxDistribution(
    val box: StorageBox,
    val quantity: Int,
    val locationPath: String = ""
)

data class ItemWithDetails(
    val item: Item,
    val photos: List<ItemPhoto> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val boxDistributions: List<BoxDistribution> = emptyList(),
    val totalQuantity: Int = boxDistributions.sumOf { it.quantity }
)

data class ItemInBox(
    val item: Item,
    val quantity: Int
)

data class BoxWithDetails(
    val box: StorageBox,
    val location: Location? = null,
    val locationPath: String = "",
    val items: List<ItemInBox> = emptyList(),
    val itemCount: Int = items.size,
    val totalQuantity: Int = items.sumOf { it.quantity }
)

data class LocationNode(
    val location: Location,
    val fullPath: String,
    val level: Int = 0,
    val children: List<LocationNode> = emptyList(),
    val boxes: List<StorageBox> = emptyList(),
    val totalItemsCount: Int = 0,
    val totalQuantity: Int = 0
)

data class DashboardStats(
    val totalItems: Int = 0,
    val totalQuantity: Int = 0,
    val totalBoxes: Int = 0,
    val totalLocations: Int = 0,
    val totalTags: Int = 0
)
