package com.typ.aassl.data.models

import org.json.JSONException
import java.io.Serializable

data class CarInfo(
    val carId: String,
    val carModel: String,
    val carOwner: String,
    val emergencyContacts: Array<String>
) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CarInfo

        if (carId != other.carId) return false
        if (carModel != other.carModel) return false
        if (carOwner != other.carOwner) return false
        if (!emergencyContacts.contentEquals(other.emergencyContacts)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = carId.hashCode()
        result = 31 * result + carModel.hashCode()
        result = 31 * result + carOwner.hashCode()
        result = 31 * result + emergencyContacts.contentHashCode()
        return result
    }

    companion object {

        const val KEY_CAR_ID = "id"
        const val KEY_CAR_MODEL = "model"
        const val KEY_CAR_OWNER = "owner"
        const val KEY_EMERGENCY = "emergency"

        @JvmStatic
        fun parseCarInfo(carInfoModel: String): CarInfo {
            return try {
                val carInfo = carInfoModel.trim().split(",")
                CarInfo(
                    carId = carInfo.getOrElse(0) { "[NO-ID]" },
                    carModel = carInfo.getOrElse(1) { "[NO-MODEL]" },
                    carOwner = carInfo.getOrElse(2) { "[NO-OWNER]" },
                    emergencyContacts = arrayOf(
                        carInfo.getOrElse(3) { "[NO-CONTACT]" },
                        carInfo.getOrElse(4) { "[NO-CONTACT]" }
                    )
                )
            } catch (e: JSONException) {
                e.printStackTrace()
                CarInfo(
                    carId = "",
                    carModel = "",
                    carOwner = "",
                    emergencyContacts = emptyArray()
                )
            }
        }

    }

}
