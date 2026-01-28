package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.navigation.compose.rememberNavController

import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.composable
import com.example.myapplication.data.entity.ScoreEntity
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()

                androidx.navigation.compose.NavHost(
                    navController = navController,
                    startDestination = "Game"
                ) {
                    composable("Game") {
                        GameScreen(navController = navController)
                    }

                    composable("Settings") {
                        SettingScreen(navController = navController)
                    }

                    composable("Leaderboard") {
                        LeaderboardScreen(navController = navController)
                    }


                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(navController: androidx.navigation.NavController) {

    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val sharedPref = remember { context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE) }
    var score by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(30) }
    var moleIndex by remember { mutableIntStateOf(-1) }
    var isGameRunning by remember { mutableStateOf(false) }
    var highScore by remember { mutableIntStateOf(0) }
    var showGameOver by remember { mutableStateOf(false) }
    var buttonClick by remember { mutableIntStateOf(0) }
    val currentUsername = sharedPref.getString("logged_in_user", null)

    var currentUserId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(currentUsername) {
        if (currentUsername != null) {
            val userEntity = db.userDao().getUser(currentUsername)
            currentUserId = userEntity?.userId

            if (currentUserId != null) {
                val history = db.scoreDao().getHistoryForUser(currentUserId!!)

                if (history.isNotEmpty()) {
                    highScore = history[0].score
                } else {
                    highScore = 0
                }
            }
        }
    }

    LaunchedEffect(isGameRunning) {
        if (isGameRunning) {
            while (timeLeft > 0) {
                delay(1000L)
                timeLeft--
            }
            isGameRunning = false
            if (score > highScore) {
                highScore = score
                if (currentUserId != null) {
                    val newScore = ScoreEntity(
                        userId = currentUserId!!,
                        score = highScore,
                        timestamp = System.currentTimeMillis()
                    )
                    launch {
                        db.scoreDao().insertScore(newScore)
                    }
                }
            }
            showGameOver = true
        }
    }

    LaunchedEffect(isGameRunning) {
        if (isGameRunning) {
            while (timeLeft > 0) {
                buttonClick = 0
                moleIndex = Random.nextInt(0, 9)
                delay(850L)
            }
            moleIndex = -1
        }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Whack-a-Mole") },
            actions = {
                IconButton(onClick = { navController.navigate("Settings") }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }

                IconButton(onClick = {
                    sharedPref.edit().remove("logged_in_user").apply()

                    val intent = Intent(context, LoginScreen::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                }

                IconButton(onClick = { navController.navigate("Leaderboard") }) {
                    Icon(Icons.Filled.List, contentDescription = "Leaderboard")
                }
            }
        )
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                    Text("Score: ${score}")
                    Text("Time: ${timeLeft}")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ){
                Text("High Score: ${highScore}")
            }
            Spacer(modifier = Modifier.height(32.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(count = 9){ index ->
                    Button(
                        onClick = {
                                    if (index == moleIndex && buttonClick == 0)
                                    {
                                        score += 1
                                        buttonClick += 1
                                    }
                                  }
                        ,
                        shape = CircleShape,
                        modifier = Modifier.size(110.dp),
                        contentPadding = PaddingValues(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (index == moleIndex) Color.LightGray else Color.DarkGray
                        )
                )
                    {
                        if (index == moleIndex) {
                            Text("M", color = Color.White)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            if(!isGameRunning && timeLeft == 30) {
                Button(onClick = {
                    isGameRunning = true
                }) {
                    Text("Start")
                }
            } else{
                Button(onClick = {
                    timeLeft = 30
                    isGameRunning = false
                    score = 0
                    moleIndex = -1
                }) {
                    Text("Restart")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            if (showGameOver){
                Text("Game Over! Final score is: ${score}")
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(navController: androidx.navigation.NavController){
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Game")
            }
            Text("Settings")
        }
        Row(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom,
        ){
            Text("Settings Screen", modifier = Modifier.padding(32.dp))

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(navController: androidx.navigation.NavController){
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val sharedPref = remember { context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE) }
    val currentUsername = sharedPref.getString("logged_in_user", null)
    var currentUserId by remember { mutableStateOf<Long?>(null) }
    val leaderboard by db.scoreDao().getLeaderboard().collectAsState(initial = emptyList())

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Game")
            }
            Text("Leaderboard")
        }
        Spacer(modifier = Modifier.padding(32.dp))
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(leaderboard.size) { index ->
                val item = leaderboard[index]

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "${index + 1}. ${item.username}", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "${item.score}", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}



