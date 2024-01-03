package com.example.flomodo

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flomodo.ui.theme.DarkGrey
import com.example.flomodo.ui.theme.DarkText
import com.example.flomodo.ui.theme.LightGray
import com.example.flomodo.ui.theme.LightText
import com.example.flomodo.ui.theme.LightYellow
import com.example.flomodo.ui.theme.StopwatchTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.opencsv.CSVWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import kotlin.math.roundToInt


class RecordActivity : ComponentActivity() {

    private var coroutineScope = CoroutineScope(Dispatchers.Main)

    fun addRecord(activity: Activity) {
        if(Stopwatch.tempList.isNullOrEmpty()){ return }

        val list = getRecordTitle(MainActivity@ this)

        val alphanumerics = CharArray(26) { it -> (it + 97).toChar() }.toSet()
            .union(CharArray(9) { it -> (it + 48).toChar() }.toSet())

        val temp = list
        val db = PreferenceManager.getDefaultSharedPreferences(activity)
        val editor = db.edit()
        temp?.add(( FlomoModel(id= System.currentTimeMillis(),title = Stopwatch.timeformat + " "+   (0..0).map {
            alphanumerics.toList().random()
        }.joinToString(""), values = TimerModel(
            Stopwatch.tempList
        ), false)))
        val gson = Gson()
        val dbs = gson.toJson(temp)
        editor.putString("record", dbs).apply()


        Stopwatch.fullList?.clear()
        Stopwatch.tempList?.clear()
        Stopwatch.flowListSize = 0
        Stopwatch.restListSize = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addRecord(RecordActivity@this)

        coroutineScope.launch {
            setContent {
                Box(modifier = Modifier.fillMaxSize()) {
                    val context = LocalContext.current
                    var offsetX by remember { mutableStateOf(0f) }
                    var offsetY by remember { mutableStateOf(0f) }

                    Box(
                        Modifier
                            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                            .size(100.dp, 100.dp)
                            .background(Color.Blue)
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()

                                    val (x,y) = dragAmount
                                    when {
                                        x > 0 ->{ finish()}
                                        x < 0 ->{ /* left */ }
                                    }
                                    when {
                                        y > 0 -> { /* down */ }
                                        y < 0 -> { /* up */ }
                                    }

                                    offsetX += dragAmount.x
                                    offsetY += dragAmount.y
                                }
                            }
                    )
                }

                StopwatchTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colors.surface
                    ) {
                        AppBar()
                        val db = PreferenceManager.getDefaultSharedPreferences(LocalContext.current)
                        Stopwatch.recordList = mutableListOf(
                            FlomoModel(
                                id = System.currentTimeMillis(),
                                title = Stopwatch.timeformat,
                                values = TimerModel(
                                    Stopwatch.tempList
                                ),
                                false
                            )
                        )
                        showList(db)
                    }
                }
            }
        }
    }

    fun getRecordList(activity: Activity): SnapshotStateList<FlomoModel?> {
        val temp: MutableList<FlomoModel?>
        val db = PreferenceManager.getDefaultSharedPreferences(activity)
        val gson = Gson()
        val content = db.getString("record", null)
        temp = if (content.isNullOrEmpty()) {
            ArrayList()
        } else {
            val type = object : TypeToken<List<FlomoModel?>?>() {}.type
            gson.fromJson(content, type)
        }
        return temp.toMutableStateList()
    }

    fun getRecordTitle(activity: Activity): ArrayList<FlomoModel?> {
        val temp: ArrayList<FlomoModel?>
        val db = PreferenceManager.getDefaultSharedPreferences(activity)
        val gson = Gson()
        val content = db.getString("record", null)
        temp = if (content.isNullOrEmpty()) {
            ArrayList()
        } else {
            val type = object : TypeToken<List<FlomoModel?>?>() {}.type
            gson.fromJson(content, type)
        }
        return temp
    }

    fun <T> Collection<T>.toMutableStateList() = SnapshotStateList<T>().also { it.addAll(this) }

    @Composable
    fun showList(db: SharedPreferences) {
        var flowItemCount by remember { mutableStateOf(0) }
        var flowItems by remember { mutableStateOf(Stopwatch.recordList) }

        val activity: Activity = MainActivity@ this
        flowItems = getRecordList(MainActivity@ this)
        flowItemCount = flowItems.size
        val viewModel by viewModels<FlomoViewModel>()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            viewModel.myItems = getRecordList(activity) as SnapshotStateList<FlomoModel>
            SelectableLazyListSample(viewModel, db)
        }

    }
}


