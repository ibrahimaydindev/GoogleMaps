package com.example.googlemaps.model

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities =[Place::class],version=1)
abstract class PlaceDatabase: RoomDatabase() {
    abstract fun placeDao():PlaceDao
}