package za.co.varsitycollege.st10215473.community.data

import com.google.firebase.Timestamp
import java.time.LocalTime
import java.util.Date

class ServiceProvider(
    val id: String= "",
    val idNumber: String = "",
    val name: String ="",
    val surname: String ="",
    val phoneNumber: String ="",
    val email: String ="",
    val age: Int = 0,
    val dateOfBirth: Date? = null,
    val status: String = "",
    val dateSubmitted: Date? = null,
    val isOnline: Boolean = false,
    val lastMessage: String = "",
    val lastMessageTime: Timestamp? = null

){
    constructor():this("", "", "", "", "", "", 0, null, "", null, false, "", null)
}