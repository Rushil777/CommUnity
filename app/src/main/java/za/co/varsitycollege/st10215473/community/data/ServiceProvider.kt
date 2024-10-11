package za.co.varsitycollege.st10215473.community.data

import com.google.firebase.firestore.GeoPoint

class ServiceProvider (
    val userId: String = "",
    val email: String = "",
    val idNumber: Long = 0,
    val name: String = "",
    val surname: String = "",
    val gender: String ="",
    val age: Number = 0,
    val businessName: String= "",
    val review : Double? = null,
    val phoneNumber : Long = 0 ,
    val businessAddress : GeoPoint? = null,
    val role: String = "",
    val isOnline: Boolean = false,
    val lastMessage: String = "",
    val imageUrl: String? = null
)