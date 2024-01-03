package com.example.flomodo

import androidx.compose.runtime.Immutable

@Immutable
data class FlomoModel (
    val id: Long,
    var title: String?,
    val values: TimerModel?,
    var isSelected: Boolean
)
@Immutable
data class TimerModel (
    val time: MutableList<String?>?
)