package me.tangobee.tumult.authentication

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import me.tangobee.tumult.R
import me.tangobee.tumult.application.ApplicationActivity
import me.tangobee.tumult.functions.TransparentWidnow

class UploadProfilePicActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var userName : TextView
    private lateinit var skipBTN : TextView

    private lateinit var uploadImage : ImageView

    private lateinit var uploadImageButton : Button

    private var userSelectedImage = false

    private lateinit var imageUri: Uri

    private lateinit var phoneNumber: String
    private lateinit var uid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Making Action and Nav bar INVISIBLE
        val transWindow = TransparentWidnow()
        transWindow.transparentWindow(window, Color.BLACK)

        setContentView(R.layout.activity_upload_profile_pic)

        val username = "Welcome " + intent.getStringExtra("username")
        phoneNumber = intent.getStringExtra("phoneNumber").toString()
        uid = intent.getStringExtra("uid").toString()

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
                startActivity(Intent(this@UploadProfilePicActivity, ApplicationActivity::class.java))
                finish()
            }


            R.id.uploadImage -> {
                resultLauncher.launch("image/*")
            }


            R.id.uploadImageButton -> {
                if(userSelectedImage) {

                    uploadImageToFirebase()

                    startActivity(Intent(this@UploadProfilePicActivity, ApplicationActivity::class.java))
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
        if(it != null) {
            userSelectedImage = true
            imageUri = it
        }
        uploadImage.setImageURI(it)
    }

    private fun uploadImageToFirebase() {
        val fileName = "$phoneNumber.jpg"

        val db = FirebaseFirestore.getInstance()
        val refStorage = FirebaseStorage.getInstance().reference.child("userProfile/$fileName")

        refStorage.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                    val imageUrl = it.toString()

                    db.collection("Users")
                        .document(uid)
                        .update(mapOf(
                            "image" to imageUrl
                        ))
                }
            }

            .addOnFailureListener { e ->
                Toast.makeText(this@UploadProfilePicActivity, e.message.toString(), Toast.LENGTH_SHORT).show()
            }
    }


}