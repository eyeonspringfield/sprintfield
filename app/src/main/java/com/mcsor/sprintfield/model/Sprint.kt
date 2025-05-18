package com.mcsor.sprintfield.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID
import com.google.firebase.Timestamp

@Parcelize
data class Sprint(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val startDate: Timestamp? = null,
    val endDate: Timestamp? = null,
    val tasks: List<TaskItem> = emptyList(),
    val isCurrent: Boolean = false
) : Parcelable {
    val startDateMillis: Long?
        get() = startDate?.toDate()?.time

    val endDateMillis: Long?
        get() = endDate?.toDate()?.time

    val progress: Int
        get() = if (tasks.isEmpty()) 0 else {
            val completed = tasks.count { !it.open }
            (completed * 100) / tasks.size
        }
}
