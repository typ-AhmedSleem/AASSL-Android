package com.typ.aassl.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

@Entity(tableName = "accidents")
class Accident(
    @ColumnInfo val location: MapLocation,
    @ColumnInfo val videoStorageUrl: String = "",
    @ColumnInfo val timestamp: Long,
    isRead: Boolean = false,
    @ColumnInfo val carInfo: CarInfo,
) : Serializable {

    @ColumnInfo
    var isRead: Boolean = isRead
        private set

    @PrimaryKey(autoGenerate = true)
    var id: Int = Random.nextInt()

    @Ignore
    val accidentVideoFile: File = File("${timestamp}.mp4")

    fun formattedTimestamp(pattern: String = "dd MMM yyyy hh:mm:ss aa"): String {
        return SimpleDateFormat(pattern, Locale.getDefault())
            .format(Date(timestamp))
    }

    fun markAsRead() {
        isRead = true
    }

    suspend fun downloadVideo(pBar: CircularProgressIndicator) {
        withContext(Dispatchers.Main) {
            pBar.progress += 1
        }
        TODO("Not yet implemented")
    }

    suspend fun saveAccidentVideo() {
        TODO("Not yet implemented")
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if ((other is Accident).not()) return false
        return (other as Accident).timestamp == timestamp
    }

    override fun hashCode(): Int {
        var result = location.hashCode()
        result = 31 * result + videoStorageUrl.hashCode()
        result = 31 * result + carInfo.hashCode()
        result = 31 * result + isRead.hashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }

    override fun toString(): String {
        return "Accident(location=$location,\nvideoStorageUrl='$videoStorageUrl',\ntimestamp=$timestamp,\ncarInfo=$carInfo,\nisRead=$isRead,\nid=$id,\naccidentVideoFile=${accidentVideoFile.path})"
    }


    companion object {
        // Keys of message payload
        const val KEY_LAT = "lat"
        const val KEY_LNG = "lng"
        const val KEY_VIDEO_STORAGE_PATH = "video"
        const val KEY_TIMESTAMP = "timestamp"
    }

    fun randomCarInfo(): CarInfo {
        return CarInfo(
            carId = Random.nextInt(1, 9999).toString(),
            carModel = arrayOf("Mazda RX7", "Toyota Supra", "Nissan GTR").random(),
            carOwner = arrayOf("Vin Diesel", "Paul Walker", "John Cina").random(),
            emergencyContacts = arrayOf("01029787124", "01553106473")
        )
    }

}
