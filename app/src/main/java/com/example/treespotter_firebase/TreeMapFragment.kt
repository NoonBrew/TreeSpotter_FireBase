package com.example.treespotter_firebase

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.GeoPoint
import java.util.*

private const val TAG = "TREE_MAP_FRAGMENT"

class TreeMapFragment : Fragment() {

    private lateinit var addTreeButton: FloatingActionButton

    private var locationPermissionGranted = false // Checks for permission to access location

    private var mapStartingPointLaunched = false // Checks to see if we zoomed in on current location

    private var fusedLocationProvider: FusedLocationProviderClient? = null

    private var map: GoogleMap? = null //Holds an instance of our google map

    private val treeMarkers = mutableListOf<Marker>() // Mutable list of map markers

    private var treeList = listOf<Tree>() // List of our tree objects grabbed from view model

    private val treeSpotterViewModel: TreeSpotterViewModel by lazy {
        ViewModelProvider(requireActivity()).get(TreeSpotterViewModel::class.java)
    }

    private val mapReadyCallback = OnMapReadyCallback { googleMap ->

        Log.d(TAG, "Google map ready")
        map = googleMap // Instantiate a GoogleMap

        googleMap.setOnInfoWindowClickListener { marker ->
            // Sets the value based on the object stored in the marker.
            val treeForMarker = marker.tag as Tree
            // calls our requestToDelete function with the Tree Object passed to treeForMarker
            requestDeleteTree(treeForMarker)
        }
        updateMap()
    }

    private fun requestDeleteTree(tree: Tree) {
        // Creates an alert dialog that notify user when they attempt to delete a tree
        AlertDialog.Builder(requireActivity())
            .setTitle(getString(R.string.delete))
            .setMessage(getString(R.string.confirm_delete_tree, tree.name))
            .setPositiveButton(android.R.string.ok) { dialog, id ->
                treeSpotterViewModel.deleteTree(tree)
            }
            .setNegativeButton(android.R.string.cancel) { dialog, id ->
                // do nothing
            }
            .create()
            .show()

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val mainView = inflater.inflate(R.layout.fragment_tree_map, container, false)

        addTreeButton = mainView.findViewById(R.id.add_tree_fab)
        addTreeButton.setOnClickListener{
            treeNameEditTextDialog()
//            addTreeLocation()
        }
        // Checks to see if there is a SupportMapFragment and calls the Callback function to be
        // informed when the map is ready. SupportMapFragment is the type of container we have
        // for the layout of the Map fragment.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_view) as SupportMapFragment?
        mapFragment?.getMapAsync(mapReadyCallback)

        // disable add tree button until location is available.
        setAddTreeButtonEnabled(false)

        // request user's permission ot access device location
        requestLocationPermission()

        // Gets the latest trees from the view model and observes as they change.
        treeSpotterViewModel.latestTrees.observe(requireActivity()) { latestTrees ->
            treeList = latestTrees
            drawTrees()
        }



        return mainView
    }

    companion object {

        @JvmStatic
        fun newInstance() = TreeMapFragment()
    }

    @SuppressLint("MissingPermission")
    private fun addTreeLocation(treeName: String) {
        // If the map, LocationProvider, or Location Permission is not available
        // the function return and no tree will be added.
        if (map == null) { return }
        if (fusedLocationProvider == null) { return }
        if (!locationPermissionGranted) {
            showSnackbar(getString(R.string.grant_location_permission))
            return
        }

        // This adds a tree with the longitude and latitude of the location of the GPS, not the
        // not for the area the map is showing.
        fusedLocationProvider?.lastLocation?.addOnCompleteListener(requireActivity()){ locationReqTask ->
            // Last location
            val location = locationReqTask.result
            if (location != null) {
                val tree = Tree(
                    name = treeName,
                    dateSpotted = Date(),
                    location = GeoPoint(location.latitude, location.longitude)
                )
                treeSpotterViewModel.addTree(tree)

                moveMapToUserLocation() // moves back to user location.
                showSnackbar(getString(R.string.added_tree, treeName))
            } else {
                showSnackbar(getString(R.string.no_location))
            }
        }
    }

