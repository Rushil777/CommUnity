package za.co.varsitycollege.st10215473.community.data

import com.google.firebase.Timestamp

class Message{
    var messageId: String? = null
    var message: String? = null
    var senderId: String? = null
    var imageUrl: String? = null
    var timeStamp: Long? = 0

    constructor(){}
    constructor(
        message: String?,
        senderId: String,
        timeStamp: Long
    ){
        this.message = message
        this.senderId = senderId
        this.timeStamp = timeStamp

    }

}