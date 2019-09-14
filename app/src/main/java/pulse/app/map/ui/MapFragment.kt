package pulse.app.map.ui

import android.graphics.Color
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.FillExtrusionLayer
import com.mapbox.mapboxsdk.maps.MapboxMap
import kotlinx.android.synthetic.main.map_fragment.*
import pulse.app.BuildConfig
import pulse.app.R

class MapFragment : Fragment(R.layout.map_fragment) {

    private lateinit var map: MapboxMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(requireContext(), BuildConfig.MAPBOX_KEY)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        map_view.onCreate(savedInstanceState)
        map_view.getMapAsync {
            map = it
            syncLocation()
            map.setStyle(Style.LIGHT) { style ->
                val extrusion = FillExtrusionLayer("3d-buildings", "composite").apply {
                    sourceLayer = "building"
                    setFilter(eq(`var`("extrude"), true))
                    setProperties(
                        fillExtrusionColor(Color.LTGRAY),
                        fillExtrusionHeight(
                            get("height")
                        ),
                        fillExtrusionBase(
                            get("min-height")
                        ),
                        fillExtrusionOpacity(0.6f)
                    )
                }

                style.addLayer(extrusion)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        map_view.onStart()
    }

    override fun onPause() {
        super.onPause()
        map_view.onPause()
    }

    override fun onStop() {
        super.onStop()
        map_view.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        map_view.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        map_view.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        map_view.onSaveInstanceState(outState)
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
            }
        }
    }
}