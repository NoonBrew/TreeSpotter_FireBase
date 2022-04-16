package com.example.treespotter_firebase

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.GeoPoint
import java.util.*

data class Tree(val name: String? = null,
                var favorite: Boolean? = null,
                val location: GeoPoint? = null,
                val dateSpotted: Date? = null,
                // Ignores this field when sending and getting from firebase
                @get:Exclude @set:Exclude var documentReference: DocumentReference? = null) {
}