package com.fahdkhaled.salon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    private var currentUser by mutableStateOf<FirebaseUser?>(null)
    private var message by mutableStateOf("أدخل رقم هاتفك للمتابعة")
    private var loading by mutableStateOf(false)
    private var verificationId by mutableStateOf<String?>(null)
    private var codeSent by mutableStateOf(false)
    private var phoneNumber by mutableStateOf("+20")
    private var smsCode by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        if (currentUser != null) {
            message = "أهلاً بيك في FAHD KHALED SALON ❤️"
        }

        setContent {
            SalonApp(
                user = currentUser,
                message = message,
                loading = loading,
                phone = phoneNumber,
                code = smsCode,
                codeSent = codeSent,
                onPhoneChange = { phoneNumber = it },
                onCodeChange = { smsCode = it },
                onSendCode = { sendCode() },
                onVerifyCode = { verifyCode() }
            )
        }
    }

    private fun sendCode() {
        if (!phoneNumber.startsWith("+") || phoneNumber.length < 10) {
            message = "اكتب رقم الهاتف بالصيغة الدولية الصحيحة"
            return
        }

        loading = true
        message = "جاري إرسال كود التحقق..."

        val callbacks =
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(
                    credential: PhoneAuthCredential
                ) {
                    signIn(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    loading = false
                    message = e.localizedMessage ?: "فشل إرسال الكود"
                }

                override fun onCodeSent(
                    id: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    loading = false
                    verificationId = id
                    codeSent = true
                    message = "تم إرسال الكود إلى هاتفك ❤️"
                }
            }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyCode() {
        val id = verificationId

        if (id == null || smsCode.length != 6) {
            message = "أدخل كود التحقق المكون من 6 أرقام"
            return
        }

        loading = true
        message = "جاري التحقق..."

        val credential = PhoneAuthProvider.getCredential(id, smsCode)
        signIn(credential)
    }

    private fun signIn(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .add
