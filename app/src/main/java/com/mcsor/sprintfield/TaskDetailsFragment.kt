package com.mcsor.sprintfield

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.mcsor.sprintfield.dao.IssueDao
import com.mcsor.sprintfield.dao.TaskDao
import com.mcsor.sprintfield.model.Issue
import java.util.Locale

class TaskDetailsFragment : Fragment() {
    private val issueDao: IssueDao = IssueDao()
    private val taskDao: TaskDao = TaskDao()
    private var issuesContainer: LinearLayout? = null
    private lateinit var taskId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_task_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val title = arguments?.getString("title") ?: "N/A"
        taskId = arguments?.getString("id") ?: "N/A"
        val urgency = arguments?.getString("urgency") ?: "N/A"
        val description = arguments?.getString("description") ?: "N/A"
        val time = arguments?.getString("time") ?: "N/A"
        val addIssueButton: Button = view.findViewById(R.id.addIssueButton)
        val closeOpenTaskButton: Button = view.findViewById(R.id.closeOpenIssueButton)

        val editTaskButton: Button = view.findViewById(R.id.editTaskButton)

        view.findViewById<TextView>(R.id.detailsTitle).text = title
        view.findViewById<TextView>(R.id.detailsUrgency).text = urgency
        view.findViewById<TextView>(R.id.detailsDescription).text = description
        view.findViewById<TextView>(R.id.detailsTime).text =
            getString(R.string.estimated_time_par, time)

