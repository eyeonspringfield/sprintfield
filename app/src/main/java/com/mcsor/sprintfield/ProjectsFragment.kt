package com.mcsor.sprintfield

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.mcsor.sprintfield.model.Project
import com.mcsor.sprintfield.model.ProjectStatus
import com.mcsor.sprintfield.model.Sprint

class ProjectsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProjectsAdapter
    private val projects = mutableListOf<Project>()
    private var allSprints = listOf<Sprint>()

    private val firestore = FirebaseFirestore.getInstance()
    private val projectsCollection = firestore.collection("projects")
    private val sprintsCollection = firestore.collection("sprints")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_projects, container, false)

        recyclerView = view.findViewById(R.id.projectsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ProjectsAdapter(projects) { project ->
            openProjectSprints(project)
        }
        recyclerView.adapter = adapter

        loadProjects()
        loadAllSprints()

        val addProjectButton = view.findViewById<Button>(R.id.addProjectButton)
        addProjectButton.setOnClickListener {
            showAddProjectDialog()
        }

        adapter.onEditClick = { project ->
            showEditProjectDialog(project)
        }

        adapter.onDeleteClick = { project ->
            showDeleteProjectConfirmation(project)
        }


        val addSprintToProjectButton = view.findViewById<Button>(R.id.addSprintToProjectButton)
        addSprintToProjectButton.setOnClickListener {
            if (projects.isEmpty()) {
                Toast.makeText(requireContext(), requireContext().getString(R.string.no_project), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (allSprints.isEmpty()) {
                Toast.makeText(requireContext(), requireContext().getString(R.string.no_sprint), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            showSelectProjectDialog()
        }

        return view
    }

    private fun loadAllSprints() {
        sprintsCollection.get()
            .addOnSuccessListener { snapshot ->
                allSprints = snapshot.documents.mapNotNull { it.toObject(Sprint::class.java) }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.failed_sprint_fetch, e.message),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun loadProjects() {
        projectsCollection.get()
            .addOnSuccessListener { querySnapshot ->
                projects.clear()
                for (doc in querySnapshot.documents) {
                    val project = doc.toObject(Project::class.java)
                    if (project != null) {
                        projects.add(project)
                    }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.failed_project_fetch, e.message),
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun showSelectProjectDialog() {
        val projectTitles = projects.map { it.title }.toTypedArray()
        var selectedProjectIndex = 0

        AlertDialog.Builder(requireContext())
            .setTitle(requireContext().getString(R.string.select_project))
            .setSingleChoiceItems(projectTitles, 0) { _, which ->
                selectedProjectIndex = which
            }
            .setPositiveButton(requireContext().getString(R.string.next)) { dialog, _ ->
                dialog.dismiss()
                showSprintSelectionDialog(projects[selectedProjectIndex])
            }
            .setNegativeButton(requireContext().getString(R.string.cancel), null)
            .show()
    }

    private fun showSprintSelectionDialog(project: Project) {
        val sprintTitles = allSprints.map { it.title }.toTypedArray()
        val checkedSprints = BooleanArray(sprintTitles.size) { false }

        AlertDialog.Builder(requireContext())
            .setTitle(requireContext().getString(R.string.select_sprint))
            .setMultiChoiceItems(sprintTitles, checkedSprints) { _, which, isChecked ->
                checkedSprints[which] = isChecked
            }
            .setPositiveButton(requireContext().getString(R.string.add)) { dialog, _ ->
                dialog.dismiss()
                val selectedSprintIds =
                    allSprints.filterIndexed { index, _ -> checkedSprints[index] }
                        .map { it.id }
                        .filter { it !in project.sprintIds }

                if (selectedSprintIds.isEmpty()) {
                    Toast.makeText(requireContext(), requireContext().getString(R.string.no_new_sprint_selected), Toast.LENGTH_SHORT)
                        .show()
                    return@setPositiveButton
                }

                addSprintIdsToProject(project, selectedSprintIds)
            }
            .setNegativeButton(requireContext().getString(R.string.cancel), null)
            .show()
    }

    private fun addSprintIdsToProject(project: Project, sprintIdsToAdd: List<String>) {
        val updatedSprintIds = project.sprintIds.toMutableList().apply { addAll(sprintIdsToAdd) }

        projectsCollection.document(project.id)
            .update("sprintIds", updatedSprintIds)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), requireContext().getString(R.string.sprint_connected_to_project), Toast.LENGTH_SHORT)
                    .show()
                loadProjects()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.failed_project_update, e.message),
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun showAddProjectDialog() {
        val inputView = layoutInflater.inflate(R.layout.dialog_add_project, null)
        val projectTitleInput = inputView.findViewById<EditText>(R.id.projectTitleInput)

        AlertDialog.Builder(requireContext())
            .setTitle(requireContext().getString(R.string.add_new_project))
            .setView(inputView)
            .setPositiveButton(requireContext().getString(R.string.add)) { _, _ ->
                val title = projectTitleInput.text.toString().trim()
                if (title.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        requireContext().getString(R.string.project_title_cant_be_empty),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                addProject(title)
            }
            .setNegativeButton(requireContext().getString(R.string.cancel), null)
            .show()
    }

    private fun addProject(title: String) {
        val newProjectRef = projectsCollection.document()
        val newProject = Project(
            id = newProjectRef.id,
            title = title,
            sprintIds = emptyList(),
            status = ProjectStatus.ACTIVE
        )

        newProjectRef.set(newProject)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), requireContext().getString(R.string.project_added), Toast.LENGTH_SHORT)
                    .show()
                loadProjects()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.failed_project_adding, e.message),
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun openProjectSprints(project: Project) {
        Toast.makeText(
            requireContext(),
            requireContext().getString(R.string.long_hold_to_edit_or_delete, project.title),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showEditProjectDialog(project: Project) {
        val inputView = layoutInflater.inflate(R.layout.dialog_add_project, null)
        val projectTitleInput = inputView.findViewById<EditText>(R.id.projectTitleInput)
        projectTitleInput.setText(project.title)

        AlertDialog.Builder(requireContext())
            .setTitle(requireContext().getString(R.string.edit_project_name))
            .setView(inputView)
            .setPositiveButton(requireContext().getString(R.string.save)) { _, _ ->
                val newTitle = projectTitleInput.text.toString().trim()
                if (newTitle.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        requireContext().getString(R.string.project_title_cant_be_empty),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }
                updateProjectTitle(project, newTitle)
            }
            .setNegativeButton(requireContext().getString(R.string.cancel), null)
            .show()
    }

    private fun updateProjectTitle(project: Project, newTitle: String) {
        projectsCollection.document(project.id)
            .update("title", newTitle)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), requireContext().getString(R.string.project_updated), Toast.LENGTH_SHORT).show()
                loadProjects()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.failed_project_delete, e.message),
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun showDeleteProjectConfirmation(project: Project) {
        AlertDialog.Builder(requireContext())
            .setTitle(requireContext().getString(R.string.delete_project))
            .setMessage(requireContext().getString(R.string.are_you_sure_delete, project.title))
            .setPositiveButton(requireContext().getString(R.string.delete)) { _, _ ->
                deleteProject(project)
            }
            .setNegativeButton(requireContext().getString(R.string.cancel), null)
            .show()
    }

    private fun deleteProject(project: Project) {
        projectsCollection.document(project.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), requireContext().getString(R.string.project_deleted), Toast.LENGTH_SHORT).show()
                loadProjects()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.failed_project_delete, e.message),
                    Toast.LENGTH_LONG
                ).show()
            }
    }

}
