package za.co.varsitycollege.st10215473.community

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import za.co.varsitycollege.st10215473.community.adapter.MessagesAdapter
import za.co.varsitycollege.st10215473.community.data.Message
import za.co.varsitycollege.st10215473.community.databinding.ActivityServiceChatBinding
import java.lang.reflect.Array
import java.util.Calendar
import java.util.Date

class ServiceChatActivity : AppCompatActivity() {

    var binding: ActivityServiceChatBinding? = null
    var adapter: MessagesAdapter? = null
    var messages: ArrayList<Message>? = null
    var senderRoom: String? = null
    var receiverRoom:String? = null
    var database: FirebaseFirestore? = null
    //var storage
    var dialog: ProgressDialog? = null
    var senderUid:String? = null
    var receiverUid: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_service_chat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityServiceChatBinding.inflate(layoutInflater)

        setContentView(binding!!.root)
        setSupportActionBar(binding!!.toolbar)
        database = FirebaseFirestore.getInstance()
        //storage instance
        dialog = ProgressDialog(this@ServiceChatActivity)
        dialog!!.setMessage("Uploading Image...")
        dialog!!.setCancelable(false)
        messages = ArrayList()
        val name = intent.getStringExtra("name")
        //val profile = intent.getStringExtra("image")
        binding!!.name.text = name
        receiverUid = intent.getStringExtra("Id")
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
        binding!!.chatRV.layoutManager = LinearLayoutManager(this@ServiceChatActivity)
        binding!!.chatRV.adapter = adapter
        database!!.collection("Chats").document(senderRoom!!).collection("messages")
            .addSnapshotListener{ snapshots, e ->
                if(snapshots != null && !snapshots.isEmpty){
                    messages!!.clear()
                    for(document in snapshots.documents){
                        val message = document.toObject(Message::class.java)
                        message!!.messageId = document.id
                        messages!!.add(message)
                    }
                    adapter!!.notifyDataSetChanged()
                }
            }
        binding!!.send.setOnClickListener{
            val messageTxt: String = binding!!.messageBox.text.toString()
            val date = Date()
            val message = Message(messageTxt, senderUid!!, date.time)

            binding!!.messageBox.setText("")
            val randomKey = database!!.collection("Chats").document().id
            val lastMsgObj = HashMap<String, Any>()
            lastMsgObj["lastMessage"] = message.message!!
            lastMsgObj["lastMessageTime"] = date.time

            database!!.collection("Chats").document(senderRoom!!).update(lastMsgObj)
            database!!.collection("Chats").document(receiverRoom!!).update(lastMsgObj)
            database!!.collection("Chats").document(senderRoom!!).collection("messages")
                .document(randomKey)
                .set(message)
                .addOnSuccessListener {
                    database!!.collection("Chats").document(receiverRoom!!)
                        .collection("message")
                        .document(randomKey)
                        .set(message)
                        .addOnSuccessListener {

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

            }

        })
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