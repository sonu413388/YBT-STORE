package com.example.ybt_store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val firestore = Firebase.firestore

    fun searchProducts(query: String) {
        viewModelScope.launch {
            if (query.isEmpty()) {
                _products.value = emptyList()
                return@launch
            }

            firestore.collection("products")
                .whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", query + '\uf8ff')
                .addSnapshotListener { snapshots, e ->
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
