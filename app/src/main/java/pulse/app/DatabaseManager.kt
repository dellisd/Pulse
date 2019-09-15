package pulse.app

import android.util.Log
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryDataEventListener
import com.firebase.geofire.GeoQueryEventListener
import com.google.firebase.database.*

object DatabaseManager {
    private val database = FirebaseDatabase.getInstance().reference
    private val geofire = GeoFire(database.child("geofire"))
    private lateinit var userToken: String
    private var currentListener: GeoQueryEventListener? = null


    fun writeAuthToken(token: String, expiresAt: Long) {
        database.child("users").child(token).setValue(User(token, expiresAt))
        userToken = token
    }

    fun writeLocation(location: Location) {
        database.child("users").child(userToken).child("location").setValue(location)
    }

    fun subscribeToSongs(location: Location, callback: (List<Song>) -> Unit) {
        currentListener = object : GeoQueryEventListener {
            override fun onGeoQueryReady() {
            }

            override fun onKeyEntered(key: String?, location: GeoLocation?) {
                key ?: return
                database.child("songs").child(key)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            Log.d("DatabaseManager", "Child Added")
                            val songs = dataSnapshot.children.mapNotNull {
                                val thing = it.getValue(Song::class.java)!!

                                thing.location ?: return@mapNotNull null
                                thing
                            }

                            callback(songs)
                        }
                    })
            }

            override fun onKeyMoved(key: String?, location: GeoLocation?) {
            }

            override fun onKeyExited(key: String?) {
            }

            override fun onGeoQueryError(error: DatabaseError?) {
            }
        }
        geofire.queryAtLocation(GeoLocation(location.latitude, location.longitude), 5.0)
            .addGeoQueryEventListener(currentListener)
    }

}

data class User(
    var token: String? = "",
    var expiresAt: Long = 0,
    var location: Location? = null
)

data class Location(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
)

data class Song(
    var songName: String? = "",
    var artists: String? = "",
    var apiID: String? = "",
    var albumName: String? = "",
    var location: Location? = null,
    var beats: List<Beat> = emptyList(),
    var progress: Long = 0L,
    var startTime: Long = 0L
)

data class Beat(
    var elementLoudness: Int = 0,
    var elementStart: Long = 0,
    var section: Int = 0
)