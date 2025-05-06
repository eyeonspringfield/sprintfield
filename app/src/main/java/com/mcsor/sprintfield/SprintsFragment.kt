package com.mcsor.sprintfield

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.mcsor.sprintfield.model.Sprint
import com.mcsor.sprintfield.model.Task
import com.mcsor.sprintfield.model.TaskItem
import com.mcsor.sprintfield.model.TaskUrgency
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SprintsFragment : Fragment() {

    private lateinit var pastSprintsContainer: LinearLayout
    private lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_sprints, container, false)
        pastSprintsContainer = rootView.findViewById(R.id.pastSprintsContainer)

        displaySprints()
        return rootView
    }

    private fun displaySprints() {
        val allSprints = listOf(
            Sprint(
                title = "Sprint 5: Notification Overhaul",
                startDate = 1714156800000,
                endDate = 1715366400000,
                tasks = generateTasks(12, 6),
                isCurrent = true
            ),
            Sprint(
                title = "Sprint 4: Bug Fix Marathon",
                startDate = 1712966400000,
                endDate = 1714156800000,
                tasks = generateTasks(10, 10)
            ),
            Sprint(
                title = "Sprint 3: UI Refactor",
                startDate = 1711564800000,
                endDate = 1712966400000,
                tasks = generateTasks(8, 8)
            )
        )

        val inflater = layoutInflater
        val currentSprint = allSprints.find { it.isCurrent }
        currentSprint?.let {
            val currentCard = inflateSprintCard(inflater, it)
            val container = rootView.findViewById<LinearLayout>(R.id.pastSprintsContainer).parent as ViewGroup
            container.addView(currentCard, 1)
        }

        allSprints.filter { !it.isCurrent }.forEach { sprint ->
            val cardView = inflateSprintCard(inflater, sprint)
            pastSprintsContainer.addView(cardView)
        }
    }

    private fun inflateSprintCard(inflater: LayoutInflater, sprint: Sprint): View {
        val card = inflater.inflate(R.layout.sprint_card_view, pastSprintsContainer, false)

        card.findViewById<TextView>(R.id.sprintTitle).text = sprint.title
        card.findViewById<TextView>(R.id.sprintDuration).text =
            formatDateRange(sprint.startDate, sprint.endDate)

        val progress = sprint.progress
        card.findViewById<ProgressBar>(R.id.sprintProgressBar).progress = progress
        card.findViewById<TextView>(R.id.sprintProgressText).text = "$progress% complete"

        val chip = card.findViewById<Chip>(R.id.sprintStatus)
        if (sprint.isCurrent) {
            chip.text = "Current"
            chip.setChipBackgroundColorResource(R.color.chip_color_positive)
            chip.setTextColor(resources.getColor(R.color.on_chip_color_positive, null))
        } else {
            chip.text = "Past"
            chip.setChipBackgroundColorResource(R.color.chip_color_neutral)
            chip.setTextColor(resources.getColor(R.color.on_chip_color_neutral, null))
        }

        return card
    }

    private fun formatDateRange(startMillis: Long, endMillis: Long): String {
        val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
        return "${formatter.format(Date(startMillis))} - ${formatter.format(Date(endMillis))}"
    }

    private fun generateTasks(total: Int, completed: Int): List<TaskItem> {
        return List(total) { index ->
            Task(
                title = "Task #$index",
                open = index >= completed,
                description = "Description #$index",
                timeEstimate = "${index * 12 / 7} hrs",
                urgency = TaskUrgency.MODERATE,
                project = "Bruh",
                assignee = "YOU BRUH",
                issues = listOf()
            )
        }
    }
}
