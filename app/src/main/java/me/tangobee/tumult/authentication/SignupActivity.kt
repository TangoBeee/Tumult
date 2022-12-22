package me.tangobee.tumult.authentication

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.Patterns
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.hbb20.CountryCodePicker
import me.tangobee.tumult.R
import me.tangobee.tumult.authentication.verification.OTPService
import me.tangobee.tumult.functions.TransparentWidnow
import me.tangobee.tumult.model.MobileNumber


class SignupActivity : AppCompatActivity(), OnClickListener {

    private lateinit var tos : TextView
    private lateinit var haveAccount : TextView

    private lateinit var countryCode : CountryCodePicker

    private lateinit var signup : Button

    private lateinit var usernameInput : EditText
    private lateinit var phoneInput : EditText

    private lateinit var auth: FirebaseAuth
    private lateinit var storageRef: StorageReference
    private lateinit var firebaseStore: FirebaseFirestore

    private lateinit var dialog : Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Making Action and Nav bar INVISIBLE
        val transWindow = TransparentWidnow()
        transWindow.transparentWindow(window, Color.BLACK)

        setContentView(R.layout.activity_signup)

        //finding views by their IDs ->
        tos = findViewById(R.id.tos)
        haveAccount = findViewById(R.id.havelogin)
        signup = findViewById(R.id.signup)
        usernameInput = findViewById(R.id.username_inp)
        phoneInput = findViewById(R.id.phoneno_inp)
        countryCode = findViewById(R.id.countryCode)


        dialog = Dialog(this@SignupActivity)
        dialog.setContentView(R.layout.dialog_wait)
        dialog.setCancelable(false)

        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance().reference.child("UserProfile")
        firebaseStore = FirebaseFirestore.getInstance()

        //setting on click listeners ->
        tos.setOnClickListener(this)
        haveAccount.setOnClickListener(this)
        signup.setOnClickListener(this)


        //Changing style of @haveAccount ->
        val text = "Already have an account? LOGIN"
        val spannableString = SpannableString(text)
        val foregroundColorSpanCyan = ForegroundColorSpan(Color.parseColor("#F72225"))
        spannableString.setSpan(foregroundColorSpanCyan, 25, text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        haveAccount.text = spannableString
    }

    //last button clicked on? ->
    private var mLastClickTime = 0
    override fun onClick(v: View?) {
        if(SystemClock.elapsedRealtime() - mLastClickTime < 500)
            return

        mLastClickTime = SystemClock.elapsedRealtime().toInt()


        usernameInput = findViewById(R.id.username_inp)
        phoneInput = findViewById(R.id.phoneno_inp)
        countryCode = findViewById(R.id.countryCode)

        when(v?.id) {
            R.id.tos -> {
                val tabBuilder = CustomTabsIntent.Builder()
                val tabs = tabBuilder.build()

                tabs.launchUrl(this, Uri.parse("https://tangobee.netlify.app/tumult/tos.html"))
            }

            R.id.havelogin -> {
                val loginActivityIntent = Intent(this@SignupActivity, LoginActivity::class.java)
                startActivity(loginActivityIntent)
                finish()
            }

            R.id.signup -> {

                if(TextUtils.isEmpty(phoneInput.text.toString().trim()) && TextUtils.isEmpty(usernameInput.text.toString().trim())) {
                    Toast.makeText(this, "Please fill all the fields.", Toast.LENGTH_SHORT).show()
                }

                else if(TextUtils.isEmpty(usernameInput.text.toString().trim())) {
                    Toast.makeText(this, "Please enter your username.", Toast.LENGTH_SHORT).show()
                }

                else if (!TextUtils.isEmpty(phoneInput.text.toString().trim()) && Patterns.PHONE.matcher(phoneInput.text.toString().trim()).matches() && (phoneInput.text.toString().trim().length in 4..15) && !TextUtils.isEmpty(usernameInput.text.toString().trim())) {

                    dialog.show()

                    val phoneNumber = countryCode.selectedCountryCodeWithPlus + phoneInput.text.toString().trim()
                    val username = usernameInput.text.toString().lowercase().trim()

                    val rootRef = FirebaseFirestore.getInstance()
                    val yourCollRef = rootRef.collection("Users")
                    val query: Query = yourCollRef.whereEqualTo("mobile.number", phoneInput.text.toString())
                    query.get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            var done = true
                            for (document in task.result) {
                                Toast.makeText(this, "Account already exists", Toast.LENGTH_SHORT)
                                    .show()
                                startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                                finish()
                                done = false
                                dialog.dismiss()
                            }

                            if(done) {
                                //CALL OTPService class
                                val otpService =
                                    OTPService(
                                        phoneNumber,
                                        this@SignupActivity,
                                        username,
                                        MobileNumber(
                                            countryCode.selectedCountryCodeWithPlus.toString(),
                                            phoneInput.text.toString().trim()
                                        ),
                                        false
                                    )
                                otpService.otpService()
                            }
                            dialog.dismiss()
                        }
                    }

                } else {
                    Toast.makeText(this, "Please enter a valid phone number.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}