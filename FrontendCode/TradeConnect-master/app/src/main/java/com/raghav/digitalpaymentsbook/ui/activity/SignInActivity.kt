package com.raghav.digitalpaymentsbook.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.messaging.FirebaseMessaging
import com.raghav.digitalpaymentsbook.data.model.Customer
import com.raghav.digitalpaymentsbook.data.model.Retailer
import com.raghav.digitalpaymentsbook.data.model.apis.RetailerSignIn
import com.raghav.digitalpaymentsbook.data.model.enums.UserRole
import com.raghav.digitalpaymentsbook.data.network.RetrofitHelper
import com.raghav.digitalpaymentsbook.databinding.ActivitySignInBinding
import com.raghav.digitalpaymentsbook.ui.dialog.LoadingDialog
import com.raghav.digitalpaymentsbook.util.PreferenceManager
import com.raghav.digitalpaymentsbook.util.add
import com.raghav.digitalpaymentsbook.util.saveAuthToken
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SignInActivity : AppCompatActivity() {
    
    lateinit var binding : ActivitySignInBinding
    val auth = FirebaseAuth.getInstance()
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var storedVerificationId: String
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mAuth: FirebaseAuth? = null
    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {

            loadingDialog.dismiss()

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

            loadingDialog.dismiss()
            if (e is FirebaseAuthInvalidCredentialsException) {
                Toast.makeText(
                    this@SignInActivity,
                    "Can't verify phone right now",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (e is FirebaseTooManyRequestsException) {
                Toast.makeText(
                    this@SignInActivity,
                    "Can't verify phone right now",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            loadingDialog.dismiss()
            binding.loginRelative.visibility = View.GONE
            binding.otpRelative.visibility = View.VISIBLE


            // Save verification ID and resending token so we can use them later
            storedVerificationId = verificationId
            resendToken = token
        }
    }
    val loadingDialog = LoadingDialog()

    val handler = CoroutineExceptionHandler { _, throwable -> Log.d("TAG","ERROR=${throwable.message}") }


    val gsignLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
        try {
            // Google Sign In was successful, authenticate with Firebase
            val account = task.getResult(ApiException::class.java)

            firebaseAuthWithGoogle(account.idToken)
        } catch (e: ApiException) {
            // Google Sign In failed, update UI appropriately
            Toast.makeText(
                this@SignInActivity,
                "Couldn't sign you in. Try signing in through phone number",
                Toast.LENGTH_LONG
            ).show()
            loadingDialog.dismiss()
            Log.w("TAG", "Google sign in failed", e)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginRelative.visibility = View.VISIBLE
        binding.otpRelative.visibility = View.GONE
        binding.nextBtn.isEnabled = false
        binding.nextBtn.isActivated = false

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(com.raghav.digitalpaymentsbook.R.string.web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        mAuth = FirebaseAuth.getInstance()

        binding.signin.setOnClickListener { 
            signIn()
        }

        binding.signup.setOnClickListener {
            startActivity(Intent(this@SignInActivity, SignUpActivity::class.java))
        }


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
            loadingDialog.isCancelable = false
            loadingDialog.show(supportFragmentManager, "loading")
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
        loadingDialog.show(supportFragmentManager, "loading")

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {

                    val user = task.result?.user

                    lifecycleScope.launch{

                            userServerSignIn(this,RetailerSignIn(null, user?.phoneNumber!!.substring(3)))

                    }


                } else {
                    loadingDialog.dismiss()
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        binding.TextFieldOtp.isErrorEnabled = true
                        binding.TextFieldOtp.error = "Invalid OTP"
                    }else{
                        Toast.makeText(
                            this@SignInActivity,
                            "Some error occurred. Try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }
    }

    private fun signIn() {
        loadingDialog.show(supportFragmentManager,"loading")
        val signInIntent = mGoogleSignInClient!!.signInIntent
        gsignLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = mAuth!!.currentUser
                    lifecycleScope.launch(handler) {
                        userServerSignIn(this,RetailerSignIn(user!!.email, null))
                    }
                } else {

                    loadingDialog.dismiss()
                    // If sign in fails, display a message to the user.
                    Log.w("TAG", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private suspend fun userServerSignIn(scope: CoroutineScope, retailerSignIn: RetailerSignIn){


        //dumping '+91' from the user phone number
        val job1 = scope.async {
            RetrofitHelper.getInstance(this@SignInActivity).retailerSignIn(
                retailerSignIn
            )
        }


        val result1 = job1.await()

        Log.d("TAG","GG 1")

        if (result1.isSuccessful && result1.body() != null) {


            val job2 = scope.async {
                RetrofitHelper.getInstance(this@SignInActivity)
                    .getUser(retailerSignIn.email,retailerSignIn.phone)
            }

            Log.d("TAG","GG 2")
            val result2 = job2.await()
            if (result2.isSuccessful && result2.body() != null) {

                Log.d("TAG","GG 3")

                when (result2.body()!!.role) {
                    UserRole.Retailer -> {
                        PreferenceManager.getInstance(this@SignInActivity).add(
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
                        PreferenceManager.getInstance(this@SignInActivity).add(
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
                PreferenceManager.getInstance(this@SignInActivity).saveAuthToken(result1.body()!!)
                loadingDialog.dismiss()
                startActivity(
                    Intent(
                        this@SignInActivity,
                        MainActivity::class.java
                    )
                )

                val token =  getFcmToken()
                val jo = JSONObject()
                jo.put("registrationToken", token)

                val body =
                    jo.toString().toRequestBody("application/json".toMediaTypeOrNull())


                scope.launch {
                    val job = async {
                        RetrofitHelper.getInstance(this@SignInActivity)
                            .updateNotificationToken(body)
                    }
                    val res = job.await()
                    if (res.isSuccessful && res.body() != null) {
                        Log.d("TAG", "Updated token")
                    } else {
                        Log.d("TAG", "Can't update token")
                    }
                }

                finish()
            }else{
                Toast.makeText(
                    this@SignInActivity,
                    "Some error occurred. Can't get user details from server.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        else{
//            auth.signOut()
            auth.currentUser?.delete()?.addOnCompleteListener {
                loadingDialog.dismiss()
                Toast.makeText(this@SignInActivity, "User with this does not exist.", Toast.LENGTH_SHORT).show()
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