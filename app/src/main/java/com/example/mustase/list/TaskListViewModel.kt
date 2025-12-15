package com.example.mustase.list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mustase.data.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class TaskListViewModel : ViewModel() {
    private val webService = Api.tasksWebService

    sealed class TaskListState {
        data object Loading : TaskListState()
        data class Success(val list: List<Task>) : TaskListState()
        data class Error(val message: String) : TaskListState()
    }

    val tasksStateFlow = MutableStateFlow<TaskListState>(TaskListState.Loading)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            tasksStateFlow.value = TaskListState.Loading
            try {
                val response = webService.fetchTasks() // Call HTTP (opération longue)
                if (!response.isSuccessful) { // à cette ligne, on a reçu la réponse de l'API
                    Log.e("Network", "Error: ${response.message()}")
                    tasksStateFlow.value = TaskListState.Error(response.message())
                    return@launch
                }
                val fetchedTasks = response.body()!!
                tasksStateFlow.value = TaskListState.Success(fetchedTasks) // on modifie le flow, ce qui déclenche ses observers
            } catch (e: Exception) {
                Log.e("Network", "Error: ${e.message}")
                tasksStateFlow.value = TaskListState.Error(e.message ?: "Une erreur est survenue")
            }
        }
    }

    fun add(task: Task) {
        viewModelScope.launch {
            try {
                val response = webService.create(task)
                if (!response.isSuccessful) {
                    Log.e("Network", "Error: ${response.message()}")
                    return@launch
                }

                val createdTask = response.body()!!
                val currentState = tasksStateFlow.value
                if (currentState is TaskListState.Success) {
                    tasksStateFlow.value = TaskListState.Success(currentState.list + createdTask)
                }
            } catch (e: Exception) {
                Log.e("Network", "Error: ${e.message}")
            }
        }
    }

    fun update(task: Task) {
        viewModelScope.launch {
            try {
                val response = webService.update(task, task.id)
                if (!response.isSuccessful) {
                    Log.e("Network", "Error: ${response.message()}")
                    return@launch
                }

                val updatedTask = response.body()!!
                val currentState = tasksStateFlow.value
                if (currentState is TaskListState.Success) {
                    val updatedList = currentState.list.map {
                        if (it.id == updatedTask.id) updatedTask else it
                    }
                    tasksStateFlow.value = TaskListState.Success(updatedList)
                }
            } catch (e: Exception) {
                Log.e("Network", "Error: ${e.message}")
            }
        }
    }

    fun delete(task: Task) {
        viewModelScope.launch {
            try {
                val response = webService.delete(task.id)
                if (!response.isSuccessful) {
                    Log.e("Network", "Error: ${response.message()}")
                    return@launch
                }

                val currentState = tasksStateFlow.value
                if (currentState is TaskListState.Success) {
                    tasksStateFlow.value = TaskListState.Success(currentState.list - task)
                }
            } catch (e: Exception) {
                Log.e("Network", "Error: ${e.message}")
            }
        }
    }
}

