package com.mcsor.sprintfield.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class Sprint(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val startDate: Long,
    val endDate: Long,
    val tasks: List<TaskItem> = emptyList(),
    val isCurrent: Boolean = false
) : Parcelable {
    val progress: Int
        get() = if (tasks.isEmpty()) 0 else {
            val completed = tasks.count { !it.open }
            (completed * 100) / tasks.size
        }
}