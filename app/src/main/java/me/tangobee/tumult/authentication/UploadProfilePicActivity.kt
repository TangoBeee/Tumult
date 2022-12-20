package me.tangobee.tumult.authentication

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import me.tangobee.tumult.R
import me.tangobee.tumult.application.HomeActivity
import me.tangobee.tumult.functions.TransparentWidnow

class UploadProfilePicActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var userName : TextView
    private lateinit var skipBTN : TextView

    private lateinit var uploadImage : ImageView

    private lateinit var uploadImageButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Making Action and Nav bar INVISIBLE
        val transWindow = TransparentWidnow()
        transWindow.transparentWindow(window, Color.BLACK)

        setContentView(R.layout.activity_upload_profile_pic)

        val username = "Welcome " + intent.getStringExtra("username")

        //finding views by their ID ->
        userName = findViewById(R.id.welcUsername)
        skipBTN = findViewById(R.id.skip)
        uploadImage = findViewById(R.id.uploadImage)
        uploadImageButton = findViewById(R.id.uploadImageButton)


        //setting on click listener ->
        skipBTN.setOnClickListener(this)
        uploadImage.setOnClickListener(this)
        uploadImageButton.setOnClickListener(this)


        userName.text = username

    }


    private var mLastClickTime = 0
    override fun onClick(v: View?) {
        if(SystemClock.elapsedRealtime() - mLastClickTime < 500)
            return

        mLastClickTime = SystemClock.elapsedRealtime().toInt()

        when(v?.id) {
            R.id.skip -> {
                startActivity(Intent(this@UploadProfilePicActivity, HomeActivity::class.java))
                finish()
            }


            R.id.uploadImage -> {
                resultLauncher.launch("image/*")
            }


            R.id.uploadImageButton -> {
                if(uploadImage.drawable == AppCompatResources.getDrawable(this@UploadProfilePicActivity, R.drawable.userprofile)) {
                    startActivity(Intent(this@UploadProfilePicActivity, HomeActivity::class.java))
                    finish()
                }
                else {
                    Toast.makeText(this@UploadProfilePicActivity, "Please select an image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        uploadImage.setImageURI(it)
    }
}