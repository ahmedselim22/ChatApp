package com.selim.chatapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.selim.chatapp.databinding.ActivityChatBinding
import java.util.Calendar
import java.util.Date

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter:MessageAdapter
    private lateinit var messages:ArrayList<Message>
    private lateinit var senderRoom:String
    private lateinit var receiverRoom:String
    private lateinit var database: FirebaseDatabase
    private lateinit var storage:FirebaseStorage
    private lateinit var dialog:ProgressDialog
    private lateinit var senderUid:String
    private lateinit var receiverUid:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        messages = ArrayList()
        setSupportActionBar(binding.toolbarChat)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        dialog = ProgressDialog(this@ChatActivity)
        dialog.setMessage("Uploading Image ..")
        dialog.setCancelable(false)
        val name = intent.getStringExtra("name")
        val profileImage = intent.getStringExtra("image")
        receiverUid = intent.getStringExtra("uid").toString()
        senderUid = FirebaseAuth.getInstance().uid.toString()
        binding.tvChatName.text = name
        Glide.with(this).load(profileImage).placeholder(R.drawable.placeholder).into(binding.ivChatProfileImage)
        database.reference.child("presence").child(receiverUid).addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val status = snapshot.getValue(String::class.java)
                    if (status == "offline"){
                        binding.tvChatStatus.visibility = View.GONE
                    }
                    else{
                        binding.tvChatStatus.setText(status)
                        binding.tvChatStatus.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid
        adapter = MessageAdapter(this,messages,senderRoom,receiverRoom)
        binding.recyclerViewChat.layoutManager = LinearLayoutManager(this@ChatActivity)
        binding.recyclerViewChat.adapter = adapter
        database.reference.child("chats")
            .child(senderRoom).child("messages").addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages.clear()
                    for (snap in snapshot.children){
                        val message:Message? = snap.getValue(Message::class.java)
                        message!!.messageId = snap.key
                        messages.add(message)
                        Log.d("here message came", message.message.toString())
                    }
                    Log.d("here all messages", messages.toString())
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        binding.btnChatSend.setOnClickListener {
            val messageText:String = binding.etChatMessageBox.text.toString()
            val date = Date()
            val message = Message(messageText,senderUid,date.time)
            binding.etChatMessageBox.setText("")
            val randomKey = database.reference.push().key
            val lastMsgObj = HashMap<String,Any>()
            lastMsgObj["lastMessage"] = message.message!!
            lastMsgObj["lastMessageTime"]= date.time
            database.reference.child("chats").child(senderRoom).updateChildren(lastMsgObj)
            database.reference.child("chats").child(receiverRoom).updateChildren(lastMsgObj)
            database.reference.child("chats").child(senderRoom).child("messages")
                .child(randomKey!!)
                .setValue(message).addOnSuccessListener {
                    database.reference.child("chats")
                        .child(receiverRoom)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message).addOnSuccessListener {  }
                }

        }
        binding.ivChatIconAttachment.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent,25)
        }
        binding.ivChatIconBack.setOnClickListener { finish() }

        val handler = Handler()
        binding.etChatMessageBox.addTextChangedListener (object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                database.reference.child("presence")
                    .child(senderUid)
                    .setValue("typing...")
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedTyping,1000)
            }
            val userStoppedTyping = Runnable {
                database.reference.child("presence")
                    .child(senderUid)
                    .setValue("online")
            }

        })

        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database.reference.child("presence")
            .child(currentId!!)
            .setValue("online")
    }

    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database.reference.child("presence")
            .child(currentId!!)
            .setValue("offline")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==25){
            if (data!=null){
                if (data.data!=null){
                    val selectedImage = data.data
                    val calender = Calendar.getInstance()
                    val reference = storage.reference.child("chats")
                        .child(calender.timeInMillis.toString()+"")
                    dialog.show()
                    reference.putFile(selectedImage!!)
                        .addOnCompleteListener{task->
                            dialog.dismiss()
                            if (task.isSuccessful){
                                reference.downloadUrl.addOnSuccessListener {uri->
                                    val filePath = uri.toString()
                                    val messageText:String = binding.etChatMessageBox.text.toString()
                                    val date = Date()
                                    val message = Message(messageText,senderUid,date.time)
                                    message.message = "photo"
                                    message.imageUrl = filePath
                                    binding.etChatMessageBox.setText("")
                                    val randomKey = database.reference.push().key
                                    val lastMessageObj =HashMap<String,Any>()
                                    lastMessageObj["lastMessage"]= message.message!!
                                    lastMessageObj["lastMessageTime"] = date.time
                                    database.reference.child("chats").child(senderRoom).updateChildren(lastMessageObj)
                                    database.reference.child("chats").child(receiverRoom).updateChildren(lastMessageObj)
                                    database.reference.child("chats").child(senderRoom)
                                        .child("messages")
                                        .child(randomKey!!)
                                        .setValue(message).addOnSuccessListener {
                                            database.reference.child("chats").child(receiverRoom)
                                                .child("messages")
                                                .child(randomKey)
                                                .setValue(message)
                                                .addOnSuccessListener{}

                                        }
                                }
                            }
                        }
                }
            }
        }
    }
}