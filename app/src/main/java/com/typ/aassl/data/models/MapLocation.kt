package com.typ.aassl.data.models

import java.io.Serializable

data class MapLocation(
    val lat: Double,
    val lng: Double
) : Serializable {
    override fun toString(): String = "%.3f,%.3f".format(lat, lng)
}