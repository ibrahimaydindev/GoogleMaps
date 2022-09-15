package com.example.googlemaps.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Place(
    @ColumnInfo(name="name")
    val name:String,
    @ColumnInfo(name="latitude")
    val latitude:Double,
    @ColumnInfo(name="longitude")
    val longitude:Double) : Serializable
{
    @PrimaryKey(autoGenerate = true)
    var id =0
}
