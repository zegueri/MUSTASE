package com.example.mustase.list

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.mustase.R
import com.example.mustase.databinding.FragmentTaskListBinding
import com.example.mustase.detail.DetailFragment
import kotlinx.coroutines.launch

class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskListViewModel by viewModels()

    private val adapterListener: TaskListListener = object : TaskListListener {
        override fun onClickDelete(task: Task) {
            viewModel.delete(task)
        }

        override fun onClickEdit(task: Task) {
            // Naviguer vers DetailFragment pour éditer la tâche
            val fragment = DetailFragment(task.id)
            parentFragmentManager.commit {
                replace(R.id.fragment_container, fragment)
                addToBackStack(null)
            }
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.taskRecyclerView.adapter = adapter

        // Observer le StateFlow du ViewModel
        lifecycleScope.launch {
            viewModel.tasksStateFlow.collect { state ->
                when (state) {
                    is TaskListViewModel.TaskListState.Loading -> {
                        // Afficher un indicateur de chargement si nécessaire
                    }
                    is TaskListViewModel.TaskListState.Error -> {
                        // Afficher une erreur si nécessaire
                    }
                    is TaskListViewModel.TaskListState.Success -> {
                        adapter.submitList(state.list)
                    }
                }
            }
        }

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
                viewModel.refresh() // Rafraîchir après modification
            }
        }

        binding.fabAddTask.setOnClickListener {
            // Naviguer vers DetailFragment pour ajouter une nouvelle tâche
            val fragment = DetailFragment()
            parentFragmentManager.commit {
                replace(R.id.fragment_container, fragment)
                addToBackStack(null)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Rafraîchir les données à chaque fois qu'on revient sur cet écran
        viewModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
