package com.typ.aassl.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.typ.aassl.R
import com.typ.aassl.data.Accidents
import com.typ.aassl.data.models.Accident
import com.typ.aassl.data.models.MapLocation
import com.typ.aassl.ui.theme.AASSLAndroidTheme
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class MainActivity : ComponentActivity() {

    private val accidents by lazy {
        mutableStateListOf<Accident>().apply { addAll(Accidents.getAll(this@MainActivity)) }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preview = true
        var firstTime = true
        if (preview) {
            GlobalScope.launch(Dispatchers.IO) {
                if (firstTime) {
                    delay(5.seconds)
                    firstTime = false
                }
                repeat(100) {
                    accidents.add(
                        Accident(
                            MapLocation(Random.nextDouble(), Random.nextDouble()),
                            "",
                            "",
                            System.currentTimeMillis(),
                            booleanArrayOf(false, true).random()
                        )
                    )
                    delay(Random.nextInt(0, 10).seconds)
                }
            }
        }
        setContent {
            AASSLAndroidTheme {
                window.statusBarColor = MaterialTheme.colorScheme.background.value.toInt()
                MainView()
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun MainView() {
        val ctx = LocalContext.current
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
                        .clickable {
                            accidents.add(
                                Accident(
                                    MapLocation(Random.nextDouble(), Random.nextDouble()),
                                    "",
                                    "",
                                    System.currentTimeMillis(),
                                    booleanArrayOf(false, true).random()
                                )
                            )
                        }
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
                            items(items = accidentsGroup) { accident -> AccidentItemView(accident) }
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
        val composeScope = currentRecomposeScope
        Box(modifier = Modifier
            .fillMaxWidth()
            .background(color = if (accident.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.errorContainer)
            .clickable {
                // TODO: Show accident in AccidentViewerActivity
                if (accident.isRead.not()) {
                    accident.markAsRead()
                    accidents
                        .indexOf(accident)
                        .let { idx ->
                            if (idx == -1) return@let
                            accidents[idx] = accident
                            composeScope.invalidate()
                        }
                }
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
                    fontSize = 12.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                    color = if (accident.isRead) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onErrorContainer,
                    text = "${stringResource(R.string.lat)} ${accident.location.lat} , ${stringResource(R.string.lng)} ${accident.location.lng}"
                )
            }
            Divider(color = if (accident.isRead) DividerDefaults.color else MaterialTheme.colorScheme.onErrorContainer)
        }
    }

    private fun monthOfTimestamp(timestamp: Long): String {
        return SimpleDateFormat("hh:mm:ss aa", Locale.getDefault()).format(timestamp)
    }

}