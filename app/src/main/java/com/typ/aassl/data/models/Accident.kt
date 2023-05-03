package com.typ.aassl.data.models

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale

@Entity(tableName = "accidents")
class Accident(
    @ColumnInfo val location: MapLocation,
    @ColumnInfo val videoStorageUrl: String,
    @ColumnInfo val videoLocalPath: String,
    @PrimaryKey(autoGenerate = false) val timestamp: Long,
    isRead: Boolean = false
) : Serializable {

    @ColumnInfo
    var isRead: Boolean = isRead
        private set

    val formattedTimestamp = DateTimeFormatter
        .ofPattern(
            "hh:mm aa - dd MM yyyy",
            Locale.getDefault()
        ).format(Instant.ofEpochMilli(timestamp))

    fun showLocationOnMaps(ctx: Context) {
        val mapsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:${location.lat},${location.lng}"))
        mapsIntent.resolveActivity(ctx.packageManager)?.let { ctx.startActivity(mapsIntent) }
    }

    fun markAsRead() {
        isRead = true
    }

    suspend fun downloadVideo() {
        TODO("Not yet implemented")
    }

    suspend fun saveAccidentVideo() {
        TODO("Not yet implemented")
    }

}