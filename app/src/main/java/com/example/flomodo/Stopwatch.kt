package com.example.flomodo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class Stopwatch {
    var formattedTime by mutableStateOf("00:00:00:000")
    private var coroutineScope = CoroutineScope(Dispatchers.Main)
    private var isActive = false
    private var timeMillis = 0L
    private var lastTimestamp = 0L
    private val SECOND = 1000
    private val MINUTE = 60 * SECOND
    private val HOUR = 60 * MINUTE


    companion object {
       val sdf = SimpleDateFormat("MM dd yyyy")
        val timeformat = sdf.format(Date())
        val sdf2 = SimpleDateFormat("MM-dd-yyyy")
        val timeformat2 = sdf.format(Date())
        var fullList: MutableList<String?>? = ArrayList<String?>()
        var tempList: MutableList<String?>? = ArrayList<String?>()
        var recordList: MutableList<FlomoModel?>  =  ArrayList<FlomoModel?>()
        var flowListSize: Int = 0
        var restListSize:  Int = 0
    }

    fun start() {
        if (isActive) return

        coroutineScope.launch {
            lastTimestamp = System.currentTimeMillis()
            this@Stopwatch.isActive = true
            while (this@Stopwatch.isActive) {
                delay(10L)
                timeMillis += System.currentTimeMillis() - lastTimestamp
                lastTimestamp = System.currentTimeMillis()
                formattedTime = formatTime(timeMillis)
            }
        }
    }

    fun pause() {
        isActive = false
    }

    fun reset() {
        coroutineScope.cancel()
        coroutineScope = CoroutineScope(Dispatchers.Main)
        timeMillis = 0L
        lastTimestamp = 0L
        formattedTime = "00:00:00:000"
        isActive = false
        fullList?.clear()
        tempList?.clear()
        flowListSize = 0
        restListSize = 0

    }

    fun preReset() {
        coroutineScope.cancel()
        coroutineScope = CoroutineScope(Dispatchers.Main)
        timeMillis = 0L
        lastTimestamp = 0L
        formattedTime = "00:00:00:000"
        isActive = false
    }


    fun flow() {
        coroutineScope.cancel()
        coroutineScope = CoroutineScope(Dispatchers.Main)
        formattedTime = formatTime(timeMillis)
        flowListSize++
        if(fullList == null){
            fullList = ArrayList<String?>()
        }
        if(tempList == null){
            tempList = ArrayList<String?>()
        }
        fullList?.add("Flow"+(flowListSize)+"    "+formattedTime)
        tempList?.add("Flow"+(flowListSize)+"    "+formattedTime)
        timeMillis = 0L
        lastTimestamp = 0L
        formattedTime = "00:00:00:000"
        isActive = false
        start()
    }

    fun rest() {
        coroutineScope.cancel()
        coroutineScope = CoroutineScope(Dispatchers.Main)
        formattedTime = formatTime(timeMillis)
        restListSize++
        if(fullList == null){
            fullList = ArrayList<String?>()
        }
        if(tempList == null){
            tempList = ArrayList<String?>()
        }
        fullList?.add("Rest"+(restListSize)+"    "+formattedTime)
        tempList?.add("Rest"+(restListSize)+"    "+formattedTime)
        timeMillis = 0L
        lastTimestamp = 0L
        formattedTime = "00:00:00:000"
        isActive = false
        start()
    }

    fun sleep() {
        coroutineScope.cancel()
        coroutineScope = CoroutineScope(Dispatchers.Main)
        formattedTime = formatTime(timeMillis)

        if(fullList == null){
            fullList = ArrayList()
        }
        if(tempList == null){
            tempList = ArrayList()
        }
        fullList?.add("Sleep    "+ formattedTime)
        tempList?.add("Sleep    " +formattedTime)
        timeMillis = 0L
        lastTimestamp = 0L
        formattedTime = "00:00:00:000"
        isActive = false
        start()
    }


    private fun formatTime(timeMillis: Long): String {
        val timeMillis = timeMillis
        var hours = 0L
        val localDateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timeMillis),
            ZoneId.systemDefault()
        )
        val formatter = DateTimeFormatter.ofPattern(
            "mm:ss:SSS",
            Locale.getDefault()
        )
        return if(timeMillis>HOUR){
            hours =  TimeUnit.MILLISECONDS.toHours(timeMillis)
            hours.toString()+":"+localDateTime.format(formatter)
        } else {
            "00" + ":" + localDateTime.format(formatter)
        }
    }

}