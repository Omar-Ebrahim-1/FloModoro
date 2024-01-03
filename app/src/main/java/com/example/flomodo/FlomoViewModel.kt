package com.example.flomodo

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class FlomoViewModel : ViewModel() {

    var myItems = mutableStateListOf<FlomoModel>()

    fun getSelectedItems() = myItems.filter { it.isSelected }

    fun toggleSelection(index: Int) {

        val item = myItems[index]
        val isSelected = item.isSelected?: false

        if (isSelected) {
            myItems[index] = item.copy(isSelected = false)
        } else {
            myItems[index] = item.copy(isSelected = true)
        }
    }
}