package pulse.app.map.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.mapbox.mapboxsdk.Mapbox
import kotlinx.android.synthetic.main.map_fragment.*
import pulse.app.BuildConfig
import pulse.app.R

class MapFragment : Fragment(R.layout.map_fragment) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(requireContext(), BuildConfig.MAPBOX_KEY)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        map_view.onCreate(savedInstanceState)
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
}