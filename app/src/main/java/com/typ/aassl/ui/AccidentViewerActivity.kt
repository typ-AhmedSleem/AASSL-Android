@file:Suppress("DEPRECATION")

package com.typ.aassl.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.storage.FirebaseStorage
import com.typ.aassl.R
import com.typ.aassl.data.Accidents
import com.typ.aassl.data.models.Accident
import kotlinx.coroutines.DelicateCoroutinesApi
import java.io.File

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
            accident.markAsRead()
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
        progressBar.show()
        tvProgress.visibility = View.VISIBLE
        videoView.visibility = View.INVISIBLE
        val cacheVideoFile = accident.getVideoCacheFile(cacheDir)
        Log.i("AccidentViewingActivity", "Targeting video at Storage: [%s] | at Local: [%s]".format(accident.accidentVideoRef, cacheVideoFile.path))
        val videoCached = runCatching { cacheVideoFile.exists() }.getOrDefault(false)
        if (videoCached) {
            // [APPROACH-1] Check whether video exists in LocalStorage or not
            Log.i("AccidentViewingActivity", "Accident video was found in cache. Playing it...")
            // Play accident video
            playAccidentVideo(
                cacheVideoFile,
                progressBar,
                tvProgress,
                videoView
            )
        } else {
            // [APPROACH-2] Locate video in FirebaseStorage, download it then view it in VideoPlayer
            val storage = FirebaseStorage.getInstance()
            storage.getReference(accident.accidentVideoRef).getFile(cacheVideoFile)
                .addOnProgressListener {
                    val downloadPercentage = runCatching { ((it.bytesTransferred / it.totalByteCount.toFloat()) * 100).coerceAtMost(100f) }.getOrDefault(0f)
                    progressBar.progress = downloadPercentage.toInt()
                    tvProgress.text = "%.1f".format(downloadPercentage) + " %"
                }.addOnFailureListener {
                    progressBar.hide()
                    tvProgress.text = "Error downloading Accident Video"
                    Toast.makeText(this@AccidentViewerActivity, "Error downloading video or not it's not available", Toast.LENGTH_SHORT).show()
                    Log.e("AccidentViewingActivity", "Error downloading accident video. Reason=[%s]".format(it))
                    it.printStackTrace()
                }.addOnSuccessListener {
                    Log.i("AccidentViewingActivity", "Video downloaded to cache. Playing it...")
                    // Play accident video
                    playAccidentVideo(
                        cacheVideoFile,
                        progressBar,
                        tvProgress,
                        videoView
                    )
                }
        }
    }

    private fun playAccidentVideo(
        cacheVideoFile: File,
        progressBar: CircularProgressIndicator,
        tvProgress: MaterialTextView,
        videoView: VideoView
    ) {
        // Check if video file is a DIR
        if (cacheVideoFile.isDirectory) {
            progressBar.hide()
            tvProgress.visibility = View.VISIBLE
            videoView.visibility = View.INVISIBLE
            tvProgress.text = "Can't play accident video"
            return
        }
        progressBar.hide()
        tvProgress.visibility = View.INVISIBLE

        videoView.visibility = View.VISIBLE
        videoView.setVideoURI(Uri.fromFile(cacheVideoFile))
        videoView.start()
    }
}