package com.mcsor.sprintfield

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.mcsor.sprintfield.model.Team
import com.mcsor.sprintfield.model.User

class TeamsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TeamsAdapter
    private val teams = mutableListOf<Team>()

    private val firestore = FirebaseFirestore.getInstance()
    private val teamsCollection = firestore.collection("teams")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_teams, container, false)

        recyclerView = view.findViewById(R.id.teamsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = TeamsAdapter(requireContext(), teams) { team ->
            openTeamDetails(team)
        }
        recyclerView.adapter = adapter

        loadTeams()

        val addTeamButton = view.findViewById<Button>(R.id.addTeamButton)
        addTeamButton.setOnClickListener {
            showAddTeamDialog()
        }

        adapter.onEditClick = { team -> showEditTeamDialog(team) }
        adapter.onDeleteClick = { team -> showDeleteTeamConfirmation(team) }

        return view
    }

    private fun loadTeams() {
        teamsCollection.get()
            .addOnSuccessListener { snapshot ->
                teams.clear()
                for (doc in snapshot.documents) {
                    val team = doc.toObject(Team::class.java)
                    if (team != null) teams.add(team)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), getString(R.string.failed_load_teams, e.message), Toast.LENGTH_LONG).show()
            }
    }

    private fun showAddTeamDialog() {
        val inputView = layoutInflater.inflate(R.layout.dialog_add_team, null)
        val teamNameInput = inputView.findViewById<EditText>(R.id.teamNameInput)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.add_team))
            .setView(inputView)
            .setPositiveButton(getString(R.string.add)) { _, _ ->
                val name = teamNameInput.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(requireContext(), getString(R.string.team_name_empty), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                addTeam(name)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun addTeam(name: String) {
        val newTeamRef = teamsCollection.document()
        val newTeam = Team(
            id = newTeamRef.id,
            name = name,
            memberIds = emptyList()
        )

        newTeamRef.set(newTeam)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), getString(R.string.team_added), Toast.LENGTH_SHORT).show()
                loadTeams()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to add team: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showEditTeamDialog(team: Team) {
        val inputView = layoutInflater.inflate(R.layout.dialog_add_team, null)
        val teamNameInput = inputView.findViewById<EditText>(R.id.teamNameInput)
        teamNameInput.setText(team.name)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.edit_team_name))
            .setView(inputView)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val newName = teamNameInput.text.toString().trim()
                if (newName.isEmpty()) {
                    Toast.makeText(requireContext(), getString(R.string.team_name_empty), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                updateTeamName(team, newName)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun updateTeamName(team: Team, newName: String) {
        teamsCollection.document(team.id)
            .update("name", newName)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), getString(R.string.team_updated), Toast.LENGTH_SHORT).show()
                loadTeams()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to update team: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showDeleteTeamConfirmation(team: Team) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_team))
            .setMessage(getString(R.string.are_you_sure_delete, team.name))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                deleteTeam(team)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun deleteTeam(team: Team) {
        teamsCollection.document(team.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), getString(R.string.team_deleted), Toast.LENGTH_SHORT).show()
                loadTeams()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to delete team: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun openTeamDetails(team: Team) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_team_details, null)
        val teamNameText = dialogView.findViewById<TextView>(R.id.teamNameText)
        val membersRecyclerView = dialogView.findViewById<RecyclerView>(R.id.membersRecyclerView)
        val userEmailInput = dialogView.findViewById<EditText>(R.id.userEmailInput)

        teamNameText.text = team.name
        membersRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val members = mutableListOf<User>()
        val adapter = MemberAdapter(members)
        membersRecyclerView.adapter = adapter

        fun loadMembers() {
            members.clear()
            if (team.memberIds.isEmpty()) {
                adapter.notifyDataSetChanged()
                return
            }

            firestore.collection("users")
                .whereIn("uid", team.memberIds)
                .get()
                .addOnSuccessListener { snapshot ->
                    for (doc in snapshot.documents) {
                        val user = doc.toObject(User::class.java)
                        if (user != null) members.add(user)
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to load members: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        loadMembers()

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.team_details))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.add_member)) { _, _ ->
                val email = userEmailInput.text.toString().trim()
                if (email.isEmpty()) {
                    Toast.makeText(requireContext(), getString(R.string.email_empty), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                addUserToTeam(team, email)

                userEmailInput.setText("")
                teamsCollection.document(team.id).get()
                    .addOnSuccessListener { doc ->
                        val updatedTeam = doc.toObject(Team::class.java)
                        if (updatedTeam != null) {
                            team.memberIds = updatedTeam.memberIds
                            loadMembers()
                        }
                    }
            }
            .setNegativeButton(getString(R.string.close), null)
            .create()

        dialog.show()
    }


    private fun addUserToTeam(team: Team, email: String) {
        firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    Toast.makeText(requireContext(), getString(R.string.user_not_found), Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val userDoc = snapshot.documents[0]
                val userId = userDoc.getString("uid")
                if (userId == null) {
                    Toast.makeText(requireContext(), getString(R.string.invalid_user_data), Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                teamsCollection.document(team.id)
                    .update("memberIds", FieldValue.arrayUnion(userId))
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), getString(R.string.user_added_to_team), Toast.LENGTH_SHORT).show()
                        loadTeams()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Failed to add user: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to find user: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

}

class MemberAdapter(private val members: List<User>) : RecyclerView.Adapter<MemberAdapter.MemberViewHolder>() {

    class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emailText: TextView = itemView.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.emailText.text = members[position].email
    }

    override fun getItemCount(): Int = members.size
}
