package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.BoxDao
import com.example.data.dao.InventoryDao
import com.example.data.dao.ItemDao
import com.example.data.dao.LocationDao
import com.example.data.dao.TagDao
import com.example.data.model.Item
import com.example.data.model.ItemBoxCrossRef
import com.example.data.model.ItemPhoto
import com.example.data.model.ItemTagCrossRef
import com.example.data.model.Location
import com.example.data.model.StorageBox
import com.example.data.model.Tag

@Database(
    entities = [
        Item::class,
        StorageBox::class,
        Location::class,
        Tag::class,
        ItemPhoto::class,
        ItemBoxCrossRef::class,
        ItemTagCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao
    abstract fun boxDao(): BoxDao
    abstract fun locationDao(): LocationDao
    abstract fun tagDao(): TagDao
    abstract fun inventoryDao(): InventoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inventory_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
