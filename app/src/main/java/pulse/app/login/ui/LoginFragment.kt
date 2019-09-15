package pulse.app.login.ui

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.spotify.sdk.android.authentication.AuthenticationClient
import com.spotify.sdk.android.authentication.AuthenticationRequest
import com.spotify.sdk.android.authentication.AuthenticationResponse
import kotlinx.android.synthetic.main.login_fragment.*
import pulse.app.BuildConfig
import pulse.app.MainActivity
import pulse.app.R

class LoginFragment : Fragment(R.layout.login_fragment) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        button.setOnClickListener {
            val builder = AuthenticationRequest.Builder(
                BuildConfig.SPOTIFY_CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                "pulseapp://callback"
            )
            builder.setScopes(
                arrayOf(
                    "user-read-email",
                    "user-modify-playback-state",
                    "user-read-playback-state",
                    "user-read-currently-playing",
                    "user-top-read",
                    "user-read-recently-played",
                    "user-library-modify",
                    "user-library-read",
                    "user-follow-modify",
                    "user-follow-read",
                    "playlist-read-private",
                    "playlist-modify-public",
                    "playlist-modify-private",
                    "playlist-read-collaborative",
                    "user-read-private",
                    "app-remote-control",
                    "streaming"
                )
            )
            val request = builder.build()

            AuthenticationClient.openLoginActivity(
                requireActivity(),
                AUTHENTICATION_REQUEST,
                request
            )
        }
    }

    companion object {
        const val AUTHENTICATION_REQUEST = 1337
    }
}