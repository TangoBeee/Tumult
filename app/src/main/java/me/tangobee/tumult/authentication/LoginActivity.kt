package me.tangobee.tumult.authentication

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.hbb20.CountryCodePicker
import me.tangobee.tumult.MainActivity
import me.tangobee.tumult.R
import me.tangobee.tumult.authentication.verification.OTPService
import me.tangobee.tumult.functions.TransparentWidnow
import me.tangobee.tumult.model.MobileNumber

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var countryCode : CountryCodePicker

    private lateinit var helpSupport : TextView
    private lateinit var newMember : TextView

    private lateinit var login : Button

    private lateinit var phoneInput : EditText

    private lateinit var auth: FirebaseAuth


    private lateinit var dialog : Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Making Action and Nav bar INVISIBLE
        val transWindow = TransparentWidnow()
        transWindow.transparentWindow(window, Color.BLACK)

        setContentView(R.layout.activity_login)

        //finding views by their IDs ->
        helpSupport = findViewById(R.id.help)
        newMember = findViewById(R.id.newMember)
        login = findViewById(R.id.login)
        phoneInput = findViewById(R.id.phoneno_inp)
        countryCode = findViewById(R.id.countryCode)

        auth = FirebaseAuth.getInstance()

        dialog = Dialog(this@LoginActivity)
        dialog.setContentView(R.layout.dialog_wait)
        dialog.setCancelable(false)

        //setting on click listener ->
        helpSupport.setOnClickListener(this)
        newMember.setOnClickListener(this)
        login.setOnClickListener(this)

    }

    //last button clicked on? ->
    private var mLastClickTime = 0
    override fun onClick(v: View?) {
        if(SystemClock.elapsedRealtime() - mLastClickTime < 500)
            return

        mLastClickTime = SystemClock.elapsedRealtime().toInt()

        when(v?.id) {
            R.id.help -> {
                val tabBuilder = CustomTabsIntent.Builder()
                val tabs = tabBuilder.build()

                tabs.launchUrl(this, Uri.parse("https://tangobee.netlify.app/tumult/help-and-support.html"))
            }

            R.id.newMember -> {
                val loginActivityIntent = Intent(this@LoginActivity, SignupActivity::class.java)
                startActivity(loginActivityIntent)
                finish()
            }

            R.id.login -> {
                if (!TextUtils.isEmpty(phoneInput.text.toString().trim()) && Patterns.PHONE.matcher(phoneInput.text.toString().trim()).matches() && (phoneInput.text.toString().trim().length in 4..15)) {

                    dialog.show()

                    val phoneNumber = countryCode.selectedCountryCodeWithPlus + phoneInput.text.toString().trim()

                    val rootRef = FirebaseFirestore.getInstance()
                    val yourCollRef = rootRef.collection("Users")
                    val query: Query = yourCollRef.whereEqualTo("mobile.number", phoneInput.text.toString())
                    query.get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            var done = true
                            for (document in task.result) {
                                //CALL OTPService class
                                val otpService = OTPService(phoneNumber, this@LoginActivity, "", MobileNumber(countryCode.selectedCountryCodeWithPlus.toString(), phoneInput.text.toString().trim()), true)
                                otpService.otpService()
                                done = false
                            }

                            if(done) {
                                Toast.makeText(this@LoginActivity, "Account does not exist", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
                                finish()
                            }

                        } else {
                            Toast.makeText(this@LoginActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        }
                    }


                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Please enter a valid phone number.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}