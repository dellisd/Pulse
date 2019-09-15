package pulse.app.login.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.onboard_fragment.*
import pulse.app.R

var userName = "Listener24"
var userIcon = 2;
var iconSrc = R.drawable.ic_icon2

class OnboardFragment : Fragment(R.layout.onboard_fragment) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button3.setOnClickListener {
            if(inputName.text.toString() != "") {
                userName = inputName.text.toString();
            }
            findNavController().navigate(R.id.mapFragment);
        }

        select1.isVisible = false;
        select2.isVisible = true;
        select3.isVisible = false;

        icon1.setOnClickListener {
            userIcon = 1;
            select1.isVisible = true;
            select2.isVisible = false;
            select3.isVisible = false;
            iconSrc = R.drawable.ic_icon1
        }

        icon2.setOnClickListener {
            userIcon = 2;
            select1.isVisible = false;
            select2.isVisible = true;
            select3.isVisible = false;
            iconSrc = R.drawable.ic_icon2
        }

        icon3.setOnClickListener {
            userIcon = 3;
            select1.isVisible = false;
            select2.isVisible = false;
            select3.isVisible = true;
            iconSrc = R.drawable.ic_icon3
        }

    }
}