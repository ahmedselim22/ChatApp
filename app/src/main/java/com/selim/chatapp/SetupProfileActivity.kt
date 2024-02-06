package com.selim.chatapp

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.selim.chatapp.databinding.ActivitySetupProfileBinding
import java.sql.Time
import java.util.*
import kotlin.collections.HashMap

class SetupProfileActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySetupProfileBinding
    private lateinit var auth:FirebaseAuth
    private val REQUEST_PERMISSION_CODE = 1
    private lateinit var database:FirebaseDatabase
    private lateinit var storage:FirebaseStorage
    private lateinit var selectedImage:Uri
    private lateinit var dialog:ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        auth =FirebaseAuth.getInstance()
        database =FirebaseDatabase.getInstance()
        storage =FirebaseStorage.getInstance()
        dialog = ProgressDialog(this@SetupProfileActivity)
        dialog.setMessage("Uploading Image ..")
        dialog.setCancelable(false)
        checkAndRequestPermissions()
        binding.btnSetupProfileSetup.setOnClickListener {
            val name:String =binding.etSetupProfileName.text.toString()
            if (name.isEmpty()){
                binding.etSetupProfileName.setError("please inter your name")
                return@setOnClickListener
            }
            dialog.show()
            if (selectedImage!=null){
                val reference = storage.reference.child("profile")
                    .child(auth.uid!!)
                reference.putFile(selectedImage).addOnCompleteListener{task->
                    if (task.isSuccessful){
                        reference.downloadUrl.addOnSuccessListener {uri->
                            val imageUrl =uri.toString()
                            val uid = auth.uid
                            val phone =auth.currentUser!!.phoneNumber
                            val name= binding.etSetupProfileName.text.toString()
                            val user =User(uid,name,phone,imageUrl)
                            Log.d("here user", "user :"+user.toString())
                            database.reference.child("users")
                                .child(uid!!)
                                .setValue(user)
                                .addOnSuccessListener {
                                    dialog.dismiss()
                                    startActivity(Intent(this@SetupProfileActivity,MainActivity::class.java))
                                    finish()
                                }
                        }.addOnFailureListener {
                            Log.d("here storage fail", it.message.toString())
                        }
                    }
                    else{
                        val uid = auth.uid
                        val phone =auth.currentUser!!.phoneNumber
                        val user =User(uid,name,phone,"No Image")
                        database.reference.child("users")
                            .child(uid!!)
                            .setValue(user)
                            .addOnSuccessListener {
                                dialog.dismiss()
                                startActivity(Intent(this@SetupProfileActivity,MainActivity::class.java))
                                finish()
                            }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data!=null){
            if (data.data!=null) {
                val uri = data.data
                Log.d("here uri local", uri.toString())
                val storage = FirebaseStorage.getInstance()
                val time = Date().time
                val reference = storage.reference.child("profile")
                    .child(time.toString()+"")
                reference.putFile(uri!!).addOnCompleteListener{task->
                    if (task.isSuccessful){
                        reference.downloadUrl.addOnCompleteListener {uri->
                            Log.d("here uri saved", uri.toString())
                            val filePath = uri.toString()
                            val obj = HashMap<String,Any>()
                            obj["image"]=filePath
                            database.reference.child("users")
                                .child(FirebaseAuth.getInstance().uid!!)
                                .updateChildren(obj).addOnSuccessListener{ }
                        }
                    }
                }
                binding.ivSetupProfileProfileImg.setImageURI(data.data)
                selectedImage = data.data!!
            }
        }
    }

    // Check and request permission
    private fun checkAndRequestPermissions() {
        val permission = android.Manifest.permission.READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_PERMISSION_CODE)
        } else {
            accessDevicePhotos()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                accessDevicePhotos()
            } else {
                Toast.makeText(this@SetupProfileActivity, "permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun accessDevicePhotos() {
        binding.ivSetupProfileProfileImg.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent,45)
        }
    }
}