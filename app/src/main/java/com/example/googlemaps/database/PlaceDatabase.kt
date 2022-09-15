package com.example.googlemaps.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.googlemaps.model.Place

@Database(entities =[Place::class],version=1)
abstract class PlaceDatabase: RoomDatabase() {
    abstract fun placeDao(): PlaceDao
}