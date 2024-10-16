package za.co.varsitycollege.st10215473.community.data

import com.google.firebase.Timestamp
import java.time.LocalTime
import java.util.Date

class Customer(
    val userId: String= "",
    val IdNumber: String = "",
    val Name: String ="",
    val Surname: String ="",
    val PhoneNumber: String ="",
    val Email: String ="",
    val Age: Int = 0,
    val Gender: String="",
    val DateOfBirth: Date? = null,
    val Citizenship: String = "",
    val Status: String = "",
    val DateSubmitted: Date? = null,
    val isOnline: Boolean = false,
    val lastMessage: String = "",
    val lastMessageTime: Timestamp? = null

){
    constructor():this("", "", "", "", "", "", 0, "", null, "", "", null, false, "", null)
}