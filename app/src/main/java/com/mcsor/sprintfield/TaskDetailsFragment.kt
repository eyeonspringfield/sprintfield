package com.mcsor.sprintfield

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import com.mcsor.sprintfield.dao.IssueDao
import com.mcsor.sprintfield.dao.TaskDao
import com.mcsor.sprintfield.model.Issue

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

        view.findViewById<TextView>(R.id.detailsTitle).text = title
        view.findViewById<TextView>(R.id.detailsUrgency).text = urgency
        view.findViewById<TextView>(R.id.detailsDescription).text = description
        view.findViewById<TextView>(R.id.detailsTime).text = "Estimated Time: $time"

        issuesContainer = view.findViewById(R.id.issuesContainer)
        loadIssuesForTask(taskId)
        addIssueButton.setOnClickListener {
            showAddIssueDialog(taskId)
        }
    }

    private fun showAddIssueDialog(taskId: String) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_issue, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add New Issue")
            .setView(dialogView)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
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
                            "Issue added successfully!",
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
                            "Failed to add issue: ${it.message}",
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
                            text = issue.title
                            textSize = 14f
                            setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_onSurface))
                            layoutParams = LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            ).apply {
                                topMargin = 8
                            }
                            setPadding(16, 8, 16, 8)
                        }
                        issuesContainer?.addView(issueView)
                    }
                }
            },
            onFailure = {
                Toast.makeText(requireContext(), "Failed to load issues: ${it.message}", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun addNoIssuesView() {
        val noIssuesView = TextView(context).apply {
            text = "No issues reported"
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
}
