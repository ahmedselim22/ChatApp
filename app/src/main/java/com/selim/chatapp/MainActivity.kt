package com.selim.chatapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.selim.chatapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    private lateinit var usersList:ArrayList<User>
    private lateinit var auth:FirebaseAuth
    private lateinit var database:FirebaseDatabase
    private lateinit var adapter:UserAdapter
    private lateinit var dialog:ProgressDialog
    private var user:User?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        usersList = ArrayList()
        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        adapter = UserAdapter(this,usersList)
        binding.recyclerViewMainChats.layoutManager =GridLayoutManager(this,2)
        dialog =ProgressDialog(this@MainActivity)
        dialog.setMessage("Uploading Image ..")
        dialog.setCancelable(false)

        binding.fabMainLogout.setOnClickListener {
            auth.signOut()
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this@MainActivity,VerificationActivity::class.java))
            finish()
        }

        database.reference.child("users").child(FirebaseAuth.getInstance().uid!!)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    user = snapshot.getValue(User::class.java)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        binding.recyclerViewMainChats.adapter = adapter
        database.reference.child("users").addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                usersList.clear()
                for (shot in snapshot.children){
                    val user: User? = shot.getValue(User::class.java)
                    if (!user?.uid.equals(FirebaseAuth.getInstance().uid)){
                        usersList.add(user!!)
                    }
                }
                Log.d("here list ", usersList.toString())
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onPause() {
        super.onPause()
        val currentId =FirebaseAuth.getInstance().uid
        database.reference.child("presence")
            .child(currentId!!).setValue("offline")
    }

    override fun onResume() {
        super.onResume()
        val currentId =FirebaseAuth.getInstance().uid
        database.reference.child("presence")
            .child(currentId!!).setValue("online")
    }
}