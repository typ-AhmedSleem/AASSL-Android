package com.typ.aassl.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.typ.aassl.data.models.Accident

@Dao
interface AccidentDAO {

    @Insert(entity = Accident::class)
    fun saveAccident(accident: Accident)

    @Query("SELECT DISTINCT * FROM accidents ORDER BY timestamp DESC")
    fun getAllAccidents(): Array<Accident>

    @Update(entity = Accident::class)
    fun updateAccident(accident: Accident)

}