package com.typ.aassl.utils

import androidx.room.TypeConverter
import com.typ.aassl.data.models.CarInfo
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

    @TypeConverter
    fun saveCarInfo(carInfo: CarInfo): String {
        return "${carInfo.carId},${carInfo.carModel},${carInfo.carOwner},${carInfo.emergencyContacts[0]},${carInfo.emergencyContacts[1]}"
    }

    @TypeConverter
    fun loadCarInfo(rawJson: String): CarInfo = CarInfo.parseCarInfo(rawJson)

}