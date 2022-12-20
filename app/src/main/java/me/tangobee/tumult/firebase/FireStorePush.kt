package me.tangobee.tumult.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import me.tangobee.tumult.model.UserProfile

class FireStorePush {

    fun insertUser(user: UserProfile) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Users").document(user.uId)
            .set(user, SetOptions.merge())
            .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w("TAG", "Error writing document", e) }
    }

}