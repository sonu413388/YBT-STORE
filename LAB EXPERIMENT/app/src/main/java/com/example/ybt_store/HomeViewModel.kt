package com.example.ybt_store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val firestore = Firebase.firestore

    init {
        fetchProducts()
    }

    fun fetchProducts(category: String? = null) {
        viewModelScope.launch {
            val query = if (category != null) {
                firestore.collection("products").whereEqualTo("category", category)
            } else {
                firestore.collection("products")
            }

            query.addSnapshotListener { snapshots, e ->
                if (e != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                val productList = snapshots?.map { document ->
                    document.toObject(Product::class.java)
                } ?: emptyList()

                _products.value = productList
            }
        }
    }
}
