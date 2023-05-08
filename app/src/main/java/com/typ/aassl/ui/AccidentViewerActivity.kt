@file:Suppress("DEPRECATION")

package com.typ.aassl.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textview.MaterialTextView
import com.typ.aassl.R
import com.typ.aassl.data.Accidents
import com.typ.aassl.data.models.Accident
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

class AccidentViewerActivity : AppCompatActivity(R.layout.activity_accident_viewer) {

    private lateinit var accident: Accident

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        // Init runtime
        accident = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) intent.getSerializableExtra("accident", Accident::class.java) as Accident
        else intent.getSerializableExtra("accident") as Accident
        try {
            Accidents.update(this, accident)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        // Init UI
        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }
        findViewById<MaterialTextView>(R.id.tv_accident_time).text = accident.formattedTimestamp("hh:mm:ss a\ndd MMM yyyy")
        findViewById<MaterialButton>(R.id.btn_show_loc).setOnClickListener {
            with(accident) {
                val uri = Uri.parse("geo:${location.lat},${location.lng}")
                Intent(Intent.ACTION_VIEW, uri).apply {
                    resolveActivity(packageManager)?.let {
                        startActivity(this)
                    } ?: Toast.makeText(this@AccidentViewerActivity, R.string.maps_not_installed, Toast.LENGTH_SHORT).show()
                }
            }
        }
        findViewById<MaterialTextView>(R.id.tv_accident_loc).text = "${getString(R.string.lat)} ${accident.location.lat}\n${getString(R.string.lng)} ${accident.location.lng}"
        with(accident.carInfo) {
            findViewById<MaterialTextView>(R.id.tv_car_id).text = this.carId
            findViewById<MaterialTextView>(R.id.tv_car_model).text = this.carModel
            findViewById<MaterialTextView>(R.id.tv_car_owner).text = this.carOwner
            findViewById<MaterialTextView>(R.id.tv_emergency_contacts).text = emergencyContacts.let {
                var contacts = ""
                it.forEach { contact -> contacts += "$contact\n" }
                contacts
            }
        }
        val videoView = findViewById<VideoView>(R.id.video_view)
        videoView.visibility = View.INVISIBLE
        videoView.setMediaController(MediaController(this, false))
        val tvProgress = findViewById<MaterialTextView>(R.id.tv_progress_done)
        val progressBar = findViewById<CircularProgressIndicator>(R.id.progress_video_downloader)
        // Load video then play it
        GlobalScope.launch(Dispatchers.IO) {
            val totalBytes: Int
            var downloadedBytes = 0
            var downloadPercentage: Float
            // Download the video file then cache it
            val videoFile = File(cacheDir, "${accident.timestamp}.mp4")
            var ins: InputStream? = null
            var ots: FileOutputStream? = null
            try {
                ins = assets.open("video.mp4")
                ots = FileOutputStream(videoFile)
                totalBytes = ins.available()
                // Copy video file from ins to outs
                var read: Int
                val buffer = ByteArray(1024)
                while ((ins.read(buffer).also { read = it }) != -1) {
                    ots.write(buffer, 0, read)
                    downloadedBytes += 1024
                    withContext(Dispatchers.Main) {
                        downloadPercentage = ((downloadedBytes / totalBytes.toFloat()) * 100).coerceAtMost(100f)
                        progressBar.progress = downloadPercentage.toInt()
                        tvProgress.text = "%.1f".format(downloadPercentage) + " %"
                    }
                    delay(Random.nextInt(0, 5).milliseconds)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    ins?.close()
                } catch (_: IOException) {
                }
                try {
                    ots?.flush()
                    ots?.close()
                } catch (_: IOException) {
                }
            }
            delay(500L)
            // Prepare VideoView then play the video
            withContext(Dispatchers.Main) {
                progressBar.hide()
                videoView.visibility = View.VISIBLE
                tvProgress.visibility = View.INVISIBLE
                videoView.setVideoURI(Uri.fromFile(videoFile))
                videoView.start()
            }
        }
    }

}