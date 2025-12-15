package com.example.mustase

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.mustase.list.Task
import com.example.mustase.ui.theme.TodoTheme
import kotlinx.coroutines.launch
import java.util.UUID

// Navigation screens
sealed class Screen {
    data object List : Screen()
    data class Detail(val task: Task) : Screen()
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

    var items by remember {
        mutableStateOf(List(100) {
            Task(
                id = "id_$it",
                title = "Task #$it",
                description = "Description for task #$it"
            )
        })
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

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
                        val newItem = Task(
                            id = UUID.randomUUID().toString(),
                            title = "Task #${items.size}",
                            description = "Description for task #${items.size}"
                        )
                        items = items + newItem
                        coroutineScope.launch {
                            listState.animateScrollToItem(items.size - 1)
                        }
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
                ListScreenWrapper(
                    items = items,
                    listState = listState,
                    onTaskClick = { task ->
                        backStack.add(Screen.Detail(task))
                    },
                    onTaskDelete = { task ->
                        items = items - task
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is Screen.Detail -> {
                DetailScreen(
                    task = currentScreen.task,
                    onBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }
        }
    }
}


