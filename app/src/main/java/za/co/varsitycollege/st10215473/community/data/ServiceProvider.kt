package za.co.varsitycollege.st10215473.community.data

import com.google.firebase.Timestamp
import java.util.Date

class ServiceProvider(
    val id: String = "",
    val idNumber: String = "",
    val name: String = "",
    val surname: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val age: Int = 0,
    val dateOfBirth: Date? = null,
    val status: String = "",
    val registrationDate: Date? = null,  // Added property
    val isOnline: Boolean = false,
    val lastMessage: String = "",
    val lastMessageTime: Timestamp? = null,
    val image1: String = "",
    val image2: String = "",
    val image3: String = "",
    val image4: String = "",
    val bio: String = "",
    val category: String = "",
    val subCategory: String = "",
    val profileUrl: String = "",
    val fcmToken: String = "",
    val profilePic: String = "",      // Added property
    val description: String = "",     // Added property
    val address: String = "",         // Added property
    val city: String = "",            // Added property
    val postalCode: String = ""       // Added property
) {
    constructor() : this("", "", "", "", "", "", 0, null, "", null, false, "", null, "", "", "", "", "", "", "", "", "", "", "", "", "")
}
