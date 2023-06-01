package com.raghav.digitalpaymentsbook.ui.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.messaging.FirebaseMessaging
import com.raghav.digitalpaymentsbook.data.model.Customer
import com.raghav.digitalpaymentsbook.data.model.Retailer
import com.raghav.digitalpaymentsbook.data.model.apis.RetailerSignIn
import com.raghav.digitalpaymentsbook.data.model.enums.UserRole
import com.raghav.digitalpaymentsbook.data.network.RetrofitHelper
import com.raghav.digitalpaymentsbook.databinding.ActivityLoginBinding
import com.raghav.digitalpaymentsbook.ui.dialog.ChooseRoleDialog
import com.raghav.digitalpaymentsbook.ui.dialog.LoadingDialog
import com.raghav.digitalpaymentsbook.util.Constants
import com.raghav.digitalpaymentsbook.util.PreferenceManager
import com.raghav.digitalpaymentsbook.util.add
import com.raghav.digitalpaymentsbook.util.saveAuthToken
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit


class SignUpActivity : AppCompatActivity() {

    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var storedVerificationId: String
    lateinit var binding: ActivityLoginBinding
    val dialog = LoadingDialog()
    val auth = FirebaseAuth.getInstance()
    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {

            dialog.dismiss()

            val code: String? = credential.smsCode

            //sometime the code is not detected automatically
            //in this case the code will be null
            //so user has to manually enter the code
            if (code != null) {
                binding.editTextOtp.text = Editable.Factory.getInstance().newEditable(code)
                //verifying the code
                verifyVerificationCode(code)
            }
        }

        override fun onVerificationFailed(e: FirebaseException) {

            dialog.dismiss()
            if (e is FirebaseAuthInvalidCredentialsException) {
                Toast.makeText(
                    this@SignUpActivity,
                    "Can't verify phone right now",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (e is FirebaseTooManyRequestsException) {
                Toast.makeText(
                    this@SignUpActivity,
                    "Can't verify phone right now",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            dialog.dismiss()
            binding.loginRelative.visibility = View.GONE
            binding.otpRelative.visibility = View.VISIBLE


            // Save verification ID and resending token so we can use them later
            storedVerificationId = verificationId
            resendToken = token
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginRelative.visibility = View.VISIBLE
        binding.otpRelative.visibility = View.GONE
        binding.nextBtn.isEnabled = false
        binding.nextBtn.isActivated = false

        binding.editTextPhone.doOnTextChanged { text, start, before, count ->
            if (text?.length != 10) {
                binding.TextFieldPhone.isErrorEnabled = true
                binding.nextBtn.isEnabled = false
                binding.nextBtn.isActivated = false
                binding.TextFieldPhone.error = "Enter a valid phone number"
            } else {
                binding.nextBtn.isEnabled = true
                binding.nextBtn.isActivated = true
                binding.TextFieldPhone.isErrorEnabled = false
            }
        }

        binding.nextBtn.setOnClickListener {
            dialog.isCancelable = false
            dialog.show(supportFragmentManager, "loading")
            binding.editTextPhone.text?.let {
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber("+91$it")
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(callbacks)
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            }
        }

        binding.verifyBtn.setOnClickListener {
            verifyVerificationCode(binding.editTextOtp.text.toString())
        }


    }

    private fun verifyVerificationCode(code: String) {
        //creating the credential
        val credential = PhoneAuthProvider.getCredential(storedVerificationId, code)

        //signing the user
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        dialog.show(supportFragmentManager, "loading")

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {

                    val user = task.result?.user

                    lifecycleScope.launch {


                        //dumping '+91' from the user phone number
                        val job1 = async {
                            RetrofitHelper.getInstance(this@SignUpActivity).retailerSignIn(
                                RetailerSignIn(null, user?.phoneNumber!!.substring(3))
                            )
                        }

                        val result1 = job1.await()
                        if (result1.isSuccessful && result1.body() != null) {

                            val job2 = async {
                                RetrofitHelper.getInstance(this@SignUpActivity)
                                    .getUser(null,user!!.phoneNumber!!.substring(3))
                            }

                            val result2 = job2.await()
                            if (result2.isSuccessful && result2.body() != null) {

                                when (result2.body()!!.role) {
                                    UserRole.Retailer -> {
                                        PreferenceManager.getInstance(this@SignUpActivity).add(
                                            Retailer(
                                                result2.body()!!.name,
                                                result2.body()!!.email,
                                                result2.body()!!.password,
                                                result2.body()!!.phone,
                                                result2.body()!!.address,
                                                result2.body()!!.businessName!!,
                                                result2.body()!!.businessType!!,
                                                result2.body()!!.totalSales,
                                                result2.body()!!.registrationToken,
                                                result2.body()!!.id
                                            )
                                        )
                                    }
                                    UserRole.Customer -> {
                                        PreferenceManager.getInstance(this@SignUpActivity).add(
                                            Customer(
                                                result2.body()!!.name,
                                                result2.body()!!.email,
                                                result2.body()!!.phone,
                                                result2.body()!!.address,
                                                result2.body()!!.registrationToken,
                                                result2.body()!!.id
                                            )
                                        )
                                    }
                                }
                                PreferenceManager.getInstance(this@SignUpActivity).saveAuthToken(result1.body()!!)
                                dialog.dismiss()
                                startActivity(
                                    Intent(
                                        this@SignUpActivity,
                                        MainActivity::class.java
                                    )
                                )
                                finish()
                            }else{
                                Toast.makeText(
                                    this@SignUpActivity,
                                    "Some error occurred. Can't get user details from server.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            dialog.dismiss()
                            val roleDialog = ChooseRoleDialog("Choose Role",onCustomerClick = {
                                startActivity(Intent(this@SignUpActivity,CreateUserActivity::class.java).putExtra("role",
                                    UserRole.Customer))
                            }, onRetailerClick = {
                                startActivity(Intent(this@SignUpActivity,CreateUserActivity::class.java).putExtra("role",
                                    UserRole.Retailer))
                            })
                            roleDialog.show(supportFragmentManager, "roleDialog")
                            roleDialog.isCancelable = false
                        }

                    }


                } else {
                    dialog.dismiss()
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        binding.TextFieldOtp.isErrorEnabled = true
                        binding.TextFieldOtp.error = "Invalid OTP"
                    }else{
                        Toast.makeText(
                            this@SignUpActivity,
                            "Some error occurred. Try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    private suspend fun getFcmToken(): String {
        return try {
            Log.d("TAG", "Got token : ${FirebaseMessaging.getInstance().token.await()}")
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            Log.w("TAG", "Fetching FCM registration token failed" + e.printStackTrace())
            ""
        }
    }

}