package za.co.varsitycollege.st10215473.community.data

//ok change 2
import com.google.firebase.Timestamp
import java.time.LocalTime
import java.util.Date


class Customer(
    val Id: String= "",
    val IdNumber: String = "",
    val Name: String ="",
    val Surname: String ="",
    val PhoneNumber: String ="",
    val Email: String ="",
    val Age: Int = 0,
    val DateOfBirth: Date? = null,
    val Status: String = "",
    val DateSubmitted: Date? = null,
    val isOnline: Boolean = false,
    val lastMessage: String = "",
    val lastMessageTime: Timestamp? = null

){
    constructor():this("", "", "", "", "", "", 0, null, "", null, false, "", null)
}