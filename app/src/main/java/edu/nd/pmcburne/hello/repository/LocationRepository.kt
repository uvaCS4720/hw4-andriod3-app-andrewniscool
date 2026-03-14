package edu.nd.pmcburne.hello.repository

import edu.nd.pmcburne.hello.data.LocationDao
import edu.nd.pmcburne.hello.data.LocationEntity
import edu.nd.pmcburne.hello.data.LocationTagEntity
import edu.nd.pmcburne.hello.network.PlacemarkApiService
import kotlinx.coroutines.flow.Flow

class LocationRepository(
    private val apiService: PlacemarkApiService,
    private val locationDao: LocationDao
) {
    fun getLocationsByTag(tag: String): Flow<List<LocationEntity>> = locationDao.getLocationsByTag(tag)

    fun getAllTags(): Flow<List<String>> = locationDao.getAllTags()

    suspend fun syncLocationsOnce() {
        val remoteLocations = apiService.getPlacemarks()
        val existingIds = locationDao.getAllLocationIds().toSet()

        val newLocations = remoteLocations
            .filter { it.id !in existingIds }
            .map {
                LocationEntity(
                    id = it.id,
                    name = it.name,
                    description = it.description,
                    latitude = it.visualCenter.latitude,
                    longitude = it.visualCenter.longitude
                )
            }

        if (newLocations.isNotEmpty()) {
            locationDao.insertLocations(newLocations)

            val tags = remoteLocations
                .filter { it.id !in existingIds }
                .flatMap { location ->
                    location.tagList.map { tag ->
                        LocationTagEntity(locationId = location.id, tag = tag)
                    }
                }
            locationDao.insertLocationTags(tags)
        }
    }
}
