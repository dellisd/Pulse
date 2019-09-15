package pulse.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.navigation.findNavController
import com.google.firebase.database.FirebaseDatabase
import com.spotify.sdk.android.authentication.AuthenticationClient
import com.spotify.sdk.android.authentication.AuthenticationResponse
import pulse.app.login.ui.LoginFragment
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LoginFragment.AUTHENTICATION_REQUEST) {
            val response = AuthenticationClient.getResponse(resultCode, data)
            if (response.type == AuthenticationResponse.Type.TOKEN) {
                Toast.makeText(this, "Signed in successfully", Toast.LENGTH_SHORT).show()


                DatabaseManager.writeAuthToken(
                    response.accessToken,
                    Date().time / 1000 + response.expiresIn
                )

                PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putString("userAccessToken", response.accessToken).apply()

                // TODO: Navigate to MAP

                findNavController(R.id.nav_fragment).navigate(R.id.onboardFragment)
            } else {
                Toast.makeText(this, "Signed in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun writeTokenToDatabase() {
        val database = FirebaseDatabase.getInstance().reference
    }
}
