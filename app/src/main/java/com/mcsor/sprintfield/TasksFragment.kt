package com.mcsor.sprintfield

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.mcsor.sprintfield.databinding.FragmentTasksBinding
import com.mcsor.sprintfield.databinding.TaskCardViewBinding
import com.mcsor.sprintfield.model.*
import com.mcsor.sprintfield.dao.TaskDao
import java.util.Locale
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle

class TasksFragment : Fragment() {
    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!
    private val dao: TaskDao = TaskDao()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.tasks_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_add_task -> {
                        showAddTaskDialog()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        val rootView = binding.root

        binding.loadingProgressBar.visibility = View.VISIBLE

        dao.getTasks(
            onSuccess = { tasks ->
                for (task in tasks) {
                    addTaskCard(task)
                    Log.d("TasksFragment", "Task document ID: ${task.id}")
                }
                binding.loadingProgressBar.visibility = View.GONE
            },
            onFailure = { exception ->
                Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                binding.loadingProgressBar.visibility = View.GONE
            }
        )
        return rootView
    }

    private fun addTaskCard(task: Task) {
        val cardBinding = TaskCardViewBinding.inflate(layoutInflater)
        cardBinding.title.text = task.title
        cardBinding.description.text = task.description
        cardBinding.timeEstimate.text = task.timeEstimate

        task.urgency = if (task.issues.isNotEmpty()) {
            TaskUrgency.BLOCKED
        } else {
            task.urgency
        }

        cardBinding.chipOpen.text = if (task.open) "Open" else "Closed"
        cardBinding.chipOpen.chipBackgroundColor = ColorStateList.valueOf(
            ContextCompat.getColor(
                requireContext(),
                if (task.open) R.color.chip_color_positive else R.color.md_theme_surfaceDim_highContrast
            )
        )
        cardBinding.chipOpen.apply {
            isCheckable = false
            isClickable = false
        }

        cardBinding.chipOpen.setTextColor(
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    if (task.open) R.color.on_chip_color_positive else R.color.md_theme_onSurfaceVariant_highContrast
                )
            )
        )

        cardBinding.chipUrgent.text = task.urgency.toString().lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        when (task.urgency) {
            TaskUrgency.URGENT -> {
                cardBinding.chipUrgent.setTextColor(
                    ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.on_chip_color_negative)
                    )
                )
                cardBinding.chipUrgent.chipBackgroundColor = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.chip_color_negative)

                )
            }

            TaskUrgency.BLOCKED -> {
                cardBinding.chipUrgent.setTextColor(
                    ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.on_chip_color_negative)
                    )
                )
                cardBinding.chipUrgent.chipBackgroundColor = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.chip_color_negative)

                )
            }

            TaskUrgency.MODERATE -> {
                cardBinding.chipUrgent.setTextColor(
                    ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.on_chip_color_neutral)
                    )
                )
                cardBinding.chipUrgent.chipBackgroundColor = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.chip_color_neutral)

                )
            }

            TaskUrgency.LOW -> {
                cardBinding.chipUrgent.setTextColor(
                    ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.on_chip_color_positive)
                    )
                )
                cardBinding.chipUrgent.chipBackgroundColor = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.chip_color_positive)

                )
            }
        }
        cardBinding.chipUrgent.apply {
            isCheckable = false
            isClickable = false
        }

        cardBinding.chipProject.text = task.project
        cardBinding.chipProject.apply {
            isCheckable = false
            isClickable = false
        }

        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = 8
            bottomMargin = 12
            marginStart = 12
            marginEnd = 12
        }

        cardBinding.openButton.setOnClickListener {
            val bundle = Bundle().apply {
                putString("title", cardBinding.title.text.toString())
                putString("id", task.id)
                putString("description", cardBinding.description.text.toString())
                putString("time", cardBinding.timeEstimate.text.toString())
                putString("urgency", cardBinding.chipUrgent.text.toString())
                putString("assignee", cardBinding.asignee.text.toString())
                putParcelableArrayList("issues", ArrayList(task.issues))
            }

            findNavController().navigate(
                R.id.action_tasksFragment_to_taskDetailsFragment,
                bundle
            )
        }

        cardBinding.root.layoutParams = layoutParams
        binding.taskContainer.addView(cardBinding.root)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showAddTaskDialog() {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_task, null)

        val urgencySpinner: Spinner = dialogView.findViewById(R.id.inputUrgency)
        val urgencyOptions = arrayOf("Low", "Moderate", "Urgent")
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, urgencyOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        urgencySpinner.adapter = adapter

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add New Task")
            .setView(dialogView)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            addButton.setOnClickListener {
                val titleField = dialogView.findViewById<EditText>(R.id.inputTitle)
                val descField = dialogView.findViewById<EditText>(R.id.inputDescription)
                val timeField = dialogView.findViewById<EditText>(R.id.inputTimeEstimate)

                val title = titleField.text.toString().trim()
                val description = descField.text.toString().trim()
                val timeEstimate = timeField.text.toString().trim()
                val urgencyStr = urgencySpinner.selectedItem.toString()

                if (title.isEmpty() || description.isEmpty() || timeEstimate.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.fill_out),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                val urgency = when (urgencyStr) {
                    "Low" -> TaskUrgency.LOW
                    "Moderate" -> TaskUrgency.MODERATE
                    "Urgent" -> TaskUrgency.URGENT
                    else -> TaskUrgency.MODERATE
                }

                val newTask = Task(
                    title,
                    description,
                    timeEstimate,
                    open = true,
                    urgency = urgency,
                    project = "Placeholder",
                    assignee = "Placeholder"
                )

                addButton.isEnabled = false
                val progressBar = dialogView.findViewById<ProgressBar>(R.id.uploadProgressBar)
                progressBar.visibility = View.VISIBLE

                dao.addTask(newTask,
                    onSuccess = { taskWithId ->
                        dialog.dismiss()
                        Toast.makeText(requireContext(), "Task added successfully!", Toast.LENGTH_SHORT).show()
                        addTaskCard(taskWithId)
                    },
                    onFailure = {
                        addButton.isEnabled = true
                        progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), "Failed to add task: ${it.message}", Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
        dialog.show()
    }
}