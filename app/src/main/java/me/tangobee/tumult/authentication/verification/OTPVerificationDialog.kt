package me.tangobee.tumult.authentication.verification

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import me.tangobee.tumult.R
import me.tangobee.tumult.application.ApplicationActivity
import me.tangobee.tumult.authentication.UploadProfilePicActivity
import me.tangobee.tumult.firebase.FireStorePush
import me.tangobee.tumult.model.MobileNumber
import me.tangobee.tumult.model.UserProfile
import java.util.concurrent.TimeUnit


class OTPVerificationDialog(context : Context,
                            phoneNum : String,
                            private val username :String,
                            private var otp: String,
                            private var resendToken: ForceResendingToken,
                            private val activity: Activity,
                            private val mobileNumberObject: MobileNumber,
                            private val isLoggedIn: Boolean) : Dialog(context), View.OnClickListener {

    private val mob = phoneNum

    private lateinit var otpET1 : EditText
    private lateinit var otpET2 : EditText
    private lateinit var otpET3 : EditText
    private lateinit var otpET4 : EditText
    private lateinit var otpET5 : EditText
    private lateinit var otpET6 : EditText

    private lateinit var mobileNumber : TextView

    private lateinit var resendOTP : TextView

    private lateinit var verifyOTP : AppCompatButton

    private var selectedOTPBox = 0
    private val resendTime : Long = 100
    private var resendEnable = false

    private lateinit var auth: FirebaseAuth

    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        window?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(context, android.R.color.transparent)))
        setContentView(R.layout.dialog_otp_verification)

        //finding views by their IDs ->
        otpET1 = findViewById(R.id.otpET1)
        otpET2 = findViewById(R.id.otpET2)
        otpET3 = findViewById(R.id.otpET3)
        otpET4 = findViewById(R.id.otpET4)
        otpET5 = findViewById(R.id.otpET5)
        otpET6 = findViewById(R.id.otpET6)

        mobileNumber = findViewById(R.id.mobileNumber)

        resendOTP = findViewById(R.id.resendOTP)
        verifyOTP = findViewById(R.id.verifyOTP)

        auth = FirebaseAuth.getInstance()

        dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_wait)
        dialog.setCancelable(false)

        //keyboard open on which otp box by default ->
        showKeyboard(otpET1)


        //When user fill all the box in otp ->
        val textChangeListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if(s?.isNotEmpty() == true) {
                    when(selectedOTPBox) {
                        0 -> {
                            selectedOTPBox = 1
                            showKeyboard(otpET2)
                        }
                        1 -> {
                            selectedOTPBox = 2
                            showKeyboard(otpET3)
                        }
                        2 -> {
                            selectedOTPBox = 3
                            showKeyboard(otpET4)
                        }
                        3 -> {
                            selectedOTPBox = 4
                            showKeyboard(otpET5)
                        }
                        4 -> {
                            selectedOTPBox = 5
                            showKeyboard(otpET6)
                        }

                        else -> {
                            verifyOTP.setBackgroundResource(R.drawable.otp_btn_black_red)
                        }
                    }
                }
            }

        }

        otpET1.addTextChangedListener(textChangeListener)
        otpET2.addTextChangedListener(textChangeListener)
        otpET3.addTextChangedListener(textChangeListener)
        otpET4.addTextChangedListener(textChangeListener)
        otpET5.addTextChangedListener(textChangeListener)
        otpET6.addTextChangedListener(textChangeListener)


        //Set Mobile Number ->
        mobileNumber.text = mob

        //Start resend otp counter ->
        startCountDownTimer()

        //setting on click listener ->
        resendOTP.setOnClickListener(this)

        //setting on click listener ->
        verifyOTP.setOnClickListener(this)

    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if(keyCode == KeyEvent.KEYCODE_DEL) {
            when(selectedOTPBox) {
                5 -> {
                    selectedOTPBox = 4
                    showKeyboard(otpET5)
                    verifyOTP.setBackgroundResource(R.drawable.otp_btn_black_brown)
                }
                4 -> {
                    selectedOTPBox = 3
                    showKeyboard(otpET4)
                    verifyOTP.setBackgroundResource(R.drawable.otp_btn_black_brown)
                }
                3 -> {
                    selectedOTPBox = 2
                    showKeyboard(otpET3)
                    verifyOTP.setBackgroundResource(R.drawable.otp_btn_black_brown)
                }
                2 -> {
                    selectedOTPBox = 1
                    showKeyboard(otpET2)
                    verifyOTP.setBackgroundResource(R.drawable.otp_btn_black_brown)
                }
                1 -> {
                    selectedOTPBox = 0
                    showKeyboard(otpET1)
                    verifyOTP.setBackgroundResource(R.drawable.otp_btn_black_brown)
                }

                else -> {

                    verifyOTP.setBackgroundResource(R.drawable.otp_btn_black_brown)
                }
            }
            return true
        }
        else {
            verifyOTP.setBackgroundResource(R.drawable.otp_btn_black_red)
            return super.onKeyUp(keyCode, event)
        }
    }

    private fun showKeyboard(otpET : EditText) {
        otpET.requestFocus()

        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(otpET, InputMethodManager.SHOW_IMPLICIT)
    }


    private fun startCountDownTimer() {
        resendOTP = findViewById(R.id.resendOTP)
        resendEnable = false
        resendOTP.setTextColor(Color.parseColor("#99000000"))

        object: CountDownTimer(resendTime*1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                resendOTP.text = context.getString(R.string.resendCodeTime, millisUntilFinished/1000)
            }

            override fun onFinish() {
                resendEnable = true
                val resendCode = context.getString(R.string.resendCode)
                resendOTP.text = resendCode
                resendOTP.setTextColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark))
            }

        }.start()
    }


    //last button clicked on? ->
    private var mLastClickTime = 0
    override fun onClick(v: View?) {
        if(SystemClock.elapsedRealtime() - mLastClickTime < 500)
            return

        mLastClickTime = SystemClock.elapsedRealtime().toInt()

        verifyOTP = findViewById(R.id.verifyOTP)


        when(v?.id) {
            R.id.resendOTP -> {
                if(resendEnable) {
                    resendOTP()
                    startCountDownTimer()
                }
            }

            R.id.verifyOTP -> {
                val getOTP = otpET1.text.toString().trim()+otpET2.text.toString().trim()+otpET3.text.toString().trim()+otpET4.text.toString().trim()+otpET5.text.toString().trim()+otpET6.text.toString().trim()
                if(getOTP.length != 6) {
                    verifyOTP.setBackgroundResource(R.drawable.otp_btn_black_brown)
                    Toast.makeText(context, "Invalid OTP", Toast.LENGTH_SHORT).show()
                }
                else {
                    dialog.show()
                    val credential : PhoneAuthCredential = PhoneAuthProvider.getCredential(
                        otp, getOTP
                    )

                    signInWithPhoneAuthCredential(credential)

                    verifyOTP.setBackgroundResource(R.drawable.otp_btn_black_red)
                    dialog.dismiss()
                    dismiss()
                }
            }
        }
    }

    private fun resendOTP() {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(mob)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity)                 // Activity (for callback binding)
            .setCallbacks(callbacks)               // OnVerificationStateChangedCallbacks
            .setForceResendingToken(resendToken)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(context, "Authentication Successful.", Toast.LENGTH_SHORT).show()
