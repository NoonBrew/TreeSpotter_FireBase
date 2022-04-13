package com.example.treespotter_firebase

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


private const val COLLECTION_NAME = "trees"
private const val TAG = "TREE_VIEW_MODEL"
class TreeSpotterViewModel: ViewModel() {


    // Connect to firebase.
    // TODO set up a MVVM using a Repo and a Service

    private val database = Firebase.firestore
    private val treeCollectionReference = database.collection(COLLECTION_NAME)

    val latestTrees = MutableLiveData<List<Tree>>()


    private val latestTreeListener = treeCollectionReference
        .orderBy("dateSpotted", Query.Direction.DESCENDING)
        .limit(10)
        .addSnapshotListener{ snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error fetching latest trees", error)
            } else if (snapshot != null) {
                val trees = snapshot.toObjects(Tree::class.java)
                Log.d(TAG, "Trees from firebase: $trees")
                latestTrees.postValue(trees)
            }
        }

}
