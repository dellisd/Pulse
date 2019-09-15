package pulse.app

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

object DatabaseManager {
    private val database = FirebaseDatabase.getInstance().reference
    private lateinit var userToken: String


    fun writeAuthToken(token: String, expiresAt: Long) {
        database.child("users").child(token).setValue(User(token, expiresAt))
        userToken = token
    }

    fun writeLocation(location: Location) {
        database.child("users").child(userToken).child("location").setValue(location)
    }

    fun subscribeToSongs(callback: (List<Song>) -> Unit) {
        database.child("songs").addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildAdded(snapshot: DataSnapshot, p1: String?) {
                Log.d("DatabaseManager", "Child Added")
                val songs = snapshot.children.map {
                    it.getValue(Song::class.java)!!
                }

                callback(songs)
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }
        })

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