package com.typ.aassl.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.typ.aassl.AASSLApplication
import com.typ.aassl.R
import com.typ.aassl.data.Accidents
import com.typ.aassl.data.models.Accident
import com.typ.aassl.data.models.CarInfo
import com.typ.aassl.data.models.MapLocation
import com.typ.aassl.ui.AccidentViewerActivity
import kotlin.random.Random

class AccidentWatcherService : FirebaseMessagingService() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        FirebaseMessaging.getInstance().apply {
            isAutoInitEnabled = true
            subscribeToTopic("accidents")
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val notifyManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Post notification to user with new accident
        try {
            Log.i("AccidentWatcherService", "onMessageReceived: Sender= ${message.from} | ${message.data}")
            val accident = with(message.data) {
                Accident(
                    location = MapLocation(
                        getOrDefault(Accident.KEY_LAT, "0").toDouble(),
                        getOrDefault(Accident.KEY_LNG, "0").toDouble()
                    ),
                    videoStorageUrl = getOrDefault(Accident.KEY_VIDEO_STORAGE_PATH, ""),
                    timestamp = getOrDefault(Accident.KEY_TIMESTAMP, "${System.currentTimeMillis()}").toLong(),
                    carInfo = CarInfo(
                        carId = getOrDefault(CarInfo.KEY_CAR_ID, "[NO-ID]"),
                        carModel = getOrDefault(CarInfo.KEY_CAR_MODEL, "[NO-MODEL]"),
                        carOwner = getOrDefault(CarInfo.KEY_CAR_OWNER, "[NO-OWNER]"),
                        emergencyContacts = getOrDefault(CarInfo.KEY_EMERGENCY, "").split(",").toTypedArray()
                    )
                )
            }
            val notification = NotificationCompat.Builder(this, "accidents")
                .setContentTitle(getString(R.string.new_accident_happened))
                .setContentText("${accident.carInfo.carOwner} crashed at ${accident.location}")
                .setSmallIcon(R.drawable.ic_car_crash)
                .setAutoCancel(true)
                .setSubText("${accident.carInfo.carModel} - ${accident.carInfo.carId}")
                .setContentInfo("${accident.carInfo.carModel} - ${accident.carInfo.carId}")
                .setContentIntent(
                    PendingIntent.getActivity(
                        this,
                        2001, Intent(this, AccidentViewerActivity::class.java).apply { putExtra("accident", accident) },
                        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                ).build()
            notifyManager.createNotificationChannel(NotificationChannel("accidents", "Accidents", NotificationManager.IMPORTANCE_HIGH))
            notifyManager.notify(Random.nextInt(20), notification)
            // Send a broadcast to UI notifying it with new accident (if app is running in foreground)
            sendBroadcast(Intent(ACTION_NEW_ACCIDENT).apply { putExtra("accident", accident) })
            // Save the accident to database
            try {
                Accidents.save(this, accident)
                Log.i("AccidentWatcherService", "Saved Accident: $accident")
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        } catch (_: Throwable) {
        }
    }

    override fun onNewToken(token: String) {
        with(FirebaseDatabase.getInstance()) {
            val refToken = getReference("tokens").child(AASSLApplication.getUID(applicationContext))
            refToken.setValue(token).addOnCompleteListener {
                if (it.isSuccessful) Log.i("AccidentWatcherService", "Token refreshed successfully. NewToken= $token")
                else Log.e("AccidentWatcherService", "Failed to refresh token.")
            }
        }
    }

    companion object {
        const val ACTION_NEW_ACCIDENT = "action_new_accident"
    }

}