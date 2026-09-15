package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Location
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {

    @Query("SELECT * FROM locations ORDER BY name ASC")
    fun getAllLocations(): Flow<List<Location>>

    @Query("SELECT * FROM locations ORDER BY name ASC")
    suspend fun getAllLocationsSync(): List<Location>

    @Query("SELECT * FROM locations WHERE parentId IS NULL ORDER BY name ASC")
    fun getRootLocations(): Flow<List<Location>>

    @Query("SELECT * FROM locations WHERE parentId = :parentId ORDER BY name ASC")
    fun getChildLocations(parentId: Long): Flow<List<Location>>

    @Query("SELECT * FROM locations WHERE parentId = :parentId ORDER BY name ASC")
    suspend fun getChildLocationsSync(parentId: Long): List<Location>

    @Query("SELECT * FROM locations WHERE id = :id")
    fun getLocationById(id: Long): Flow<Location?>

    @Query("SELECT * FROM locations WHERE id = :id")
    suspend fun getLocationByIdSync(id: Long): Location?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: Location): Long

    @Update
    suspend fun updateLocation(location: Location)

    @Delete
    suspend fun deleteLocation(location: Location)

    @Query("DELETE FROM locations WHERE id = :id")
    suspend fun deleteLocationById(id: Long)

    @Query("SELECT COUNT(*) FROM locations")
    suspend fun getLocationCount(): Int

    @Query("UPDATE locations SET parentId = :newParentId WHERE id = :locationId")
    suspend fun moveLocation(locationId: Long, newParentId: Long?)
}
