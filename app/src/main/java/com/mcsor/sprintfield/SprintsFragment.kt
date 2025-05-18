package com.mcsor.sprintfield

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.mcsor.sprintfield.model.Sprint
import com.mcsor.sprintfield.model.Task
import com.mcsor.sprintfield.model.TaskUrgency
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldPath
import com.mcsor.sprintfield.dao.TaskDao
import java.util.Calendar

class SprintsFragment : Fragment() {

    private lateinit var pastSprintsContainer: LinearLayout
    private lateinit var currentSprintsContainer: LinearLayout
    private lateinit var rootView: View
    private val db = Firebase.firestore
    private lateinit var loadingProgressBar: ProgressBar


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_sprints, container, false)
        pastSprintsContainer = rootView.findViewById(R.id.pastSprintsContainer)
        currentSprintsContainer = rootView.findViewById(R.id.currentSprintsContainer)
        loadingProgressBar = rootView.findViewById(R.id.loadingProgressBar)
        val addSprintButton = rootView.findViewById<FloatingActionButton>(R.id.addSprintButton)
        addSprintButton.setOnClickListener {
            showAddSprintDialog()
        }


        fetchAndDisplaySprints()
        return rootView
    }

    private fun fetchAndDisplaySprints() {
        loadingProgressBar.visibility = View.VISIBLE

        pastSprintsContainer.removeAllViews()
        currentSprintsContainer.removeAllViews()

        db.collection("sprints")
            .get()
            .addOnSuccessListener { sprintSnapshots ->
                val sprints = sprintSnapshots.documents.mapNotNull { sprintDoc ->
                    val title = sprintDoc.getString("title") ?: return@mapNotNull null
                    val startTimestamp =
                        sprintDoc.getTimestamp("startDate") ?: return@mapNotNull null
                    val endTimestamp = sprintDoc.getTimestamp("endDate") ?: return@mapNotNull null
                    val isCurrent = sprintDoc.getBoolean("isCurrent") ?: false
                    val sprintId = sprintDoc.id
                    val taskIds = sprintDoc.get("taskIds") as? List<String> ?: emptyList()

                    Sprint(
                        id = sprintId,
                        title = title,
                        startDate = startTimestamp,
                        endDate = endTimestamp,
                        tasks = emptyList(),
                        isCurrent = isCurrent
                    ) to taskIds
                }

                if (sprints.isEmpty()) {
                    loadingProgressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "No sprints found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                loadTasksByIds(sprints)
            }
            .addOnFailureListener { e ->
                loadingProgressBar.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    "Failed to load sprints: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }


    private fun loadTasksByIds(sprintsWithTaskIds: List<Pair<Sprint, List<String>>>) {
        val sprintCount = sprintsWithTaskIds.size
        var loadedCount = 0

        val sprintsWithTasks = mutableListOf<Sprint>()

        for ((sprint, taskIds) in sprintsWithTaskIds) {
            if (taskIds.isEmpty()) {
                sprintsWithTasks.add(sprint)
                loadedCount++
                if (loadedCount == sprintCount) {
                    displaySprints(sprintsWithTasks)
                    loadingProgressBar.visibility = View.GONE
                }
                continue
            }

            val chunkedTaskIds = taskIds.chunked(10)
            val tasks = mutableListOf<Task>()

            val chunksCount = chunkedTaskIds.size
            var chunksLoaded = 0

            for (chunk in chunkedTaskIds) {
                db.collection("tasks")
                    .whereIn(FieldPath.documentId(), chunk)
                    .get()
                    .addOnSuccessListener { taskSnapshots ->
                        taskSnapshots.documents.forEach { taskDoc ->
                            val title = taskDoc.getString("title") ?: ""
                            val description = taskDoc.getString("description") ?: ""
                            val timeEstimate = taskDoc.getString("timeEstimate") ?: ""
                            val open = taskDoc.getBoolean("open") ?: true
                            val urgencyStr = taskDoc.getString("urgency") ?: "LOW"
                            val urgency = try {
                                TaskUrgency.valueOf(urgencyStr)
                            } catch (ex: Exception) {
                                TaskUrgency.LOW
                            }
                            val project = taskDoc.getString("project") ?: ""
                            val assignee = taskDoc.getString("assignee") ?: ""
                            val id = taskDoc.id

                            tasks.add(
                                Task(
                                    title = title,
                                    description = description,
                                    timeEstimate = timeEstimate,
                                    open = open,
                                    urgency = urgency,
                                    project = project,
                                    assignee = assignee,
                                    id = id
                                )
                            )
                        }
                        chunksLoaded++
                        if (chunksLoaded == chunksCount) {
                            sprintsWithTasks.add(sprint.copy(tasks = tasks))
                            loadedCount++
                            if (loadedCount == sprintCount) {
                                displaySprints(sprintsWithTasks)
                                loadingProgressBar.visibility = View.GONE
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.failed_to_load_tasks_for_sprint),
                            Toast.LENGTH_LONG
                        ).show()
                        chunksLoaded++
                        if (chunksLoaded == chunksCount) {
                            sprintsWithTasks.add(sprint.copy(tasks = tasks))
                            loadedCount++
                            if (loadedCount == sprintCount) {
                                displaySprints(sprintsWithTasks)
                                loadingProgressBar.visibility = View.GONE
                            }
                        }
                    }
            }
        }
    }


    private fun showAddSprintDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_sprint, null)
        val titleInput = dialogView.findViewById<EditText>(R.id.sprintTitleInput)
        val startDateInput = dialogView.findViewById<TextView>(R.id.startDateInput)
        val endDateInput = dialogView.findViewById<TextView>(R.id.endDateInput)
        val tasksContainer = dialogView.findViewById<LinearLayout>(R.id.tasksSelectionContainer)

        val taskDao = TaskDao()
        var allTasks: List<Task> = emptyList()

        taskDao.getTasks(
            onSuccess = { tasks ->
                allTasks = tasks
                tasks.forEach { task ->
                    val checkBox = CheckBox(requireContext()).apply {
                        text = task.title
                        tag = task.id
                    }
                    tasksContainer.addView(checkBox)
                }
            },
            onFailure = {
                Toast.makeText(requireContext(), getString(R.string.failed_to_load_tasks), Toast.LENGTH_SHORT).show()
            }
        )

        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()

        fun pickDate(textView: TextView) {
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    textView.text = sdf.format(calendar.time)
                    textView.tag = calendar.timeInMillis
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        startDateInput.setOnClickListener { pickDate(startDateInput) }
        endDateInput.setOnClickListener { pickDate(endDateInput) }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.new_sprint))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.add)) { _, _ ->
                val title = titleInput.text.toString().trim()
                val startMillis = startDateInput.tag as? Long
                val endMillis = endDateInput.tag as? Long

                if (title.isBlank() || startMillis == null || endMillis == null) {
                    Toast.makeText(requireContext(), getString(R.string.fill_out), Toast.LENGTH_SHORT)
                        .show()
                    return@setPositiveButton
                }
                if (endMillis < startMillis) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.end_after_start),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                val selectedTaskIds = (0 until tasksContainer.childCount).mapNotNull { idx ->
                    val view = tasksContainer.getChildAt(idx)
                    if (view is CheckBox && view.isChecked) view.tag as? String else null
                }

                addSprint(
                    title, startMillis, endMillis, selectedTaskIds, false
                ) { success ->
                    if (success) {
                        Toast.makeText(requireContext(), getString(R.string.added_sprint), Toast.LENGTH_SHORT).show()
                        fetchAndDisplaySprints()
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.failed_sprint_add), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun addSprint(
        title: String,
        startDateMillis: Long,
        endDateMillis: Long,
        taskIds: List<String>,
        unusedIsCurrent: Boolean,
        onComplete: (Boolean) -> Unit
    ) {
        val today = Date().time

        db.collection("sprints")
            .get()
            .addOnSuccessListener { existingSprints ->
                val overlapping = existingSprints.any { doc ->
                    val existingStart =
                        doc.getTimestamp("startDate")?.toDate()?.time ?: return@any false
                    val existingEnd =
                        doc.getTimestamp("endDate")?.toDate()?.time ?: return@any false
                    !(endDateMillis < existingStart || startDateMillis > existingEnd)
                }

                if (overlapping) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.overlaps),
                        Toast.LENGTH_LONG
                    ).show()
                    onComplete(false)
                    return@addOnSuccessListener
                }

                // Determine if new sprint is the current one
                val newSprintIsCurrent = startDateMillis <= today && today <= endDateMillis

                // If it should be current, reset all others to not current
                val updates = mutableListOf<() -> Unit>()
                if (newSprintIsCurrent) {
                    for (doc in existingSprints) {
                        val docId = doc.id
                        val current = doc.getBoolean("isCurrent") ?: false
                        if (current) {
                            updates.add {
                                db.collection("sprints").document(docId)
                                    .update("isCurrent", false)
                            }
                        }
                    }
                }

                // Apply updates
                updates.forEach { it() }

                // Create the new sprint
                val sprintData = hashMapOf(
                    "title" to title,
                    "startDate" to Timestamp(Date(startDateMillis)),
                    "endDate" to Timestamp(Date(endDateMillis)),
                    "taskIds" to taskIds,
                    "isCurrent" to newSprintIsCurrent
                )

                db.collection("sprints")
                    .add(sprintData)
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Failed to check existing sprints: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
                onComplete(false)
            }
    }

    // Overload displaySprints to accept data loaded from DB
    private fun displaySprints(sprints: List<Sprint>) {
        pastSprintsContainer.removeAllViews()
        val inflater = layoutInflater
        val currentSprint = sprints.find { it.isCurrent }

        val container =
            rootView.findViewById<LinearLayout>(R.id.pastSprintsContainer).parent as ViewGroup

        currentSprint?.let {
            val currentCard = inflateSprintCard(inflater, it)
            currentSprintsContainer.addView(currentCard)
        }

        sprints.filter { !it.isCurrent }.forEach { sprint ->
            val cardView = inflateSprintCard(inflater, sprint)
            pastSprintsContainer.addView(cardView)
        }
    }

    // Your existing inflateSprintCard remains unchanged
    private fun inflateSprintCard(inflater: LayoutInflater, sprint: Sprint): View {
        val card = inflater.inflate(R.layout.sprint_card_view, pastSprintsContainer, false)

        card.findViewById<TextView>(R.id.sprintTitle).text = sprint.title
        card.findViewById<TextView>(R.id.sprintDuration).text =
            formatDateRange(sprint.startDateMillis!!, sprint.endDateMillis!!)

        val progress = sprint.progress
        card.findViewById<ProgressBar>(R.id.sprintProgressBar).progress = progress
        card.findViewById<TextView>(R.id.sprintProgressText).text =
            getString(R.string.percent_complete, progress.toString())

        val chip = card.findViewById<Chip>(R.id.sprintStatus)
        if (sprint.isCurrent) {
            chip.text = getString(R.string.current)
            chip.setChipBackgroundColorResource(R.color.chip_color_positive)
            chip.setTextColor(resources.getColor(R.color.on_chip_color_positive, null))
        } else {
            chip.text = getString(R.string.past)
            chip.setChipBackgroundColorResource(R.color.chip_color_neutral)
            chip.setTextColor(resources.getColor(R.color.on_chip_color_neutral, null))
        }

        card.findViewById<Button>(R.id.openButton).setOnClickListener {
            val bundle = Bundle().apply {
                putParcelable("sprint", sprint)
            }
            findNavController().navigate(R.id.sprintDetailFragment, bundle)
        }

        return card
    }

    private fun formatDateRange(startMillis: Long, endMillis: Long): String {
        val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
        return "${formatter.format(Date(startMillis))} - ${formatter.format(Date(endMillis))}"
    }
}
