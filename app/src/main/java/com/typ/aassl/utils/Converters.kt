package com.typ.aassl.utils

import androidx.room.TypeConverter
import com.typ.aassl.data.models.MapLocation

class Converters {

    @TypeConverter
    fun deserializeLocation(rawLocation: String): MapLocation {
        rawLocation.split(",").let {
            return MapLocation(
                it[0].toDouble(),
                it[1].toDouble()
            )
        }
    }

    @TypeConverter
    fun serializeLocation(location: MapLocation): String = "${location.lat},${location.lng}"
    
}