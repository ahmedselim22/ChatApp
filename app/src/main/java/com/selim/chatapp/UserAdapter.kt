package com.selim.chatapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.selim.chatapp.databinding.UserItemLayoutBinding

class UserAdapter(private val context:Context,private val list:ArrayList<User>):RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    inner class UserViewHolder(itemView: View):ViewHolder(itemView){
        val binding = UserItemLayoutBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item_layout,parent,false)
        return UserViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser =list[position]
        holder.binding.apply {
            tvUserItemName.text = currentUser.name
            Log.d("here image", currentUser.profileImage.toString())
            Glide.with(context).load(currentUser.profileImage).into(ivUserItemImage)
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(context,ChatActivity::class.java)
            intent.putExtra("name",currentUser.name)
            intent.putExtra("uid",currentUser.uid)
            intent.putExtra("image",currentUser.profileImage)
            context.startActivity(intent)
        }
    }
}