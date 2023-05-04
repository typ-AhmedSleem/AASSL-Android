package com.typ.aassl.data

import android.content.Context
import com.typ.aassl.data.models.Accident

object Accidents {

    fun getAll(ctx: Context): Array<Accident> {
        return AccidentsDatabase.getInstance(ctx).getAccidentDAO().getAllAccidents()
    }

    fun save(ctx: Context, accident: Accident) {
        AccidentsDatabase.getInstance(ctx).getAccidentDAO().saveAccident(accident)
    }

    fun update(ctx: Context, accident: Accident) {
        AccidentsDatabase.getInstance(ctx).getAccidentDAO().updateAccident(accident)
    }

}