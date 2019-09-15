package pulse.app.map.ui

import android.Manifest
import android.R.attr.action
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
import pulse.app.*
import pulse.app.login.ui.iconSrc
import pulse.app.login.ui.userName
import pulse.app.viz.MapVisualizer

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
import android.util.Log
import android.R.attr.track
import com.bumptech.glide.Glide

//import android.R

var activeSong = false;

class MapFragment : Fragment(R.layout.map_fragment) {

    private lateinit var map: MapboxMap
    private val mSpotifyAppRemote: SpotifyAppRemote? = null

    private var currentVisualizer: MapVisualizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(requireContext(), BuildConfig.MAPBOX_KEY)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (!activeSong) {
            actionButton.setImageResource(R.drawable.ic_pause_black_24dp);
        } else {
            actionButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }

        super.onViewCreated(view, savedInstanceState)

        moveLocation.setOnClickListener {
            syncLocation()
        }

        actionButton.setOnClickListener {
            if (activeSong) {
                actionButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
            } else {
                actionButton.setImageResource(R.drawable.ic_pause_black_24dp);
            }
            activeSong = !activeSong
        }

        userField.text = userName

        mapUserIcon.setImageResource(iconSrc)

        mainIcon.onCreate(savedInstanceState)
        mainIcon.getMapAsync {
            map = it
            map.setStyle("mapbox://styles/dellisd/ck0k7ghuz4gvi1dqn0g62gvp6") { style ->
                syncLocation()
            }

            map.addOnCameraIdleListener {
                val target = map.cameraPosition.target
                DatabaseManager.subscribeToSongs(
                    Location(
                        target.latitude,
                        target.longitude
                    )
                ) { songs ->
                    switchVisualizer(songs)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mainIcon.onStart()
        val connectionParams = ConnectionParams.Builder(BuildConfig.SPOTIFY_CLIENT_ID)
            .setRedirectUri("pulseapp://callback")
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(requireContext(), connectionParams,
            object : Connector.ConnectionListener {

                override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
//                        SpotifyAppRemote = spotifyAppRemote;
                    Log.d("MainActivity", "Connected! Yay!");

                    // Now you can start interacting with App Remote
//                        connected();
                    spotifyAppRemote.playerApi
                        .subscribeToPlayerState()
                        .setEventCallback { playerState ->
                            val track = playerState.track
                            if (track != null) {
                                Log.d("MainActivity", track.name + " by " + track.artist.name)
                                titleBox.text = track.name
                                artistBox.text = track.artist.name
                                Glide.with(this@MapFragment)
                                    .load(track.imageUri)
                                    .into(coverArt)

//                                coverArt.setImageResource(track.imageUri)
                            }
                        }

                }

                override fun onFailure(throwable: Throwable) {
                    Log.e("MyActivity", throwable.message, throwable);
                    // Something went wrong when attempting to connect! Handle errors here
                }
            });
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
                DatabaseManager.subscribeToSongs(
                    Location(
                        location.latitude,
                        location.longitude
                    )
                ) { songs ->
                    switchVisualizer(songs)
                }
            }
        }
    }

    private fun switchVisualizer(songs: List<Song>) {
        currentVisualizer?.stop()

        currentVisualizer = MapVisualizer(requireActivity(), map, songs)
        currentVisualizer?.start()
    }
}