package za.co.varsitycollege.st10215473.community.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import java.util.Date

class Customer(
    val id: String = "",
    val idNumber: String = "",
    val name: String = "",
    val surname: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val age: Int = 0,
    val dateOfBirth: Date? = null,
    val status: String = "",
    val dateSubmitted: Date? = null,
    val online: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Timestamp? = null,
    val fcmToken: String = "", // Added FCM token field
    val location: GeoPoint? = null, // Add location field
    val profileUrl: String? = null
) {
    constructor() : this("", "", "", "", "", "", 0, null, "", null, "", "", null, "", null, null)
}
