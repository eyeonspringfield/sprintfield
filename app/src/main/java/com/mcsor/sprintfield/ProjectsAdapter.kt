package com.mcsor.sprintfield

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mcsor.sprintfield.model.Project

class ProjectsAdapter(
    private val projects: List<Project>,
    private val onProjectClick: (Project) -> Unit
) : RecyclerView.Adapter<ProjectsAdapter.ProjectViewHolder>() {

    class ProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.projectTitle)
        val sprintCount: TextView = itemView.findViewById(R.id.projectSprintCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.project_list_item, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = projects[position]
        holder.title.text = project.title
        holder.sprintCount.text =
            holder.itemView.context.getString(R.string.sprint_s, project.sprintIds.size)
        holder.itemView.setOnClickListener {
            onProjectClick(project)
        }
        holder.itemView.setOnLongClickListener { view ->
            showPopupMenu(view, project)
            true
        }
    }

    private fun showPopupMenu(view: View, project: Project) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.project_item_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> {
                    onEditClick(project)
                    true
                }

                R.id.action_delete -> {
                    onDeleteClick(project)
                    true
                }

                else -> false
            }
        }
        popup.show()
    }

    var onEditClick: (Project) -> Unit = {}
    var onDeleteClick: (Project) -> Unit = {}

    override fun getItemCount(): Int = projects.size
}
