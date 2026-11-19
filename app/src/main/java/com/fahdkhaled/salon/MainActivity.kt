package com.fahdkhaled.salon

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SalonApp()
        }
    }
}

@Composable
fun SalonApp() {
    val context = LocalContext.current
    val activity = context as? Activity
    val auth = remember { FirebaseAuth.getInstance() }
    val scope = rememberCoroutineScope()

    var user by remember { mutableStateOf(auth.currentUser) }
    var message by remember {
        mutableStateOf(
            if (user != null) "أهلاً بيك في FAHD KHALED SALON ❤️"
            else "سجل دخولك للمتابعة"
        )
    }
    var loading by remember { mutableStateOf(false) }

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
                        text = user?.displayName ?: "تم تسجيل الدخول",
                        color = Color.White,
                        fontSize = 18.sp
                    )

                    Text(
                        text = user?.email ?: "",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                } else {
                    Button(
                        onClick = {
                            if (activity == null) {
                                message = "حصل خطأ في فتح تسجيل الدخول"
                                return@Button
                            }

                            scope.launch {
                                loading = true
                                message = "جاري تسجيل الدخول..."

                                try {
                                    val webClientId =
                                        context.getString(R.string.default_web_client_id)

                                    val googleOption =
                                        GetSignInWithGoogleOption.Builder(webClientId)
                                            .build()

                                    val request =
                                        GetCredentialRequest.Builder()
                                            .addCredentialOption(googleOption)
                                            .build()

                                    val credentialManager =
                                        CredentialManager.create(context)

                                    val result =
                                        credentialManager.getCredential(
                                            activity,
                                            request
                                        )

                                    val credential = result.credential

                                    if (
                                        credential is CustomCredential &&
                                        credential.type ==
                                        GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                                    ) {
                                        val googleCredential =
                                            GoogleIdTokenCredential.createFrom(
                                                credential.data
                                            )

                                        val firebaseCredential =
                                            GoogleAuthProvider.getCredential(
                                                googleCredential.idToken,
                                                null
                                            )

                                        auth.signInWithCredential(firebaseCredential)
                                            .addOnCompleteListener { task ->
                                                loading = false

                                                if (task.isSuccessful) {
                                                    user = auth.currentUser
                                                    message =
                                                        "تم تسجيل الدخول بنجاح ❤️"
                                                } else {
                                                    message =
                                                        task.exception?.localizedMessage
                                                            ?: "فشل تسجيل الدخول"
                                                }
                                            }
                                    } else {
                                        loading = false
                                        message = "لم يتم اختيار حساب جوجل"
                                    }

                                } catch (e: Exception) {
                                    loading = false
                                    message =
                                        e.localizedMessage
                                            ?: "حدث خطأ أثناء تسجيل الدخول"
                                }
                            }
                        },
                        enabled = !loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = if (loading) "جاري التحميل..." else "تسجيل الدخول بحساب Google"
                        )
                    }
                }
            }
        }
    }
}