        issuesContainer = view.findViewById(R.id.issuesContainer)
        loadIssuesForTask(taskId)
        addIssueButton.setOnClickListener {
            showAddIssueDialog(taskId)
        }
        closeOpenTaskButton.setOnClickListener {
            closeOrOpenTask(taskId)
        }
        editTaskButton.setOnClickListener {
            showEditTaskDialog()
        }

    }

    private fun showEditTaskDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_task, null)

        val titleEdit = dialogView.findViewById<EditText>(R.id.taskTitleEdit)
        val urgencyEdit = dialogView.findViewById<EditText>(R.id.taskUrgencyEdit)
        val descriptionEdit = dialogView.findViewById<EditText>(R.id.taskDescriptionEdit)
        val timeEdit = dialogView.findViewById<EditText>(R.id.taskTimeEdit)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.editTaskProgressBar)

        titleEdit.setText(view?.findViewById<TextView>(R.id.detailsTitle)?.text)
        urgencyEdit.setText(view?.findViewById<TextView>(R.id.detailsUrgency)?.text)
        descriptionEdit.setText(view?.findViewById<TextView>(R.id.detailsDescription)?.text)
        timeEdit.setText(view?.findViewById<TextView>(R.id.detailsTime)?.text)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.edit_task))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.save), null)
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.setOnClickListener {
                val newTitle = titleEdit.text.toString().trim()
                val newUrgency = urgencyEdit.text.toString().trim().uppercase(Locale.getDefault())
                val newDescription = descriptionEdit.text.toString().trim()
                val newTime = timeEdit.text.toString().trim()

                if (newTitle.isEmpty() || newUrgency.isEmpty() || newDescription.isEmpty() || newTime.isEmpty()) {
                    Toast.makeText(requireContext(), getString(R.string.fill_out), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                saveButton.isEnabled = false
                progressBar.visibility = View.VISIBLE

                val db = Firebase.firestore
                val taskRef = db.collection("tasks").document(taskId)

                val updatedData = mapOf(
                    "title" to newTitle,
                    "urgency" to newUrgency,
                    "description" to newDescription,
                    "time" to newTime
                )

                taskRef.update(updatedData)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), getString(R.string.task_updated), Toast.LENGTH_SHORT).show()

                        view?.findViewById<TextView>(R.id.detailsTitle)?.text = newTitle
                        view?.findViewById<TextView>(R.id.detailsUrgency)?.text = newUrgency
                        view?.findViewById<TextView>(R.id.detailsDescription)?.text = newDescription
                        view?.findViewById<TextView>(R.id.detailsTime)?.text = getString(R.string.estimated_time_par, newTime)

                        progressBar.visibility = View.GONE
                        saveButton.isEnabled = true
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), getString(R.string.task_failed_update, it.message), Toast.LENGTH_LONG).show()
                        progressBar.visibility = View.GONE
                        saveButton.isEnabled = true
                    }
            }
        }

        dialog.show()
    }


    private fun showAddIssueDialog(taskId: String) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_issue, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.new_issue))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.add), null)
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.setOnShowListener {
            val addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            addButton.setOnClickListener {
                val titleField = dialog.findViewById<EditText>(R.id.issueTitle)
                val descField = dialog.findViewById<EditText>(R.id.issueDescription)
                val timeField = dialog.findViewById<EditText>(R.id.issueTimeEstimate)
                val progressBar = dialog.findViewById<ProgressBar>(R.id.uploadProgressBar)

                val title = titleField?.text?.toString()?.trim() ?: ""
                val description = descField?.text?.toString()?.trim() ?: ""
                val timeEstimate = timeField?.text?.toString()?.trim() ?: ""

                if (title.isEmpty() || description.isEmpty() || timeEstimate.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.fill_out),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                val newIssue = Issue(
                    title,
                    description,
                    timeEstimate,
                    open = true,
                    project = "Placeholder",
                    id = taskId
                )

                addButton.isEnabled = false
                progressBar?.visibility = View.VISIBLE

                issueDao.addIssue(
                    newIssue,
                    onSuccess = {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.issue_added),
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                        loadIssuesForTask(taskId)
                    },
                    onFailure = {
                        addButton.isEnabled = true
                        progressBar?.visibility = View.GONE
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.issue_failed_added, it.message),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        }

        dialog.show()
    }

    private fun loadIssuesForTask(taskId: String) {
        issueDao.getIssuesByTaskId(taskId,
            onSuccess = { issues ->
                issuesContainer?.removeAllViews()
                if (issues.isEmpty()) {
                    addNoIssuesView()
                } else {
                    for (issue in issues) {
                        val issueView = TextView(context).apply {
                            text = context.getString(
                                R.string.estimated,
                                issue.title,
                                issue.timeEstimate
                            )
                            textSize = 14f
                            setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_onSurface))
                            layoutParams = LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            ).apply {
                                topMargin = 8
                            }
                            setPadding(16, 8, 16, 8)

                            setOnClickListener {
                                showEditIssueDialog(issue)
                            }

                            setOnLongClickListener {
                                showDeleteIssueDialog(issue)
                                true
                            }
                        }
                        issuesContainer?.addView(issueView)
                    }
                }
            },
            onFailure = {
                Toast.makeText(requireContext(), getString(R.string.issue_failed_load, it.message), Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun showEditIssueDialog(issue: Issue) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_issue, null)

        val titleField = dialogView.findViewById<EditText>(R.id.issueTitle)
        val descField = dialogView.findViewById<EditText>(R.id.issueDescription)
        val timeField = dialogView.findViewById<EditText>(R.id.issueTimeEstimate)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.uploadProgressBar)

        titleField.setText(issue.title)
        descField.setText(issue.description)
        timeField.setText(issue.timeEstimate)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.edit_issue))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.save), null)
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.setOnClickListener {
                val newTitle = titleField.text.toString().trim()
                val newDesc = descField.text.toString().trim()
                val newTime = timeField.text.toString().trim()

                if (newTitle.isEmpty() || newDesc.isEmpty() || newTime.isEmpty()) {
                    Toast.makeText(requireContext(), getString(R.string.fill_out), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val updatedIssue = issue.copy(
                    title = newTitle,
                    description = newDesc,
                    timeEstimate = newTime
                )

                saveButton.isEnabled = false
                progressBar?.visibility = View.VISIBLE

                issueDao.updateIssue(updatedIssue,
                    onSuccess = {
                        Toast.makeText(requireContext(), getString(R.string.issue_updated), Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        loadIssuesForTask(taskId)
                    },
                    onFailure = {
                        saveButton.isEnabled = true
                        progressBar?.visibility = View.GONE
                        Toast.makeText(requireContext(), getString(R.string.issue_failed_update, it.message), Toast.LENGTH_LONG).show()
                    }
                )
            }
        }

        dialog.show()
    }

    private fun showDeleteIssueDialog(issue: Issue) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_issue))
            .setMessage(getString(R.string.are_you_sure_delete, issue.title))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                issueDao.deleteIssue(issue,
                    onSuccess = {
                        Toast.makeText(requireContext(), getString(R.string.issue_deleted), Toast.LENGTH_SHORT).show()
                        loadIssuesForTask(taskId)
                    },
                    onFailure = {
                        Toast.makeText(requireContext(), getString(R.string.delete_failed, it.message), Toast.LENGTH_LONG).show()
                    }
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addNoIssuesView() {
        val noIssuesView = TextView(context).apply {
            text = context.getString(R.string.no_issues_reported)
            textSize = 14f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_onSurface))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 16, 16, 16)
        }
        issuesContainer?.addView(noIssuesView)
    }

    private fun closeOrOpenTask(taskId: String) {
        val db = Firebase.firestore
        val taskRef = db.collection("tasks").document(taskId)

        taskRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val currentOpenStatus = document.getBoolean("open") ?: true
                    val newStatus = !currentOpenStatus

                    taskRef.update("open", newStatus)
                        .addOnSuccessListener {
                            Toast.makeText(
                                requireContext(),
                                if (newStatus) getString(R.string.task_reopened) else getString(R.string.task_closed),
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController().navigate(R.id.tasksFragment)
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to update task: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "Task not found", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error fetching task: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}
