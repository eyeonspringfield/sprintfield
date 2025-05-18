package com.mcsor.sprintfield

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mcsor.sprintfield.model.Team

class TeamsAdapter(
    private val context: Context,
    private val teams: List<Team>,
    private val onTeamClick: (Team) -> Unit
) : RecyclerView.Adapter<TeamsAdapter.TeamViewHolder>() {


    class TeamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.teamName)
        val memberCount: TextView = itemView.findViewById(R.id.teamMemberCount)
    }

    var onEditClick: (Team) -> Unit = {}
    var onDeleteClick: (Team) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.team_list_item, parent, false)
        return TeamViewHolder(view)
    }

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        val team = teams[position]
        holder.name.text = team.name
        holder.memberCount.text = context.getString(R.string.member_s, team.memberIds.size)

        holder.itemView.setOnClickListener {
            onTeamClick(team)
        }

        holder.itemView.setOnLongClickListener { view ->
            showPopupMenu(view, team)
            true
        }
    }

    private fun showPopupMenu(view: View, team: Team) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.team_item_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> {
                    onEditClick(team)
                    true
                }
                R.id.action_delete -> {
                    onDeleteClick(team)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    override fun getItemCount(): Int = teams.size
}
