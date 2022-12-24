package me.tangobee.tumult.application.applicationsfragments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import me.tangobee.tumult.MainActivity
import me.tangobee.tumult.R

class ProfileViewFragment : Fragment(), View.OnClickListener {

    private lateinit var signOut: Button
    private lateinit var savingInfo: Button

    private lateinit var name: TextView
    private lateinit var helpSupport: TextView

    private lateinit var nameET: EditText

    private lateinit var uploadImg: ImageView

    private lateinit var auth : FirebaseAuth

    private lateinit var dialog: Dialog

    private var userSelectedImage = false
    private lateinit var imageUri: Uri
    private lateinit var phoneNumber: String
    private lateinit var username: String
    private lateinit var uid: String
    private var imageUrl: String = "https://i.ibb.co/2MKcTxt/Untitled-removebg.png"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile_view, container, false)

        //getting data from shared preference
        val sharedPref = requireActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        phoneNumber = sharedPref.getString("phoneno", "").toString()
        username = sharedPref.getString("username", "").toString()
        uid = sharedPref.getString("uid", "").toString()

        //finding views by their id ->
        signOut = view.findViewById(R.id.signOut)
        savingInfo = view.findViewById(R.id.saveInfo)
        uploadImg = view.findViewById(R.id.uploadImg)
        name = view.findViewById(R.id.name)
        nameET = view.findViewById(R.id.nameET)
        helpSupport = view.findViewById(R.id.helpsupport)

        auth = FirebaseAuth.getInstance()


        name.text = username

        dialog = Dialog(requireActivity())
        dialog.setContentView(R.layout.dialog_wait)
        dialog.setCancelable(false)

        signOut.setOnClickListener(this)
        savingInfo.setOnClickListener(this)
        uploadImg.setOnClickListener(this)
        helpSupport.setOnClickListener(this)

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
                        requireActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit().clear().apply()
                        startActivity(Intent(requireActivity(), MainActivity::class.java))
                        requireActivity().finish()
                        dialog.dismiss()
                    }
            }

            R.id.uploadImg -> {
                resultLauncher.launch("image/*")
            }

            R.id.saveInfo -> {
                if(userSelectedImage && TextUtils.isEmpty(nameET.text.trim())) {
                    uploadImageToFirebase()
                }

                else if(!userSelectedImage && !TextUtils.isEmpty(nameET.text.trim())) {
                    uploadNameToFirebase()
                }

                else if(userSelectedImage && !TextUtils.isEmpty(nameET.text.trim())) {
                    uploadInfoToFirebase()
                }

                else {
                    Toast.makeText(requireContext(), "Please enter a name or select an image", Toast.LENGTH_SHORT).show()
                }
            }

            R.id.helpsupport -> {
                val tabBuilder = CustomTabsIntent.Builder()
                val tabs = tabBuilder.build()

                tabs.launchUrl(requireContext(), Uri.parse("https://tangobee.netlify.app/tumult/help-and-support.html"))
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
                    imageUrl = it.toString()

                    db.collection("Users")
                        .document(uid)
                        .update(mapOf(
                            "image" to imageUrl
                        ))
                }
            }

            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), e.message.toString(), Toast.LENGTH_SHORT).show()
            }
    }


    private fun uploadNameToFirebase() {
        val db = FirebaseFirestore.getInstance()

        db.collection("Users")
            .document(uid)
            .update(mapOf(
                "user_name" to nameET.text.toString().trim()
            ))
        //updating username inside shared pref
        val sharedPref = requireActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        sharedPref.edit().putString("username", nameET.text.toString()).apply()
        username = sharedPref.getString("username", "").toString()
        name.text = username

        nameET.text.clear()
    }


    private fun uploadInfoToFirebase() {
        val fileName = "$phoneNumber.jpg"

        val db = FirebaseFirestore.getInstance()
        val refStorage = FirebaseStorage.getInstance().reference.child("userProfile/$fileName")

        refStorage.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                    imageUrl = it.toString()

                    db.collection("Users")
                        .document(uid)
                        .update(mapOf(
                            "image" to imageUrl,
                            "user_name" to nameET.text.toString().trim()
                        ))
                }


                //updating username inside shared pref
                val sharedPref = requireActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE)
                sharedPref.edit().putString("username", nameET.text.toString()).apply()
                username = sharedPref.getString("username", "").toString()
                name.text = username

                nameET.text.clear()
            }

            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), e.message.toString(), Toast.LENGTH_SHORT).show()
            }
    }


//    <-------------------- Getting imageURL from firestore --------------------->

//    private fun getImageURL() {
//        val db = FirebaseFirestore.getInstance().collection("Users").document(uid)
//
//        db.get().addOnCompleteListener { document ->
//            if(document.isSuccessful) {
//                imageUrl = document.result.getString("image").toString()
//            }
//            else {
//                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
}