package za.co.varsitycollege.st10215473.community.data

class Feed(
    val description: String = "",
    val imageUrls: List<String> = emptyList(),
    val userId: String = "",
    val name: String = "",
    val surname: String = "",
    val profileUrl: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
