package com.selim.chatapp

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.selim.chatapp.databinding.DeleteLayoutBinding
import com.selim.chatapp.databinding.ReceiveMessageBinding
import com.selim.chatapp.databinding.SendMessageBinding

class MessageAdapter(
    private val context: Context,
    private val messages:ArrayList<Message>?,
    private val senderRoom:String,
    private val receiverRoom:String
):RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val ITEM_SENT=11
    private val ITEM_RECEIVE=22
    inner class SentMessageHolder(itemView: View) :ViewHolder(itemView){
        val binding = SendMessageBinding.bind(itemView)
    }
    inner class ReceivedMessageHolder(itemView: View) :ViewHolder(itemView){
        val binding = ReceiveMessageBinding.bind(itemView)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == ITEM_SENT){
            val view = LayoutInflater.from(context).inflate(R.layout.send_message,parent,false)
            return SentMessageHolder(view)
        }
        else{
            val view = LayoutInflater.from(context).inflate(R.layout.receive_message,parent,false)
            return ReceivedMessageHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages!![position]
        if (FirebaseAuth.getInstance().uid == message.senderId){
            return ITEM_SENT
        }
        else{
            return ITEM_RECEIVE
        }
    }

    override fun getItemCount(): Int {
        return messages!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages!![position]
        if (holder.javaClass == SentMessageHolder::class.java) {
            val viewHolder = holder as SentMessageHolder
            if (message.message.equals("photo")) {
                viewHolder.binding.apply {
                    tvSendMessageMessage.visibility = View.GONE
                    mLinear.visibility = View.GONE
                    ivSendMessageImage.visibility = View.VISIBLE
                    Glide.with(context).load(message.imageUrl)
                        .placeholder(R.drawable.placeholder)
                        .into(ivSendMessageImage)
                    Log.d("here image sent url", message.imageUrl!!)
                }
            }
                viewHolder.binding.tvSendMessageMessage.text = message.message
                viewHolder.itemView.setOnLongClickListener {
                    val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                    val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)

                    val dialog = AlertDialog.Builder(context)
                        .setTitle("Delete Message ")
                        .setView(binding.root)
                        .create()

                    binding.tvDeleteEveryone.setOnClickListener {
                        message.message = "This message is removed"
                        message.messageId.let {
                            if (it != null) {
                                FirebaseDatabase.getInstance().reference.child("chats")
                                    .child(senderRoom)
                                    .child("message")
                                    .child(it).setValue(message)
                            }
                        }
                        message.messageId.let {
                            if (it != null) {
                                FirebaseDatabase.getInstance().reference.child("chats")
                                    .child(receiverRoom)
                                    .child("message")
                                    .child(it).setValue(message)
                            }
                        }
                        dialog.dismiss()
                    }

                    binding.tvDeleteDelete.setOnClickListener {
                        message.messageId.let {
                            FirebaseDatabase.getInstance().reference.child("chats")
                                .child(senderRoom)
                                .child("message")
                                .child(it!!).setValue(null)
                        }
                        dialog.dismiss()
                    }

                    binding.tvDeleteCancel.setOnClickListener {
                        dialog.dismiss()
                    }

                    dialog.show()

                    false
                }
            }
         else {
            val viewHolder = holder as ReceivedMessageHolder
            if (message.message.equals("photo")) {
                viewHolder.binding.apply {
                    tvReceiveMessageMessage.visibility = View.GONE
                    mLinear.visibility = View.GONE
                    tvReceiveMessageImage.visibility = View.VISIBLE
                    Glide.with(context).load(message.imageUrl)
                        .placeholder(R.drawable.placeholder)
                        .into(tvReceiveMessageImage)
                }
            }
            viewHolder.binding.tvReceiveMessageMessage.text = message.message
            viewHolder.itemView.setOnLongClickListener {
                val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)

                val dialog = AlertDialog.Builder(context)
                    .setTitle("Delete Message ")
                    .setView(binding.root)
                    .create()

                binding.tvDeleteEveryone.setOnClickListener {
                    message.message = "This message is removed"
                    message.messageId.let {
                        if (it != null) {
                            FirebaseDatabase.getInstance().reference.child("chats")
                                .child(senderRoom)
                                .child("message")
                                .child(it).setValue(message)
                        }
                    }
                    message.messageId.let {
                        if (it != null) {
                            FirebaseDatabase.getInstance().reference.child("chats")
                                .child(receiverRoom)
                                .child("message")
                                .child(it).setValue(message)
                        }
                    }
                    dialog.dismiss()
                }

                binding.tvDeleteDelete.setOnClickListener {
                    message.messageId.let {
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(it!!).setValue(null)
                    }
                    dialog.dismiss()
                }

                binding.tvDeleteCancel.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()

                false
            }
        }
    }

}