    private fun drawTrees() {
        // This function will only attempt to mark the location of the trees on the map
        // if the map is available
        if (map == null) { return }

        for (marker  in treeMarkers) {
            marker.remove()
        }

        for (tree in treeList) {
            // make a marker for each tree and add to the map
            tree.location?.let { geoPoint ->

                val isFavorite = tree.favorite ?: false

                // Sets the marker style based on whether the three is a favorite or not.
                val iconId = if( isFavorite) R.drawable.tree_marker_gold else R.drawable.tree_marker

                val markerOptions = MarkerOptions()
                    .position(LatLng(geoPoint.latitude, geoPoint.longitude))
                    .title(tree.name)
                    .snippet("Spotted on ${tree.dateSpotted}")
                    .icon(BitmapDescriptorFactory.fromResource(iconId))

                map?.addMarker(markerOptions)?.also { marker ->
                    treeMarkers.add(marker)
                    marker.tag = tree
                }
            }
        }

    }

//    private fun getTreeName(): String {
//
//       return treeNameEditTextDialog()
//
//    }

    private fun updateMap() {

        drawTrees() // when the map is ready the trees get called again to draw.
        if(locationPermissionGranted) {
            if(!mapStartingPointLaunched){
                moveMapToUserLocation()
            }
        }
        //
    }

    private fun setAddTreeButtonEnabled(isEnabled: Boolean) {
        // Sets the button to be clickable if true, not if false.
        // Color changes depending on the state of the button.
        addTreeButton.isClickable = isEnabled
        addTreeButton.isEnabled = isEnabled

        if (isEnabled){
            addTreeButton.backgroundTintList = AppCompatResources.getColorStateList(requireActivity(),
                android.R.color.holo_green_light)
        }else {
            addTreeButton.backgroundTintList = AppCompatResources.getColorStateList(requireActivity(),
                android.R.color.darker_gray)
        }
    }

    private fun showSnackbar (message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity(), // Checks to see if the app has location permission
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {


            locationPermissionGranted = true
            fusedLocationProvider = LocationServices.getFusedLocationProviderClient(requireActivity())
            setAddTreeButtonEnabled(true)
            Log.d(TAG, "permission already granted")
            updateMap()
        } else {
            val requestLocationPermissionLauncher =
                registerForActivityResult(ActivityResultContracts.RequestPermission()){ granted ->
                    if (granted) {
                        Log.d(TAG, "User granted permission")
                        setAddTreeButtonEnabled(true) // look into requireActivity context definition.
                        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(requireActivity())
                    } else {
                        Log.d(TAG, "You did not grant permission")
                        setAddTreeButtonEnabled(false)
                        locationPermissionGranted = false
                        showSnackbar(getString(R.string.give_permission))
                    }

                    updateMap()
                }
            // Prompts the user for location permission
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    private fun moveMapToUserLocation() {
        // map references our global google map. If the map is not ready we return
        if (map == null) {
            return
        }

        if (locationPermissionGranted) {
            map?.isMyLocationEnabled = true // Suppressed warning since it relies on permission
            map?.uiSettings?.isMyLocationButtonEnabled = true
            map?.uiSettings?.isZoomControlsEnabled = true

            fusedLocationProvider?.lastLocation?.addOnCompleteListener{ getLocationTask ->
                val location = getLocationTask.result // no location available = null
                if (location != null) {
                    Log.d(TAG, "users location $location")
                    val center = LatLng(location.latitude, location.longitude) // pulls the lat long

                    val zoomLevel = 17f
                    // Takes a new Lat and Long and zoom level.
                    map?.moveCamera(CameraUpdateFactory.newLatLngZoom(center, zoomLevel))

                    mapStartingPointLaunched = true
                } else {
                    showSnackbar(getString(R.string.no_location))
                }

            }
        }
    }

    private fun treeNameEditTextDialog(){

        val builder = AlertDialog.Builder(requireActivity())
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.tree_name_dialog, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.tree_name_edit_text)

        // When the add tree button is pressed the user is prompted to give the tree a name

        with(builder){
            setTitle("Tree name")
            setPositiveButton("Add Tree"){dialog, id ->
                if(editText.text.toString().isNotBlank()){
                    addTreeLocation(editText.text.toString())
                }else{
                    showSnackbar(getString(R.string.no_tree_name_warning))
                }
            }
            setNegativeButton("Cancel"){dialog, id ->
                Log.d(TAG, "Tree name dialog canceled")
                showSnackbar(getString(R.string.no_tree_added))
            }
            setView(dialogLayout).show()
        }

    }
}