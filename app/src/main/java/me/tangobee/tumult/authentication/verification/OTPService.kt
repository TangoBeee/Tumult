package me.tangobee.tumult.authentication.verification

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import me.tangobee.tumult.application.ApplicationActivity
import me.tangobee.tumult.authentication.UploadProfilePicActivity
import me.tangobee.tumult.firebase.FireStorePush
import me.tangobee.tumult.model.MobileNumber
import me.tangobee.tumult.model.UserProfile
import java.util.concurrent.TimeUnit

class OTPService(private val phoneNumber : String,
                 private val activity: Activity,
                 private val username: String,
                 private val mobileNumberObject: MobileNumber,
                 private val isLoggedIn: Boolean) {

    private lateinit var auth: FirebaseAuth

    fun otpService() {

        //getting instance
        auth = Firebase.auth


        /* SENDING OTP */
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
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
                Toast.makeText(activity, e.toString(), Toast.LENGTH_SHORT).show()
            }

            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.

            // Save verification ID and resending token so we can use them later
//            storedVerificationId = verificationId
//            resendToken = token

            //Starting my OTP dialog box UI ->
            val otpVerificationDialog = OTPVerificationDialog(activity, phoneNumber, username, verificationId, token, activity, mobileNumberObject, isLoggedIn)
            otpVerificationDialog.setCancelable(false)
            otpVerificationDialog.show()
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
//                    val user = task.result?.user


                    if(!isLoggedIn) {
                        auth.currentUser?.let { firebaseStoreHelper(it.uid) }

                        activity.startActivity(Intent(activity, UploadProfilePicActivity::class.java).putExtra("username", username).putExtra("phoneNumber", phoneNumber).putExtra("uid", auth.currentUser?.uid).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                        activity.finish()
                    }
                    else {
                        activity.startActivity(Intent(activity, ApplicationActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
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