//                    val user = task.result?.user


                    if(!isLoggedIn) {
                        auth.currentUser?.let { firebaseStoreHelper(it.uid) }

                        activity.startActivity(Intent(activity, UploadProfilePicActivity::class.java).putExtra("username", username).putExtra("phoneNumber", mob).putExtra("uid", auth.currentUser?.uid).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                        dismiss()
                        activity.finish()
                    }
                    else {
                        Log.d("mainactivity", "testing")
                        activity.startActivity(Intent(context, ApplicationActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                        dismiss()
                        activity.finish()
                    }

                } else {
                    // Sign in failed, display a message and update the UI
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid

                        Toast.makeText(activity,
                            (task.exception as FirebaseAuthInvalidCredentialsException).localizedMessage?.toString()
                                ?: "ERROR", Toast.LENGTH_SHORT).show()
                        Log.e("ERROR", task.exception.toString())
                    }
                    // Update UI
                    Toast.makeText(activity, "Authentication Failed!", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Toast.makeText(activity, e.localizedMessage?.toString() ?: "ERROR", Toast.LENGTH_SHORT).show()
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Toast.makeText(activity, "SMS Quota has been exceeded!", Toast.LENGTH_SHORT).show()
            }

            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.

            // Save verification ID and resending token so we can use them later
//            storedVerificationId = verificationId
//            resendToken = token

            //Starting my OTP dialog box UI ->
            otp = verificationId
            resendToken = token
        }
    }

    private fun firebaseStoreHelper(uID: String) {
        val user = UserProfile(
            uID,
            username,
            "https://i.ibb.co/2MKcTxt/Untitled-removebg.png",
            mobileNumberObject
        )

        val inserting = FireStorePush()
        inserting.insertUser(user)
    }
}