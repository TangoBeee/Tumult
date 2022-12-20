package me.tangobee.tumult

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import me.tangobee.tumult.application.HomeActivity
import me.tangobee.tumult.authentication.LoginActivity
import me.tangobee.tumult.authentication.SignupActivity
import me.tangobee.tumult.functions.TransparentWidnow

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var howItWorksBTN: Button
    private lateinit var loginBTN: Button
    private lateinit var signupBTN: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Making Action and Nav bar INVISIBLE
        val transWindow = TransparentWidnow()
        transWindow.transparentWindow(window, Color.BLACK)

        Thread.sleep(2000)
        installSplashScreen()
        setContentView(R.layout.activity_main)

        //Finding views by their IDs
        howItWorksBTN = findViewById(R.id.howItWorks)
        loginBTN = findViewById(R.id.loginbtn)
        signupBTN = findViewById(R.id.signupbtn)

        auth = FirebaseAuth.getInstance()


        //Buttons on click listener
        howItWorksBTN.setOnClickListener(this)
        loginBTN.setOnClickListener(this)
        signupBTN.setOnClickListener(this)
    }


    //last button clicked on? ->
    private var mLastClickTime = 0
    override fun onClick(v: View?) {
        if(SystemClock.elapsedRealtime() - mLastClickTime < 500)
            return

        mLastClickTime = SystemClock.elapsedRealtime().toInt()

        when(v?.id) {
            R.id.howItWorks -> {
                val tabBuilder = CustomTabsIntent.Builder()
                val tabs = tabBuilder.build()

                tabs.launchUrl(this, Uri.parse("https://tangobee.netlify.app/tumult/how-it-works.html"))
            }

            R.id.loginbtn -> {
                val loginActivityIntent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(loginActivityIntent)
            }

            R.id.signupbtn -> {
                val signupActivityIntent = Intent(this@MainActivity, SignupActivity::class.java)
                startActivity(signupActivityIntent)
            }
        }
    }


    override fun onStart() {
        super.onStart()
        if(auth.currentUser != null) {
            startActivity(Intent(this@MainActivity, HomeActivity::class.java))
            finish()
        }
    }

}