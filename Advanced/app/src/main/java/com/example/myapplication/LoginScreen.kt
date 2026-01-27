package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.example.myapplication.data.entity.UserEntity
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class LoginScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(
                        onLoginSuccess = {
                            val intent = Intent(this@LoginScreen, MainActivity::class.java)
                            startActivity(intent)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )

                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
){
    var username by rememberSaveable {mutableStateOf("") }
    var password by rememberSaveable {mutableStateOf("")}

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    Box(
        modifier = modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(text = "Login Page", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.padding(12.dp))
            OutlinedTextField(
                value = username,
                onValueChange = {username = it},
                label = { Text(text = "Username")},
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Username") }
            )
            Spacer(modifier = Modifier.padding(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = {password = it},
                label = { Text(text = "Password")},
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") }
            )
            Spacer(modifier = Modifier.padding(12.dp))
            Button (
                onClick = {
                    scope.launch {
                        val isValid = validateLogin(context, username, password)
                        if(isValid){
                            val sharedPref = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
                            sharedPref.edit().putString("logged_in_user", username).apply()
                            onLoginSuccess()
                        }else{
                            Toast.makeText(context, "Invalid Credentials", Toast.LENGTH_SHORT).show()
                        }

                    }
//                    if (validateLogin(context, username, password)){
//                        onLoginSuccess()
//                    }
                }
            ){
                Text(text = "Login")
            }
            Spacer(modifier = Modifier.padding(16.dp))
            Button(
                onClick = {
                    if(username.isNotEmpty() && password.isNotEmpty()){
                        scope.launch {
                            val isCreated = performSignUp(context, username, password)
                            if (isCreated){
                                Toast.makeText(context, "Successfully created user.", Toast.LENGTH_LONG).show()
                            }else{
                                Toast.makeText(context, "Signup failed!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }else{
                        Toast.makeText(context, "Enter something here", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text(text="SignUp")
            }
        }

    }

}
suspend fun performSignUp(context: Context, username: String, password: String): Boolean{
        val db = AppDatabase.get(context)
        if(db.userDao().getUser(username)==null){
            db.userDao().insertUser(UserEntity(username = username, password = password))
            return true
        }
        return false
}
suspend fun validateLogin(context: Context, username: String, password: String): Boolean{
    val user = AppDatabase.get(context).userDao().getUser(username)
    return user!=null && user.password == password
}