package za.co.varsitycollege.st10215473.community

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import za.co.varsitycollege.st10215473.community.adapter.MessagesAdapter
import za.co.varsitycollege.st10215473.community.data.Message
import za.co.varsitycollege.st10215473.community.databinding.ActivityServiceChatBinding
import java.lang.reflect.Array
import java.util.Calendar
import java.util.Date

class ServiceChatActivity : AppCompatActivity() {

    private lateinit var openCamera: ImageView
    private lateinit var profilePicture: ImageView
    private lateinit var backButton: ImageView
    private lateinit var openProfile: LinearLayout
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    var binding: ActivityServiceChatBinding? = null
    var adapter: MessagesAdapter? = null
    var messages: ArrayList<Message>? = null
    var senderRoom: String? = null
    var receiverRoom:String? = null
    var database: FirebaseFirestore? = null
    var storage: StorageReference? = null
    var dialog: ProgressDialog? = null
    var senderUid:String? = null
    var receiverUid: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServiceChatBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding!!.root) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(
                systemBarsInsets.left,
                systemBarsInsets.top,
                systemBarsInsets.right,
                systemBarsInsets.bottom
            )

            insets
        }
        profilePicture = findViewById(R.id.profilePic)
        openProfile = findViewById(R.id.OpenProfile)
        backButton = findViewById(R.id.backButton)

        backButton.setOnClickListener{
            onBackPressed()
        }

        openProfile.setOnClickListener {
            isConsumer(senderUid!!) { isConsumer ->
                if (isConsumer) {
                    val intent = Intent(this@ServiceChatActivity, ViewProfileActivity::class.java)
                    intent.putExtra("id", receiverUid) // Pass the service provider's ID
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Only Consumers can view profiles.", Toast.LENGTH_SHORT).show()
                }
            }
        }


        setSupportActionBar(binding!!.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        database = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance().reference
        dialog = ProgressDialog(this@ServiceChatActivity)
        dialog!!.setMessage("Uploading Image...")
        dialog!!.setCancelable(false)
        messages = ArrayList()
        val name = intent.getStringExtra("name")
        val profile = intent.getStringExtra("profileUrl")

        binding!!.name.text = name

        Glide.with(this)
            .load(profile)
            .placeholder(R.drawable.profile_circle)
            .error(R.drawable.profile_circle)
            .circleCrop()
            .into(profilePicture)

        receiverUid = intent.getStringExtra("id")
        senderUid = FirebaseAuth.getInstance().uid
        database!!.collection("Consumer").document(receiverUid!!)
            .addSnapshotListener{ snapshot, e ->
                if(snapshot!= null && snapshot.exists()){
                    val isOnline = snapshot.getString("online")
                    if(isOnline == "offline"){
                        binding!!.availability.visibility = View.GONE
                    }
                    else{
                        binding!!.availability.setText("online")
                        binding!!.availability.visibility = View.VISIBLE
                    }
                }
                if(e != null){
                    return@addSnapshotListener
                }
            }
        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid
        adapter = MessagesAdapter(this@ServiceChatActivity, messages, senderRoom!!, receiverRoom!!)
        binding!!.chatRV.layoutManager = LinearLayoutManager(this@ServiceChatActivity).apply {
            stackFromEnd = true  // Ensures new messages appear at the bottom
            reverseLayout = false  // New messages are not reversed in order
        }
        binding!!.chatRV.adapter = adapter
        database!!.collection("Chats").document(senderRoom!!).collection("messages")
            .orderBy("timeStamp") // Add this to ensure correct order
            .addSnapshotListener { snapshots, e ->
                if (snapshots != null && !snapshots.isEmpty) {
                    messages!!.clear()
                    for (document in snapshots.documents) {
                        val message = document.toObject(Message::class.java)
                        message!!.messageId = document.id
                        messages!!.add(message)
                    }
                    adapter!!.notifyDataSetChanged()
                    binding!!.chatRV.scrollToPosition(messages!!.size - 1)
                }
            }
        binding!!.send.setOnClickListener {
            val messageTxt: String = binding!!.messageBox.text.toString().trim()
            if (messageTxt.isEmpty()) return@setOnClickListener

            val date = Date()
            val message = Message(messageTxt, senderUid!!, date.time)
            binding!!.messageBox.setText("")

            val randomKey = database!!.collection("Chats").document().id
            message.messageId = randomKey

            val lastMsgObj = mapOf(
                "lastMessage" to message.message!!,
                "lastMessageTime" to Timestamp.now()
            )

            // Add the message to the sender's chat room
            database!!.collection("Chats").document(senderRoom!!)
                .set(lastMsgObj, SetOptions.merge())
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update last message: ${e.message}", Toast.LENGTH_SHORT).show()
                }

            database!!.collection("Chats").document(senderRoom!!)
                .collection("messages")
                .document(randomKey)
                .set(message)
                .addOnSuccessListener {
                    // Add the message to the receiver's chat room
                    database!!.collection("Chats").document(receiverRoom!!)
                        .set(lastMsgObj, SetOptions.merge())
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to update last message: ${e.message}", Toast.LENGTH_SHORT).show()
                        }

                    database!!.collection("Chats").document(receiverRoom!!)
                        .collection("messages")
                        .document(randomKey)
                        .set(message)
                }

            // Update last message in Consumer or ServiceProviders collections
            isServiceProvider(senderUid!!) { isSenderServiceProvider ->
                val senderCollection = if (isSenderServiceProvider) "ServiceProviders" else "Consumer"
                val receiverCollection = if (isSenderServiceProvider) "Consumer" else "ServiceProviders"

                database!!.collection(senderCollection).document(senderUid!!)
                    .set(lastMsgObj, SetOptions.merge())
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to update last message in $senderCollection: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

                database!!.collection(receiverCollection).document(receiverUid!!)
                    .set(lastMsgObj, SetOptions.merge())
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to update last message in $receiverCollection: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

//        takePictureLauncher =
//            registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
//                if (isSuccess) {
//                        Glide.with(this).load(profileUri).into()
//                }
//            }


        binding!!.attach.setOnClickListener{
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 25)
        }
        
        val handler = Handler()
        binding!!.messageBox.addTextChangedListener(object :TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().trim().isNotEmpty()) {
                    binding!!.send.isEnabled = true
                    binding!!.attach.visibility = View.GONE
                } else {
                    binding!!.send.isEnabled = false
                    binding!!.attach.visibility = View.VISIBLE
                }

                updateStatusField("typing...")
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedTyping, 1000)
            }
            var userStoppedTyping = Runnable {
                updateStatusField("online")
            }

        })
    }

    private fun isConsumer(uid: String, callback: (Boolean) -> Unit) {
        database!!.collection("Consumer").document(uid).get()
            .addOnSuccessListener { document ->
                callback(document.exists())
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    private fun updateStatusField(status: String) {
        isServiceProvider(senderUid!!) { isProvider ->
            val collection = if (isProvider) "ServiceProviders" else "Consumer"
            database!!.collection(collection).document(senderUid!!)
                .update("online", status)
                .addOnSuccessListener {
                    // Status updated successfully
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update status: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun isServiceProvider(uid: String, callback: (Boolean) -> Unit) {
        database!!.collection("ServiceProviders").document(uid).get()
            .addOnSuccessListener { document ->
                callback(document.exists())
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 25 && resultCode == RESULT_OK && data != null) {
            val selectedImage = data.data ?: return
            val calendar = Calendar.getInstance()
            val filePath = "Chats/${calendar.timeInMillis}"
            val reference = storage!!.child(filePath)

            dialog!!.show()
            reference.putFile(selectedImage)
                .addOnCompleteListener { task ->
                    dialog!!.dismiss()
                    if (task.isSuccessful) {
                        reference.downloadUrl.addOnSuccessListener { uri ->
                            val message = Message("photo", senderUid!!, Date().time)
                            message.imageUrl = uri.toString()

                            val randomKey = database!!.collection("Chats").document().id
                            message.messageId = randomKey

                            val lastMsgObj = mapOf(
                                "lastMessage" to "photo",
                                "lastMessageTime" to Timestamp.now()
                            )

                            // Add photo message to Firestore
                            database!!.collection("Chats").document(senderRoom!!)
                                .set(lastMsgObj, SetOptions.merge())
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Failed to update last message: ${e.message}", Toast.LENGTH_SHORT).show()
                                }

                            database!!.collection("Chats").document(senderRoom!!)
                                .collection("messages")
                                .document(randomKey)
                                .set(message)
                                .addOnSuccessListener {
                                    database!!.collection("Chats").document(receiverRoom!!)
                                        .set(lastMsgObj, SetOptions.merge())
                                        .addOnFailureListener { e ->
                                            Toast.makeText(this, "Failed to update last message: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }

                                    database!!.collection("Chats").document(receiverRoom!!)
                                        .collection("messages")
                                        .document(randomKey)
                                        .set(message)
                                }
                        }
                    }
                }
        }
    }


    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid

        if (currentId != null) {
            isServiceProvider(currentId) { isProvider ->
                val collection = if (isProvider) "ServiceProviders" else "Consumer"
                database!!.collection(collection).document(currentId)
                    .update("online", "online")
                    .addOnSuccessListener {
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to update status: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid

        if (currentId != null) {
            isServiceProvider(currentId) { isProvider ->
                val collection = if (isProvider) "ServiceProviders" else "Consumer"
                database!!.collection(collection).document(currentId)
                    .update("online", "offline")
                    .addOnSuccessListener {
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to update status: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

}