package com.selim.chatapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.selim.chatapp.databinding.ActivityOtpBinding
import java.util.concurrent.TimeUnit

class OtpActivity : AppCompatActivity() {
    private lateinit var binding:ActivityOtpBinding
    private lateinit var verificationId :String
    private lateinit var auth : FirebaseAuth
    private lateinit var dialog:ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        dialog = ProgressDialog(this@OtpActivity)
        dialog.setMessage("Sending OTP ...")
        dialog.setCancelable(false)
        dialog.show()
        val phoneNumber = intent.getStringExtra("phoneNumber").toString()
        binding.tvOtpPhoneNumber.text = phoneNumber

        val optoions = PhoneAuthOptions.newBuilder(auth).setActivity(this@OtpActivity)
            .setTimeout(60L,TimeUnit.SECONDS)
            .setPhoneNumber(phoneNumber)
            .setCallbacks(object :PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                    TODO("Not yet implemented")
                }

                override fun onVerificationFailed(p0: FirebaseException) {
                    TODO("Not yet implemented")
                }

                override fun onCodeSent(verifyId: String, forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
                    super.onCodeSent(verifyId, forceResendingToken)
                    dialog.dismiss()
                    verificationId =verifyId
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0)
                    binding.otpViewOtp.requestFocus()
                }
            }).build()
        PhoneAuthProvider.verifyPhoneNumber(optoions)
        binding.otpViewOtp.setOtpCompletionListener { otp->
            val credential = PhoneAuthProvider.getCredential(verificationId,otp)
            auth.signInWithCredential(credential).addOnCompleteListener{task->
                if (task.isSuccessful){
                    val intent = Intent(this@OtpActivity,SetupProfileActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                }else{
                    Toast.makeText(this@OtpActivity, "Verification Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}