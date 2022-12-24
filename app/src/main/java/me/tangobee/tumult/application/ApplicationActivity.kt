package me.tangobee.tumult.application

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import me.tangobee.tumult.R
import me.tangobee.tumult.application.applicationsfragments.CallingFragment
import me.tangobee.tumult.application.applicationsfragments.ChatFragment
import me.tangobee.tumult.application.applicationsfragments.GalleryFragment
import me.tangobee.tumult.application.applicationsfragments.ProfileViewFragment
import me.tangobee.tumult.functions.TransparentWidnow

class ApplicationActivity : AppCompatActivity() {

    private lateinit var topNavBarHeader: TextView

    private lateinit var contactAddBTN: FloatingActionButton

    private lateinit var bottomNavView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Making Action and Nav bar INVISIBLE
        val transWindow = TransparentWidnow()
        transWindow.transparentWindow(window, Color.BLACK)

        setContentView(R.layout.activity_application)

        //finding views by their id ->
        bottomNavView = findViewById(R.id.bottomNavBarView)
        contactAddBTN = findViewById(R.id.contactFAB)
        topNavBarHeader = findViewById(R.id.topNavBarHeader)


        val chat = "chat"
        topNavBarHeader.text = chat
        replaceFragment(ChatFragment())

        bottomNavView.background = null
        bottomNavView.menu.getItem(2).isEnabled = false
        bottomNavView.setOnItemSelectedListener {setOnItemSelectedListener(it)}

    }

    private fun setOnItemSelectedListener(menuItem: MenuItem): Boolean {
        when(menuItem.itemId) {
            R.id.miChat -> {
                val chat = "Chat"
                topNavBarHeader.text = chat
                replaceFragment(ChatFragment())

            }
            R.id.miCall-> {
                val call = "Calls"
                topNavBarHeader.text = call
                replaceFragment(CallingFragment())
            }
            R.id.miGallery -> {
                val photo = "Gallery"
                topNavBarHeader.text = photo
                replaceFragment(GalleryFragment())
            }
            R.id.miProfile -> {
                val profile = "Profile"
                topNavBarHeader.text = profile
                replaceFragment(ProfileViewFragment())
            }
        }

        return true
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransition = fragmentManager.beginTransaction()

        fragmentTransition.replace(R.id.frameApplication, fragment).commit()
    }


}