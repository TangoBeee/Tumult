package me.tangobee.tumult.application

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import me.tangobee.tumult.MainActivity
import me.tangobee.tumult.R

class HomeActivity : AppCompatActivity() {

    private lateinit var signout : Button
    private lateinit var auth : FirebaseAuth

    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        signout = findViewById(R.id.signout)
        auth = FirebaseAuth.getInstance()


        dialog = Dialog(this@HomeActivity)
        dialog.setContentView(R.layout.dialog_wait)
        dialog.setCancelable(false)

        signout.setOnClickListener {
            dialog.show()
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            dialog.dismiss()
        }

    }
}