package za.co.varsitycollege.st10215473.community.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
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
    val online: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Timestamp? = null,
    val image1: String = "",
    val image2: String = "",
    val image3: String = "",
    val image4: String = "",
    val bio: String = "",
    val category: List<String>? = listOf(),
    val subCategory: List<String>? = listOf(),
    val profileUrl: String = "",
    val fcmToken: String = "",
    val description: String = "",
    val address: String = "",
    val city: String = "",
    val postalCode: String = "",
    val averageRating: Double? = null,
    val dateSubmitted: Date? = null,
    val location: GeoPoint? = null
)
{
    constructor() : this("", "", "", "", "", "", 0, null, "", null, "", "", null, "", "", "", "", "", null, null, "", "", "", "", "", "", null, null, null)
}
