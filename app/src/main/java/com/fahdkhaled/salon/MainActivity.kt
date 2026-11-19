package com.fahdkhaled.salon

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : ComponentActivity() {

    private lateinit var googleClient: com.google.android.gms.auth.api.signin.GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    private var currentUser by mutableStateOf<FirebaseUser?>(null)
    private var message by mutableStateOf("سجل دخولك للمتابعة")
    private var loading by mutableStateOf(false)

    companion object {
        private const val RC_SIGN_IN = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        if (currentUser != null) {
            message = "أهلاً بيك في FAHD KHALED SALON ❤️"
        }

        val options = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleClient = GoogleSignIn.getClient(this, options)

        setContent {
            SalonApp(
                user = currentUser,
                message = message,
                loading = loading,
                onLogin = { signInWithGoogle() }
            )
        }
    }

    private fun signInWithGoogle() {
        loading = true
        message = "جاري فتح حسابات Google..."

        startActivityForResult(
            googleClient.signInIntent,
            RC_SIGN_IN
        )
    }

    @Deprecated("Deprecated in Android")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)

                val token = account.idToken

                if (token.isNullOrEmpty()) {
                    loading = false
                    message = "لم يتم الحصول على بيانات حساب Google"
                    return
                }

                val credential = GoogleAuthProvider.getCredential(
                    token,
                    null
                )

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

            } catch (e: ApiException) {
                loading = false
                message = "خطأ Google رقم: ${e.statusCode}"
            } catch (e: Exception) {
                loading = false
                message = e.localizedMessage ?: "حدث خطأ"
            }
        }
    }
}

@Composable
fun SalonApp(
    user: FirebaseUser?,
    message: String,
    loading: Boolean,
    onLogin: () -> Unit
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

                Spacer(modifier = Modifier.height(25.dp))

                if (user != null) {

                    Text(
                        text = user.displayName ?: "تم تسجيل الدخول",
                        color = Color.White,
                        fontSize = 18.sp
                    )

                    Text(
                        text = user.email ?: "",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )

                } else {

                    Button(
                        onClick = onLogin,
                        enabled =
