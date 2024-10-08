package com.project17.tourbooking.viewmodels

import FirestoreHelper
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project17.tourbooking.models.Category
import kotlinx.coroutines.launch

class AppViewModel(): ViewModel(){
    val priceRange = MutableLiveData<ClosedFloatingPointRange<Float>>()
    val rateMoreThan = MutableLiveData<Int>()

    val categories = mutableStateListOf<Category>()

    fun loadCategories(firestoreHelper: FirestoreHelper) {
        viewModelScope.launch {
            firestoreHelper.getCategories2 { result ->
                categories.clear()
                categories.addAll(result)
            }
        }
    }

    var selectedCategory = mutableStateOf<Category?>(null)
        private set

    var isChosenCategory = mutableStateOf(false)
        private set

    // Function to set the selected category
    fun selectCategory(category: Category) {
        selectedCategory.value = category
    }
}