package com.example.flomodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flomodo.ui.theme.StopwatchTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class ShowDetailActivity :  ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StopwatchTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.surface
                ) {
                    AppBar()

                    val bundle = intent.extras
                    val jsonString = bundle!!.getString("KEY")

                    val gson = Gson()
                    val listOfdoctorType: Type = object : TypeToken<FlomoModel?>() {}.type
                    val doctors: FlomoModel =
                        gson.fromJson(jsonString, listOfdoctorType)
                    val flowItemCount by remember { mutableStateOf(doctors.values?.time?.size) }
                    val flowItems by remember { mutableStateOf(doctors.values?.time) }


                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top=50.dp),
                    ) {

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            flowItemCount?.let {
                                items(it) { item ->
                                    Spacer(modifier = Modifier.height(10.dp))
                                    flowItems?.get(item)?.let {
                                        if (it != null) {
                                            Text(
                                                text = it,
                                                style = TextStyle(
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}