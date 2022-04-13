package com.example.treespotter_firebase


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

private const val TAG = "MAIN_ACTIVITY"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val database = Firebase.firestore
        // Passes a fragment call to our function for display on create.
        showFragment("MAP")
        // Wires up our Nav Bar. it will be persistent through fragment views
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // Nested lambdas
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            // Calls a different fragment depending on which Nav Bar item is selected.
            when (menuItem.itemId) {
                R.id.show_map -> {
                    showFragment("MAP")
                    true
                }
                R.id.show_list -> {
                    showFragment("LIST")
                    true
                }
                else -> {
                    false
                }
            }
        }


    }

    private fun showFragment(fragmentCall: String) {
        // if we are not seeing the fragment with a given fragmentCall, display it.

        if (supportFragmentManager.findFragmentByTag(fragmentCall) == null) {
            val transaction = supportFragmentManager.beginTransaction()
            when (fragmentCall) {
                "MAP" -> transaction.replace(R.id.fragmentContainerView,
                    TreeMapFragment.newInstance(), "MAP")
                "LIST" -> transaction.replace(R.id.fragmentContainerView,
                    TreeListFragment.newInstance(), "LIST")
            }
            transaction.commit()
        }
    }
}