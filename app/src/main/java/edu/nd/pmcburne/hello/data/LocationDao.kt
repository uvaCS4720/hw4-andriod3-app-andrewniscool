package edu.nd.pmcburne.hello.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLocations(locations: List<LocationEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLocationTags(tags: List<LocationTagEntity>)

    @Query("SELECT id FROM locations")
    suspend fun getAllLocationIds(): List<Int>

    @Query(
        """
        SELECT l.*
        FROM locations l
        INNER JOIN location_tags t ON l.id = t.locationId
        WHERE t.tag = :tag
        ORDER BY l.name ASC
        """
    )
    fun getLocationsByTag(tag: String): Flow<List<LocationEntity>>

    @Query("SELECT DISTINCT tag FROM location_tags ORDER BY tag ASC")
    fun getAllTags(): Flow<List<String>>
}
