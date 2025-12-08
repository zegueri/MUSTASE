package com.example.mustase.detail

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.example.mustase.databinding.FragmentDetailBinding
import com.example.mustase.list.Task
import java.util.UUID

class DetailFragment(
    private val taskId: String? = null,
    private val initialDescription: String? = null
) : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Pré-remplir la description si elle est fournie (partage entrant)
        initialDescription?.let {
            binding.editTextDescription.setText(it)
        }

        // Si on édite, récupérer les infos de la tâche depuis le listener
        taskId?.let {
            // Demander la tâche au parent
            parentFragmentManager.setFragmentResultListener(TASK_DATA_REQUEST_KEY, viewLifecycleOwner) { _, bundle ->
                val task = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getSerializable(TASK_DATA_RESULT_KEY, Task::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    bundle.getSerializable(TASK_DATA_RESULT_KEY) as? Task
                }
                task?.let { existingTask ->
                    binding.editTextTitle.setText(existingTask.title)
                    binding.editTextDescription.setText(existingTask.description)
                }
            }
        }

        binding.buttonValidate.setOnClickListener {
            val title = binding.editTextTitle.text.toString()
            val description = binding.editTextDescription.text.toString()

            if (title.isBlank()) {
                binding.titleInputLayout.error = "Le titre ne peut pas être vide"
                return@setOnClickListener
            }

            binding.titleInputLayout.error = null

            // Créer ou modifier la tâche avec les valeurs saisies
            val task = Task(
                id = taskId ?: UUID.randomUUID().toString(),
                title = title,
                description = description
            )

            // Retourner le résultat au fragment parent
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                bundleOf(RESULT_KEY to task)
            )

            // Retour au fragment précédent
            parentFragmentManager.popBackStack()
        }

        // Bouton de partage
        binding.buttonShare.setOnClickListener {
            val title = binding.editTextTitle.text.toString()
            val description = binding.editTextDescription.text.toString()

            val shareText = if (title.isNotBlank()) {
                "Tâche: $title\n$description"
            } else {
                description
            }

            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val REQUEST_KEY = "add_task_request"
        const val RESULT_KEY = "task_result"
        const val TASK_DATA_REQUEST_KEY = "task_data_request"
        const val TASK_DATA_RESULT_KEY = "task_data_result"
    }
}
