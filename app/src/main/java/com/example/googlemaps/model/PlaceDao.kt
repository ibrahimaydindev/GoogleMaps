package com.example.googlemaps.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PlaceDao {

    @Query("SELECT * FROM Place")
    fun getAll():List<Place>

    @Insert
    fun save(place:Place)

    @Delete
    fun delete(place:Place)
}