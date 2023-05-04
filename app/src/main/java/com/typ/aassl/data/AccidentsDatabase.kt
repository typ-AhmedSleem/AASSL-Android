package com.typ.aassl.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.typ.aassl.data.models.Accident
import com.typ.aassl.data.models.AccidentDAO
import com.typ.aassl.utils.Converters
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized

@Database(
    version = 1,
    entities = [Accident::class],
    exportSchema = false
)
@TypeConverters(
    Converters::class
)
abstract class AccidentsDatabase : RoomDatabase() {

    abstract fun getAccidentDAO(): AccidentDAO

    companion object {

        @Volatile
        private var INSTANCE: AccidentsDatabase? = null // Internal instance.

        @OptIn(InternalCoroutinesApi::class)
        fun getInstance(ctx: Context): AccidentsDatabase {
            if (INSTANCE == null) {
                synchronized(AccidentsDatabase::class) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            ctx,
                            AccidentsDatabase::class.java,
                            "AccidentsDatabase"
                        ).allowMainThreadQueries().build()
                    }
                }
            }
            return INSTANCE!!
        }

    }

}