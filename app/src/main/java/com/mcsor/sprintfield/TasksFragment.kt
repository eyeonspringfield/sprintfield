package com.mcsor.sprintfield

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip
import com.mcsor.sprintfield.databinding.FragmentTasksBinding
import com.mcsor.sprintfield.databinding.TaskCardViewBinding
import java.util.Locale


enum class TaskUrgency{
    URGENT,
    MODERATE,
    LOW
}

data class Task(
    val title: String,
    val description: String,
    val timeEstimate: String,
    val open: Boolean,
    val urgency: TaskUrgency,
    val project: String
)

class TasksFragment : Fragment() {
    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        val rootView = binding.root

        val tasks = listOf(
            Task("Center the div on index.html", "We need it centered ASAP or chaos ensues.", "18 hrs", true, TaskUrgency.URGENT, "SampleProject"),
            Task("Fix login bug", "OAuth2 breaks on Safari. Needs patching.", "5 hrs", false, TaskUrgency.MODERATE, "SampleProject"),
            Task("Design homepage", "Modernize landing page UI", "12 hrs", true, TaskUrgency.MODERATE, "SampleProject"),
            Task("Create logo", "New logo old one sucks", "10 hrs", true, TaskUrgency.LOW, "SampleProject"),
            Task("Fix critical payment issue", "Users unable to pay for service, thus halting new signons", "12 hrs", true, TaskUrgency.URGENT, "SampleProject")
        )

        for (task in tasks) {
            val cardBinding = TaskCardViewBinding.inflate(layoutInflater)
            cardBinding.title.text = task.title
            cardBinding.description.text = task.description
            cardBinding.timeEstimate.text = task.timeEstimate

            cardBinding.chipOpen.text = if (task.open) "Open" else "Closed"
            cardBinding.chipOpen.chipBackgroundColor = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), if (task.open) R.color.chip_color_positive else R.color.md_theme_surfaceDim_highContrast)
            )
            cardBinding.chipOpen.apply{
                isCheckable = false
                isClickable = false
            }

            cardBinding.chipOpen.setTextColor(
                ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), if (task.open) R.color.on_chip_color_positive else R.color.md_theme_onSurfaceVariant_highContrast)
                )
            )

            cardBinding.chipUrgent.text = task.urgency.toString().lowercase()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            when(task.urgency){
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
            cardBinding.chipUrgent.apply{
                isCheckable = false
                isClickable = false
            }

            cardBinding.chipProject.text = task.project
            cardBinding.chipProject.apply{
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

            cardBinding.root.layoutParams = layoutParams

            binding.taskContainer.addView(cardBinding.root)
        }

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}