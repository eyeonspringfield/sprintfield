package com.mcsor.sprintfield.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Project(
    val id: String = "",
    val title: String = "",
    val sprintIds: List<String> = emptyList(),
    val status: ProjectStatus = ProjectStatus.ACTIVE
) : Parcelable

enum class ProjectStatus {
    ACTIVE,
    COMPLETED,
    ARCHIVED
}
