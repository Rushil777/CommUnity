package za.co.varsitycollege.st10215473.community

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import za.co.varsitycollege.st10215473.community.adapter.MessagesAdapter
import za.co.varsitycollege.st10215473.community.data.Message
import za.co.varsitycollege.st10215473.community.databinding.ActivityServiceChatBinding
import java.lang.reflect.Array
import java.util.Calendar
import java.util.Date

class ServiceChatActivity : AppCompatActivity() {


    private lateinit var openProfile: LinearLayout
    var binding: ActivityServiceChatBinding? = null
    var adapter: MessagesAdapter? = null
    var messages: ArrayList<Message>? = null
    var senderRoom: String? = null
    var receiverRoom:String? = null
    var database: FirebaseFirestore? = null
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

        openProfile = findViewById(R.id.OpenProfile)

        openProfile.setOnClickListener{
            val intent = Intent(this@ServiceChatActivity, ViewProfileActivity::class.java)
            intent.putExtra("id", receiverUid)  // Pass the service provider's ID
            startActivity(intent)
        }

        setSupportActionBar(binding!!.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        database = FirebaseFirestore.getInstance()
        //storage instance
        dialog = ProgressDialog(this@ServiceChatActivity)
        dialog!!.setMessage("Uploading Image...")
        dialog!!.setCancelable(false)
        messages = ArrayList()
        val name = intent.getStringExtra("name")
        //val profile = intent.getStringExtra("image")
        binding!!.name.text = name
        receiverUid = intent.getStringExtra("id")
        senderUid = FirebaseAuth.getInstance().uid
        database!!.collection("Consumer").document(receiverUid!!)
            .addSnapshotListener{ snapshot, e ->
                if(snapshot!= null && snapshot.exists()){
                    val isOnline = snapshot.getBoolean("isOnline")
                    if(isOnline == false){
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
            val date = Date()
            val message = Message(messageTxt, senderUid!!, Date().time)

            binding!!.messageBox.setText("")
            val randomKey = database!!.collection("Chats").document().id
            message.messageId = randomKey

            val senderLastMsgObj = mapOf(
                "lastMessageSent" to message.message!!,
                "lastMessageTimeSent" to Timestamp.now()
            )
            val receiverLastMsgObj = mapOf(
                "lastMessageReceived" to message.message!!,
                "lastMessageTimeReceived" to Timestamp.now()
            )


            isServiceProvider(senderUid!!) { isSenderServiceProvider ->
                database!!.collection("Chats").document(senderRoom!!)
                    .set(senderLastMsgObj, SetOptions.merge()).addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to update last message: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                database!!.collection("Chats").document(receiverRoom!!)
                    .set(receiverLastMsgObj, SetOptions.merge()).addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to update last message: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

                database!!.collection("Chats").document(senderRoom!!).collection("messages")
                    .document(randomKey)
                    .set(message)
                    .addOnSuccessListener {
                        database!!.collection("Chats").document(receiverRoom!!)
                            .collection("messages")
                            .document(randomKey)
                            .set(message)
                    }

                if (isSenderServiceProvider) {
                    database!!.collection("ServiceProviders").document(senderUid!!)
                        .set(senderLastMsgObj, SetOptions.merge())
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to update last message in ServiceProviders: ${e.message}", Toast.LENGTH_SHORT).show()
                        }

                    database!!.collection("Consumer").document(receiverUid!!)
                        .set(receiverLastMsgObj, SetOptions.merge())
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to update last message in Consumer: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    database!!.collection("Consumer").document(senderUid!!)
                        .set(senderLastMsgObj, SetOptions.merge())
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to update last message in Consumer: ${e.message}", Toast.LENGTH_SHORT).show()
                        }

                    database!!.collection("ServiceProviders").document(receiverUid!!)
                        .set(receiverLastMsgObj, SetOptions.merge())
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to update last message in ServiceProviders: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }

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
                    binding!!.camera.visibility = View.GONE
                } else {
                    binding!!.send.isEnabled = false
                    binding!!.attach.visibility = View.VISIBLE
                    binding!!.camera.visibility = View.VISIBLE
                }
            }

        })
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
        if(resultCode == 25){
            if(data != null){
                if(data.data != null){
                    val selectedImage = data.data
                    val calendar = Calendar.getInstance()

                }
            }
        }
    }
}