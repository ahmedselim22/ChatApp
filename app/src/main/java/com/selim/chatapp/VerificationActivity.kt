package com.selim.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.selim.chatapp.databinding.ActivityVerificationBinding

class VerificationActivity : AppCompatActivity() {
    private lateinit var binding:ActivityVerificationBinding
    private lateinit var auth:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
//        if (auth.currentUser!=null){
//            val intent = Intent(this@VerificationActivity,MainActivity::class.java)
//            startActivity(intent)
//        }
        binding.etVerificationPhoneNumber.requestFocus()
        binding.btnVerificationContinue.setOnClickListener {
            val intent = Intent(this@VerificationActivity,OtpActivity::class.java)
            intent.putExtra("phoneNumber",binding.etVerificationPhoneNumber.text.toString())
            startActivity(intent)
        }
    }
}