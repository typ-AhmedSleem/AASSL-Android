package com.typ.aassl

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.FirebaseApp
import java.util.UUID

class AASSLApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }

    companion object {

        private const val UID = "uid"

        @JvmStatic
        fun getUID(ctx: Context): String {
            return ctx.getSharedPreferences("AASSL", MODE_PRIVATE).let { prefs ->
                val uid = prefs.getString(UID, null)
                if (uid.isNullOrEmpty()) {
                    val newUID = createNewUID(ctx)
                    prefs.edit().apply {
                        putString(UID, newUID)
                        apply()
                    }
                    return newUID
                } else return uid
            }
        }

        private fun createNewUID(ctx: Context) = UUID.randomUUID().toString().split('-')[0]

    }

}