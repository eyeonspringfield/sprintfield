package com.mcsor.sprintfield

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.mcsor.sprintfield.model.Sprint
import com.mcsor.sprintfield.model.Task
import com.mcsor.sprintfield.model.TaskUrgency
import java.text.SimpleDateFormat
import java.util.*

class SprintDetailFragment : Fragment() {

    companion object {
        private const val ARG_SPRINT = "sprint"

        fun newInstance(sprint: Sprint): SprintDetailFragment {
            val fragment = SprintDetailFragment()
            val args = Bundle()
            args.putParcelable(ARG_SPRINT, sprint)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var sprint: Sprint
    private lateinit var rootView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sprint = arguments?.getParcelable(ARG_SPRINT)
            ?: throw IllegalArgumentException("Sprint argument missing")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_sprint_detail, container, false)

        setupViews()
        return rootView
    }

    private fun setupViews() {
        rootView.findViewById<TextView>(R.id.sprintDetailTitle).text = sprint.title
        rootView.findViewById<TextView>(R.id.sprintDetailDuration).text =
            formatDateRange(sprint.startDateMillis!!, sprint.endDateMillis!!)

        val progressBar = rootView.findViewById<ProgressBar>(R.id.sprintDetailProgressBar)
        val progressText = rootView.findViewById<TextView>(R.id.sprintDetailProgressText)
        val progress = sprint.progress
        progressBar.progress = progress
        progressText.text = getString(R.string.percent_complete, progress.toString())

        val tasksContainer = rootView.findViewById<LinearLayout>(R.id.tasksContainer)
        tasksContainer.removeAllViews()

        val inflater = layoutInflater
        val taskList: List<Task> = sprint.tasks.filterIsInstance<Task>()

        taskList.forEach { task ->
            val taskView = inflater.inflate(R.layout.task_list_item, tasksContainer, false)

            taskView.findViewById<TextView>(R.id.taskTitle).text = task.title
            taskView.findViewById<TextView>(R.id.taskDescription).text = task.description

            val statusText = if (task.open) "Open" else "Closed"
            taskView.findViewById<TextView>(R.id.taskStatus).text = statusText

            val chip = taskView.findViewById<Chip>(R.id.taskUrgencyChip)
            chip.text =
                task.urgency.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            val colorRes = when (task.urgency) {
                TaskUrgency.URGENT -> R.color.chip_color_negative
                TaskUrgency.BLOCKED -> R.color.chip_color_negative
                TaskUrgency.MODERATE -> R.color.chip_color_neutral
                TaskUrgency.LOW -> R.color.chip_color_positive
            }
            chip.setChipBackgroundColorResource(colorRes)
            chip.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.on_chip_color_positive
                )
            )

            tasksContainer.addView(taskView)
        }

        // Back button
        rootView.findViewById<Button>(R.id.backButton).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun formatDateRange(startMillis: Long, endMillis: Long): String {
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return "${formatter.format(Date(startMillis))} - ${formatter.format(Date(endMillis))}"
    }
}
