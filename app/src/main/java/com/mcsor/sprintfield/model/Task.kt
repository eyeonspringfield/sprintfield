package com.mcsor.sprintfield.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class TaskUrgency {
    BLOCKED, // when task has an open issue, it is blocked
    URGENT,
    MODERATE,
    LOW
}

interface TaskItem : Parcelable {
    val id: String
    val title: String
    val description: String
    val timeEstimate: String
    val open: Boolean
    val project: String
}

@Parcelize
data class Task(
    override val title: String,
    override val description: String,
    override val timeEstimate: String,
    override val open: Boolean,
    var urgency: TaskUrgency,
    override val project: String,
    val assignee: String,
    val issues: List<TaskItem> = emptyList(),
    override val id: String = ""
) : TaskItem {
    constructor() : this(
        title = "",
        description = "",
        timeEstimate = "",
        open = true,
        urgency = TaskUrgency.LOW,
        project = "",
        assignee = "",
        issues = emptyList(),
        id = ""
    )
}

@Parcelize
data class Issue(
    override val title: String,
    override val description: String,
    override val timeEstimate: String,
    override val open: Boolean,
    override val project: String,
    override val id: String = ""
) : TaskItem {
    constructor() : this(
        title = "",
        description = "",
        timeEstimate = "",
        open = true,
        project = "",
        id = ""
    )
}