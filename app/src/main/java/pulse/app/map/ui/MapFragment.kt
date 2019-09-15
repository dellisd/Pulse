package pulse.app.map.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationServices
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import kotlinx.android.synthetic.main.map_fragment.*
import pulse.app.BuildConfig
import pulse.app.DatabaseManager
import pulse.app.Location
import pulse.app.R
import pulse.app.login.ui.iconSrc
import pulse.app.login.ui.userName
import pulse.app.viz.MapVisualizer

class MapFragment : Fragment(R.layout.map_fragment) {

    private lateinit var map: MapboxMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(requireContext(), BuildConfig.MAPBOX_KEY)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userField.text = userName

        mapUserIcon.setImageResource(iconSrc)

        mainIcon.onCreate(savedInstanceState)
        mainIcon.getMapAsync {
            map = it
            syncLocation()
            map.setStyle("mapbox://styles/dellisd/ck0k7ghuz4gvi1dqn0g62gvp6") { style ->
                DatabaseManager.subscribeToSongs { songs ->
                    MapVisualizer(requireContext(), map, songs).start()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mainIcon.onStart()
    }

    override fun onPause() {
        super.onPause()
        mainIcon.onPause()
    }

    override fun onStop() {
        super.onStop()
        mainIcon.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainIcon.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mainIcon.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mainIcon.onSaveInstanceState(outState)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        syncLocation(grantResults[0] == PackageManager.PERMISSION_GRANTED)
    }

    private fun syncLocation(request: Boolean = true) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && request
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        } else {
            val client = LocationServices.getFusedLocationProviderClient(requireContext())
            client.lastLocation.addOnSuccessListener { location ->
                map.cameraPosition =
                    CameraPosition.Builder().target(LatLng(location.latitude, location.longitude))
                        .zoom(15.0)
                        .build()

                DatabaseManager.writeLocation(Location(location.latitude, location.longitude))
            }
        }
    }
}