@Composable
fun AppBar() {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Top
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        color = if (isSystemInDarkTheme()) DarkText else LightText
                    )
                },
                actions = {
                },
                backgroundColor = if (isSystemInDarkTheme()) LightYellow else DarkGrey
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SelectableLazyListSample(flomoViewModel: FlomoViewModel, db: SharedPreferences) {
    val selectedItems = flomoViewModel.getSelectedItems().map { it.title }
    val selectedItemsId = flomoViewModel.getSelectedItems().map { it.id }
    val selectedItemsValues = flomoViewModel.getSelectedItems().map { it.values?.time }
    //val allItems = flomoViewModel.myItems
    val context = LocalContext.current

    val items = arrayListOf<Long>()
    if (!selectedItems.isNullOrEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp, start = 5.dp, end = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = {
                val fileName = "Flomodo " + Stopwatch.timeformat2+ ".csv"
                val path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )
                val file = File(path, fileName)
                val outputfile = FileWriter(file)
                val writer = CSVWriter(outputfile)
                val data: MutableList<String> = ArrayList()

                for (i in selectedItemsValues.toList()) {
                    if (i != null) {
                        for (j in i) {
                            if (j != null) {
                                data.add(j)
                            }
                        }
                    }
                }

                val convertedList: List<Array<out String>?> = data.map {
                    arrayOf(it)
                }
                val finalList: List<Array<out String>?> = convertedList.toList()
                writer.writeAll(finalList)
                writer.close()
            }) {
                Text(text = "Export")
            }

            Button(onClick = {
                val fileName = "Flomodo " + Stopwatch.timeformat2+ ".csv"
                val path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )
                val file = File(path, fileName)
                val outputfile = FileWriter(file)
                val writer = CSVWriter(outputfile)
                val data: MutableList<MutableList<String?>?> = ArrayList()

                for (i in flomoViewModel.myItems) {
                  data.add(i.values?.time)
                }

                val convertedList: List<Array<out String>?> = data.map { innerList ->
                    innerList?.map { it ?: "" }?.toTypedArray()
                }

                writer.writeAll(convertedList)
                writer.close()
            }) {
                Text(text = "Export All")
            }

            Button(onClick = {
                for (i in flomoViewModel.myItems) {
                    for (j in selectedItemsId) {
                        if (i.id == j) {
                            items.add(j)
                        }
                    }
                }

                val iterator = flomoViewModel.myItems.iterator()
                while (iterator.hasNext()) {
                    val item = iterator.next()
                    if (items.contains(item.id)) {
                        iterator.remove()
                    }
                }

                val temp: MutableList<FlomoModel>? = flomoViewModel.myItems
                val editor = db.edit()
                val gson = Gson()
                val dbs = gson.toJson(temp)
                editor.putString("record", dbs).apply()
                items.clear()

            }) {
                Text(text = "Delete")

            }

        }

    }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(8.dp),
        modifier = Modifier.padding(top = 15.dp)
    ) {
        itemsIndexed(
            flomoViewModel.myItems,
            key = { _, item: FlomoModel ->
                item.id
            }
        ) { index, item ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LightGray, RoundedCornerShape(8.dp))
                    .combinedClickable(onLongClick = {
                        flomoViewModel.toggleSelection(index)
                    }, onClick = {

                        val gson = Gson()
                        val jsonString = gson.toJson(flomoViewModel.myItems[index])
                        val intent = Intent(context, ShowDetailActivity::class.java)
                        intent.putExtra("KEY", jsonString)
                        context.startActivity(intent)
                    })
                    .padding(8.dp)
            ) {
                flomoViewModel.myItems[index].title?.let { Text(text = it) }
                if (item.isSelected) {
                    Icon(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .background(Color.White, CircleShape),
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.DarkGray,
                    )
                }
            }
        }
    }


}


