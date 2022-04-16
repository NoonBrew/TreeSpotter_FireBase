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
    // TODO set up a MVVM using a Repo... Callbacks?

    private val database = Firebase.firestore
    private val treeCollectionReference = database.collection(COLLECTION_NAME)

    val latestTrees = MutableLiveData<List<Tree>>()


//    private val latestTreeListener = treeCollectionReference
//        .orderBy("dateSpotted", Query.Direction.DESCENDING)
//        .limit(10)
//        .addSnapshotListener{ snapshot, error ->
//            if (error != null) {
//                Log.e(TAG, "Error fetching latest trees", error)
//            }
//            if (snapshot != null) {
//                val trees = mutableListOf<Tree>()
//                for (treeDocument in snapshot) { // loops over each item in our Firebase query
//                    val tree = treeDocument.toObject(Tree::class.java) // converts the item to a Tree object
//                    tree.documentReference = treeDocument.reference // Saves the referenceID (Prim Key) from Firebase
//                    trees.add(tree) // adds the tree object to a list of tree objects
//                }
//
//                Log.d(TAG, "Trees from firebase: $trees")
//                latestTrees.postValue(trees) // this posts values to our mutable live data
//            }
//        }
    private val latestTreesListener = treeCollectionReference
        .orderBy("dateSpotted", Query.Direction.DESCENDING)
        .limit(10)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG,"Error getting latest trees", error)
            }
            if (snapshot != null) {
                // Simplest way - convert the snapshot to tree objects.
                // val trees = snapshot.toObjects(Tree::class.java)

                // However, we want to store the tree references so we'll need to loop and
                // convert, and add the document references
                val trees = mutableListOf<Tree>()
                for (treeDocument in snapshot) {
                    val tree = treeDocument.toObject(Tree::class.java)
                    tree.documentReference = treeDocument.reference
                    trees.add(tree)
                }
                Log.d(TAG, "Trees from firebase: $trees")
                latestTrees.postValue(trees)
            }
        }

    fun setIsFavorite(tree: Tree, favorite: Boolean) {
        Log.d(TAG, "Updating tree $tree to favorite $favorite")
        tree.favorite = favorite
        tree.documentReference?.update("favorite", favorite)
    }

    fun addTree(tree: Tree) {
        // Adds a tree to our firebase database
        treeCollectionReference.add(tree)
            .addOnSuccessListener { treeDocRef ->
            Log.d(TAG, "New tree added to ${treeDocRef.path}")
        }.addOnFailureListener { error ->
            Log.e(TAG, "Error adding tree $tree", error)
        }
    }

    fun deleteTree(tree: Tree) {
        //Deletes based on document ref.
        tree.documentReference?.delete()
    }

}
