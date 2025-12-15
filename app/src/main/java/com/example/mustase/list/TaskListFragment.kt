package com.example.mustase.list

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.example.mustase.R
import com.example.mustase.databinding.FragmentTaskListBinding
import com.example.mustase.detail.DetailFragment

class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private val adapterListener: TaskListListener = object : TaskListListener {
        override fun onClickDelete(task: Task) {
            taskList = taskList - task
            refreshAdapter()
        }

        override fun onClickEdit(task: Task) {
            // Naviguer vers DetailFragment pour éditer la tâche
            val fragment = DetailFragment(task.id)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
            // Envoyer les données de la tâche au fragment
            parentFragmentManager.setFragmentResult(
                DetailFragment.TASK_DATA_REQUEST_KEY,
                bundleOf(DetailFragment.TASK_DATA_RESULT_KEY to task)
            )
        }

        override fun onLongClickShare(task: Task): Boolean {
            val shareText = "Tâche: ${task.title}\n${task.description}"
            val sendIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }
            val shareIntent = android.content.Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
            return true
        }
    }

    private val adapter = TaskListAdapter(adapterListener)
    private var taskList: List<Task> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Restaurer la liste sauvegardée ou initialiser avec les tâches par défaut
        taskList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            savedInstanceState?.getSerializable(KEY_TASK_LIST, ArrayList::class.java) as? List<Task>
        } else {
            @Suppress("DEPRECATION")
            savedInstanceState?.getSerializable(KEY_TASK_LIST) as? List<Task>
        } ?: listOf(
            Task(id = "id_1", title = "Task 1", description = "description 1"),
            Task(id = "id_2", title = "Task 2"),
            Task(id = "id_3", title = "Task 3")
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun refreshAdapter() {
        adapter.submitList(taskList.toList()) // Créer une nouvelle liste pour que DiffUtil détecte le changement
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.taskRecyclerView.adapter = adapter
        refreshAdapter()

        // Écouter le résultat du DetailFragment
        parentFragmentManager.setFragmentResultListener(DetailFragment.REQUEST_KEY, viewLifecycleOwner) { _, bundle ->
            val task = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getSerializable(DetailFragment.RESULT_KEY, Task::class.java)
            } else {
                @Suppress("DEPRECATION")
                bundle.getSerializable(DetailFragment.RESULT_KEY) as? Task
            }
            task?.let {
                // Vérifier si c'est un ajout ou une modification
                val existingTaskIndex = taskList.indexOfFirst { existing -> existing.id == it.id }
                taskList = if (existingTaskIndex != -1) {
                    // Modification : remplacer la tâche existante
                    taskList.toMutableList().apply { set(existingTaskIndex, it) }
                } else {
                    // Ajout : ajouter la nouvelle tâche
                    taskList + it
                }
                refreshAdapter()
            }
        }

        binding.fabAddTask.setOnClickListener {
            // Naviguer vers DetailFragment pour ajouter une nouvelle tâche
            val fragment = DetailFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(KEY_TASK_LIST, ArrayList(taskList))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val KEY_TASK_LIST = "task_list"
    }
}

