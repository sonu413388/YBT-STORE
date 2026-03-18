package com.example.ybt_store

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val auth = FirebaseAuth.getInstance()
            val startDestination = if (auth.currentUser != null) "main" else "auth"

            NavHost(navController = navController, startDestination = startDestination) {
                composable("auth") { AuthScreen(navController) }
                composable("main") { MainScreen() }
            }
        }
    }
}

@Composable
fun AuthScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val firestore = Firebase.firestore
    val auth = FirebaseAuth.getInstance()

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("395465866902-jhbtecnalf9752u8icdp3ldetjto0t9s.apps.googleusercontent.com")
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
                coroutineScope.launch {
                    try {
                        auth.signInWithCredential(credential).await()
                        val user = auth.currentUser
                        if (user != null) {
                            val userDocRef = firestore.collection("users").document(user.uid)
                            val userData = hashMapOf(
                                "name" to user.displayName,
                                "email" to user.email,
                                "uid" to user.uid,
                                "address" to ""
                            )
                            userDocRef.set(userData).await()
                            navController.navigate("main") { popUpTo("auth") { inclusive = true } }
                        }
                    } catch (e: Exception) {
                        Log.w("AuthScreen", "Error creating user document", e)
                    }
                }
            } catch (e: ApiException) {
                Log.w("AuthScreen", "Google sign in failed", e)
            }
        }
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { launcher.launch(googleSignInClient.signInIntent) }) {
            Text("Sign in with Google")
        }
    }
}
