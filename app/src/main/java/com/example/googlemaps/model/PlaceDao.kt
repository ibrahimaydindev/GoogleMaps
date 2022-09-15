package com.example.googlemaps.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

@Dao
interface PlaceDao {
    //
    @Query("SELECT * FROM Place")
    fun getAll():Flowable<List<Place>>

    @Insert
    fun save(place:Place):Completable
//asenkron tamamlanabilir olması icin
    @Delete
    fun delete(place:Place):Completable
}