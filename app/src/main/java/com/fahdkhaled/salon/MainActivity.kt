package com.fahdkhaled.salon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
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
    var phone by remember { mutableStateOf("+20") }
    var message by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }

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
                text = "✂",
                color = Color.White,
                fontSize = 65.sp
            )

            Text(
                text = "FAHD KHALED",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "BARBERSHOP",
                color = Color.LightGray,
                fontSize = 17.sp,
                letterSpacing = 5.sp
            )

            Spacer(modifier = Modifier.height(45.dp))

            Text(
                text = "أهلاً بيك في صالون فهد خالد",
                color = Color.White,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "سجل دخولك أو أنشئ حساب جديد",
                color = Color.LightGray,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(25.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("رقم الموبايل") },
                placeholder = { Text("+20") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                ),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.LightGray,
                    cursorColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    message = "تسجيل الدخول برقم الهاتف هيتفعل في الخطوة الجاية"
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "المتابعة برقم الهاتف",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(15.dp))

            OutlinedButton(
                onClick = {
                    scope.launch {
                        try {
                            loading = true
                            message = ""

                            val googleOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(
                                    context.getString(R.string.default_web_client_id)
                                )
                                .setAutoSelectEnabled(false)
                                .build()

                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleOption)
                                .build()

                            val credentialManager =
                                CredentialManager.create(context)

                            val result = credentialManager.getCredential(
                                context,
                                request
                            )

                            if (
                                result.credential.type ==
                                GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                            ) {
                                val googleCredential =
                                    GoogleIdTokenCredential.createFrom(
                                        result.credential.data
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
                                            message = "تم تسجيل الدخول بنجاح ❤️"
                                        } else {
                                            message = task.exception?.message
                                                ?: "حصل خطأ أثناء تسجيل الدخول"
                                        }
                                    }
                            } else {
                                loading = false
                                message = "نوع الحساب غير مدعوم"
                            }

                        } catch (e: Exception) {
                            loading = false
                            message = e.localizedMessage
                                ?: "تعذر تسجيل الدخول باستخدام Google"
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text(
                    if (loading) "جاري تسجيل الدخول..." 
                    else "التسجيل باستخدام Google",
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(15.dp))

            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(25.dp))

            Text(
                text = "خدمات الحلاقة والعناية الرجالية",
                color = Color.Gray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
