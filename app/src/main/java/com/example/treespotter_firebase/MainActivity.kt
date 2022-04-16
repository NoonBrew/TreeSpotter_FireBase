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

    val CURRENT_FRAGMENT_BUNDLE_KEY = "Holds current fragment key"
    lateinit var currentFragmentTag: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        currentFragmentTag = savedInstanceState?.getString(CURRENT_FRAGMENT_BUNDLE_KEY) ?: "MAP"

        // Passes a fragment call to our function for display on create.
        showFragment(currentFragmentTag)
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
        currentFragmentTag = fragmentCall

        // Checks the to make sure the call is not null and begins commits a fragment call
        // based on the string passed by the menu item selected.
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

    override fun onSaveInstanceState(outState: Bundle) {
        // Saves the current fragment string name in an outState bundle so when the
        // activity is restored the current fragment is also restored.
        super.onSaveInstanceState(outState)
        outState.putString(CURRENT_FRAGMENT_BUNDLE_KEY, currentFragmentTag)
    }
}