package com.example.mustase

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mustase.list.Task
import com.example.mustase.list.TaskListViewModel
import com.example.mustase.ui.theme.TodoTheme

// Navigation screens
sealed class Screen {
    data object List : Screen()
    data class Detail(val task: Task?) : Screen()
}

class ComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TodoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val backStack = remember { mutableStateListOf<Screen>(Screen.List) }
    val context = LocalContext.current
    val viewModel: TaskListViewModel = viewModel()

    val currentScreen = backStack.lastOrNull() ?: Screen.List

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Mes Tâches") },
                actions = {
                    IconButton(
                        onClick = {
                            val intent = Intent(context, MainActivity::class.java)
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "go to classic app"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentScreen is Screen.List) {
                FloatingActionButton(
                    onClick = {
                        backStack.add(Screen.Detail(null))
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add"
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentScreen) {
            Screen.List -> {
                ListScreen(
                    viewModel = viewModel,
                    onTaskClick = { task ->
                        backStack.add(Screen.Detail(task))
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is Screen.Detail -> {
                DetailScreen(
                    task = currentScreen.task,
                    viewModel = viewModel,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
