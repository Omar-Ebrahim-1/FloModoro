package com.example.flomodo

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.flomodo.Stopwatch.Companion.flowListSize
import com.example.flomodo.Stopwatch.Companion.fullList
import com.example.flomodo.Stopwatch.Companion.restListSize
import com.example.flomodo.Stopwatch.Companion.tempList
import com.example.flomodo.ui.theme.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class MainActivity : ComponentActivity() {
    fun getArrayList(key: String?, activity: Activity): MutableList<String?>? {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val gson = Gson()
        val json: String? = prefs.getString(key, null)
        val type: Type = object : TypeToken<ArrayList<String?>?>() {}.type
        return gson.fromJson(json, type)
    }


    fun saveArrayList(list: ArrayList<String>, key: String?, activity: Activity) {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val editor: SharedPreferences.Editor = prefs.edit()
        val gson = Gson()
        val json: String = gson.toJson(list)
        editor.putString(key, json)
        editor.apply()
    }




    fun getRecordList(activity: Activity): MutableList<FlomoModel> {
        val temp: MutableList<FlomoModel>
        val db = PreferenceManager.getDefaultSharedPreferences(activity)
        val gson = Gson()
        val content = db.getString("record", null)
        if (content.isNullOrEmpty()) {
            temp = ArrayList<FlomoModel>()
        } else {
            val type = object : TypeToken<List<FlomoModel?>?>() {}.type
            temp = gson.fromJson(content, type)
        }
        return temp
    }

    fun saveInt(key: String?, activity: Activity, value: Int) {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val editor: SharedPreferences.Editor = prefs.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getInt(key: String?, activity: Activity): Int? {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        return prefs.getInt(key, 0) ?: 0
    }

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(applicationContext, ACCESS_FINE_LOCATION)
        val result1 = ContextCompat.checkSelfPermission(applicationContext, WRITE_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf<String>(ACCESS_FINE_LOCATION, WRITE_EXTERNAL_STORAGE),
            0
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //fullList = getArrayList("list", MainActivity@ this)
        //flowListSize = getInt("flow", MainActivity@ this) ?: 0
        //restListSize = getInt("rest", MainActivity@ this) ?: 0
        Stopwatch.fullList?.clear()
        Stopwatch.tempList?.clear()
        Stopwatch.flowListSize = 0
        Stopwatch.restListSize = 0
        if(!checkPermission()){
            requestPermission()
        }

        setContent {
            StopwatchTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.surface
                ) {
                    val systemUiController = rememberSystemUiController()
                    val statusBarColor = MaterialTheme.colors.surface
                    val navigationBarColor = MaterialTheme.colors.onSurface
                    val barIcons = isSystemInDarkTheme()

                    val context = LocalContext.current

                    SideEffect {
                        systemUiController.setNavigationBarColor(
                            color = navigationBarColor,
                            darkIcons = barIcons
                        )
                        systemUiController.setStatusBarColor(
                            color = statusBarColor,
                            darkIcons = true
                        )
                    }
                    val stopWatch = remember { Stopwatch() }
                    StopwatchTimer(
                        formattedTime = stopWatch.formattedTime,
                        onStartClick = stopWatch::start,
                        onPauseClick = stopWatch::pause,
                        onResetClick = stopWatch::reset,
                        onFlowClick = stopWatch::flow,
                        onRestClick = stopWatch::rest,
                        onRecordClick = {
                            stopWatch.preReset()
                            context.startActivity(Intent(context, RecordActivity::class.java))
                        },
                        onSleepClick =  stopWatch::sleep
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if(!fullList.isNullOrEmpty()){
            saveArrayList(fullList?.toList() as ArrayList<String>, "list", MainActivity@ this)

        }
        saveInt("flow", MainActivity@ this, flowListSize)
        saveInt("rest", MainActivity@ this, restListSize)
       // addRecord(MainActivity@ this)
    }
}

@Composable
fun StopwatchTimer(
    formattedTime: String,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResetClick: () -> Unit,
    onFlowClick: () -> Unit,
    onRestClick: () -> Unit,
    onRecordClick: () -> Unit,
    onSleepClick: () -> Unit,
) {


    var flowItemCount by remember { mutableStateOf(0) }
    var flowItems by remember { mutableStateOf(fullList) }


    // var showInfoDialog by remember { mutableStateOf(false) }
    var isPauseFloModo by remember { mutableStateOf(false) }
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Top
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        color = if (isSystemInDarkTheme()) DarkText else LightText
                    )
                },
                actions = {
                },
                backgroundColor = if (isSystemInDarkTheme()) LightYellow else DarkGrey
            )
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 55.dp, start = 5.dp, end = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = {
                    onResetClick.invoke()
                    isPauseFloModo = false
                }) {
                    Text(text = "Reset")
                }

                Button(onClick = {
                    //your onclick code here
                }) {
                    Text(text = "Reallocate")
                }

                Button(onClick = {
                    onRecordClick.invoke()
                }) {
                    Text(text = "Record")
                }

            }

            flowItems = fullList?.asReversed()
            flowItemCount = fullList?.size ?: 0

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .clickable {
                            if (isPauseFloModo) {
                                onPauseClick.invoke()
                            } else {
                                onStartClick.invoke()
                            }
                            isPauseFloModo = !isPauseFloModo


                        }
                        .padding(top = 10.dp, bottom = 10.dp),
                    text = formattedTime,
                    fontWeight = FontWeight.Bold,
                    fontSize = 50.sp,
                    color = if (isSystemInDarkTheme()) LightYellow else DarkGrey
                )

            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(.10f),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(flowItemCount) { item ->
                        Spacer(modifier = Modifier.height(10.dp))
                        flowItems?.get(item)?.let { Text(text = it, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)) }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(50.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = {
                        onFlowClick.invoke()
                        flowItems = fullList?.asReversed()
                        flowItemCount = fullList?.size ?: 0
                    }) {
                        Text(text = "Flow")
                    }

                    Button(onClick = {
                        onSleepClick.invoke()
                        flowItems = fullList?.asReversed()
                        flowItemCount = fullList?.size ?: 0
                    }) {
                        Text(text = "Sleep")
                    }

                    Button(onClick = {
                        onRestClick.invoke()
                        flowItems = fullList?.asReversed()
                        flowItemCount = fullList?.size ?: 0
                    }) {
                        Text(text = "Rest")
                    }

                }
            }
        }
    }


}