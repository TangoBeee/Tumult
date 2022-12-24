package me.tangobee.tumult.model

import com.google.firebase.firestore.PropertyName
import java.util.*

data class UserProfile(
    var uId: String,
    @get:PropertyName("user_name")
                       @set:PropertyName("user_name")
    var userName: String="",
    var image:String="https://i.ibb.co/2MKcTxt/Untitled-removebg.png",
    var mobile: MobileNumber?=null)

data class MobileNumber(
    @get:PropertyName("country_code")
    @set:PropertyName("country_code")
    var countryCode: String="",
    var number: String="")