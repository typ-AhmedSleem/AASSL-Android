package com.typ.aassl.ui

import android.Manifest
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.typ.aassl.R
import com.typ.aassl.data.Accidents
import com.typ.aassl.data.models.Accident
import com.typ.aassl.services.AccidentWatcherService
import com.typ.aassl.ui.theme.AASSLAndroidTheme
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val accidentsReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                if (it.action == AccidentWatcherService.ACTION_NEW_ACCIDENT) {
                    try {// New accident broadcast
                        val accident = if (Build.VERSION.SDK_INT >= TIRAMISU) intent.getSerializableExtra("accident", Accident::class.java) as Accident
                        else intent.getSerializableExtra("accident") as Accident
                        accidents.add(0, accident)
                        Log.i("MainActivity::AccidentsReceiver", "onNewAccident: $accident")
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    private val accidents by lazy {
        mutableStateListOf<Accident>().apply {
            addAll(Accidents.getAll(this@MainActivity).sortedArrayWith { first, second -> (second.timestamp - first.timestamp).toInt() })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            Log.i("MainActivity", "Token: $it")
        }
        val notifyManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Check if notifications are enabled
        if (!notifyManager.areNotificationsEnabled()) {
            // Ask for permission
            if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                // Request notification permission
                val permissionLauncher = registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) {
                    // todo: continue from here
                }
            }
//            openAppSettings()
        }
        setContent {
            AASSLAndroidTheme {
                window.statusBarColor = MaterialTheme.colorScheme.background.value.toInt()
                MainView()
            }
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", packageName, null)
        startActivity(intent)
    }

    @RequiresApi(TIRAMISU)
    override fun onStart() {
        super.onStart()
        // Refresh accidents list
        accidents.clear()
        accidents.addAll(Accidents.getAll(this@MainActivity).sortedArrayWith { first, second -> (second.timestamp - first.timestamp).toInt() })
        registerReceiver(accidentsReceiver, IntentFilter(AccidentWatcherService.ACTION_NEW_ACCIDENT), RECEIVER_EXPORTED)
    }

    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(accidentsReceiver)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(accidentsReceiver)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun MainView() {
        val group = accidents.groupBy { monthOfTimestamp(it.timestamp) }
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Text(
                    text = stringResource(id = R.string.app_name_long),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                )
                Divider(modifier = Modifier.fillMaxWidth())
                // Content
                if (accidents.isEmpty()) {
                    // Show empty text
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            fontSize = 15.sp,
                            text = stringResource(id = R.string.empty_msg),
                            textAlign = TextAlign.Justify,
                            fontWeight = FontWeight.Light,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    // Show accidents list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 10.dp)
                    ) {
                        group.forEach { (month, accidentsGroup) ->
                            stickyHeader(key = month) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                ) {
                                    Text(
                                        text = month,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                            items(items = accidentsGroup) { accident ->
                                AccidentItemView(
                                    accident = accident,
                                    newAccidentText = "${accident.carInfo.carOwner} crashed his ${accident.carInfo.carModel}",
                                    oldAccidentText = "${accident.carInfo.carOwner} crashed his ${accident.carInfo.carModel}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun AccidentItemView(
        accident: Accident,
        newAccidentText: String = "${stringResource(id = R.string.new_accident_at)} ${accident.formattedTimestamp()}",
        oldAccidentText: String = "${stringResource(id = R.string.old_accident_at)} ${accident.formattedTimestamp()}"
    ) {
        val ctx = LocalContext.current
        val composeScope = currentRecomposeScope
        Box(modifier = Modifier
            .fillMaxWidth()
            .background(color = if (accident.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.errorContainer)
            .clickable {
                if (accident.isRead.not()) {
                    accidents
                        .indexOf(accident)
                        .let { idx ->
                            if (idx == -1) return@let
                            accidents[idx] = accident
                            composeScope.invalidate()
                        }
                }
                startActivity(Intent(ctx, AccidentViewerActivity::class.java).apply { putExtra("accident", accident) })
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 10.dp,
                        end = 10.dp,
                        top = 20.dp,
                        bottom = 20.dp
                    )
            ) {
                Text(
                    fontSize = 15.sp,
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    text = if (accident.isRead) oldAccidentText else newAccidentText,
                    color = if (accident.isRead) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    fontSize = 13.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                    color = if (accident.isRead) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onErrorContainer,
                    text = accident.formattedTimestamp()
                )
                Text(
                    fontSize = 12.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                    color = if (accident.isRead) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onErrorContainer,
                    text = "${stringResource(R.string.lat)} ${accident.location.lat} , ${stringResource(R.string.lng)} ${accident.location.lng}"
                )
            }
            Divider(color = if (accident.isRead) DividerDefaults.color else MaterialTheme.colorScheme.onErrorContainer)
        }
    }

    private fun monthOfTimestamp(timestamp: Long): String {
        return SimpleDateFormat("hh:mm aa", Locale.getDefault()).format(timestamp)
    }

}