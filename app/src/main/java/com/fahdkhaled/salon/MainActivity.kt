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
import com.google.firebase.auth.*

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

        if (!phoneNumber.startsWith("+") ||
            phoneNumber.length < 10
        ) {
            message = "اكتب رقم الهاتف بالصيغة الدولية الصحيحة"
            return
        }

        loading = true
        message = "جاري إرسال كود التحقق..."

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(
                credential: PhoneAuthCredential
            ) {
                signIn(credential)
            }

            override fun onVerificationFailed(
                e: FirebaseException
            ) {
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
            .setTimeout(60L, java.util.concurrent.TimeUnit.SECONDS)
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

        val credential = PhoneAuthProvider.getCredential(
            id,
            smsCode
        )

        signIn(credential)
    }

    private fun signIn(credential: PhoneAuthCredential) {

        auth.signInWithCredential(credential)
            .addOnCompleteListener { result ->

                loading = false

                if (result.isSuccessful) {
                    currentUser = auth.currentUser
                    message = "تم تسجيل الدخول بنجاح ❤️"
                } else {
                    message = result.exception?.localizedMessage
                        ?: "فشل تسجيل الدخول"
                }
            }
    }
}

@Composable
fun SalonApp(
    user: FirebaseUser?,
    message: String,
    loading: Boolean,
    phone: String,
    code: String,
    codeSent: Boolean,
    onPhoneChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onSendCode: () -> Unit,
    onVerifyCode: () -> Unit
) {

    MaterialTheme {

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = "F H",
                    color = Color.White,
                    fontSize = 55.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "FAHD KHALED SALON",
                    color = Color.White,
                    fontSize = 24.sp
                )

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 17.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (user != null) {

                    Text(
                        text = "تم تسجيل الدخول",
                        color = Color.White,
                        fontSize = 20.sp
                    )

                    Text(
                        text = user.phoneNumber ?: "",
                        color = Color.LightGray
                    )

                } else {

                    OutlinedTextField(
                        value = phone,
                        onValueChange = onPhoneChange,
                        label = { Text("رقم الهاتف") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone
                        ),
                        singleLine = true
                    )

                    if (codeSent) {

                        Spacer
