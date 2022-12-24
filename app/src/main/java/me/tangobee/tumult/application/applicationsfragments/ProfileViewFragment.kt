package me.tangobee.tumult.application.applicationsfragments

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import me.tangobee.tumult.MainActivity
import me.tangobee.tumult.R
import me.tangobee.tumult.application.ApplicationActivity

class ProfileViewFragment : Fragment(), View.OnClickListener {

    private lateinit var signOut: Button
    private lateinit var savingInfo: Button

    private lateinit var uploadImg: ImageView

    private lateinit var auth : FirebaseAuth

    private lateinit var dialog: Dialog

    private var userSelectedImage = false
    private lateinit var imageUri: Uri
    private lateinit var phoneNumber: String
    private lateinit var uid: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile_view, container, false)

        //finding views by their id ->
        signOut = view.findViewById(R.id.signOut)
        savingInfo = view.findViewById(R.id.saveInfo)
        uploadImg = view.findViewById(R.id.uploadImg)

        auth = FirebaseAuth.getInstance()


        dialog = Dialog(requireActivity())
        dialog.setContentView(R.layout.dialog_wait)
        dialog.setCancelable(false)

        signOut.setOnClickListener(this)
        savingInfo.setOnClickListener(this)
        uploadImg.setOnClickListener(this)

        return view
    }

    //last button clicked on? ->
    private var mLastClickTime = 0
    override fun onClick(v: View?) {
        if(SystemClock.elapsedRealtime() - mLastClickTime < 500)
            return

        mLastClickTime = SystemClock.elapsedRealtime().toInt()

        when(v?.id) {
            R.id.signOut -> {
                dialog.show()
                AuthUI.getInstance()
                    .signOut(requireContext())
                    .addOnCompleteListener {
                        startActivity(Intent(requireActivity(), MainActivity::class.java))
                        requireActivity().finish()
                        dialog.dismiss()
                    }
            }

            R.id.uploadImg -> {
                resultLauncher.launch("image/*")
            }

            R.id.saveInfo -> {
                if(userSelectedImage) {
                    uploadImageToFirebase()
                }
                else {
                    Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show()
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
        uploadImg.setImageURI(it)
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
                Toast.makeText(context, e.message.toString(), Toast.LENGTH_SHORT).show()
            }
    }

}