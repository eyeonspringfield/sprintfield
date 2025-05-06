package com.mcsor.sprintfield.dao

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.mcsor.sprintfield.model.Issue
import com.mcsor.sprintfield.model.Task
import com.mcsor.sprintfield.model.TaskUrgency

class IssueDao {
    private val db = Firebase.firestore
    private val issuesCollection = db.collection("issues")

    fun addIssue(issue: Issue, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        issuesCollection
            .add(issue)
            .addOnSuccessListener { docRef ->
                updateTaskUrgency(issue.id, onSuccess, onFailure)
            }
            .addOnFailureListener(onFailure)
    }

    fun getIssuesByTaskId(taskId: String, onSuccess: (List<Issue>) -> Unit, onFailure: (Exception) -> Unit) {
        issuesCollection
            .whereEqualTo("id", taskId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val issues = querySnapshot.documents.mapNotNull { it.toObject(Issue::class.java) }
                onSuccess(issues)
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    private fun updateTaskUrgency(taskId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        getIssueCountForTask(taskId,
            onSuccess = { issueCount ->
                if (issueCount > 0) {
                    db.collection("tasks").document(taskId)
                        .get()
                        .addOnSuccessListener { document ->
                            val task = document.toObject(Task::class.java)
                            val updatedTask = task?.copy(urgency = TaskUrgency.BLOCKED)
                            if (updatedTask != null) {
                                db.collection("tasks").document(taskId)
                                    .set(updatedTask)
                                    .addOnSuccessListener {
                                        onSuccess()
                                    }
                                    .addOnFailureListener(onFailure)
                            } else {
                                onFailure(Exception("Task not found"))
                            }
                        }
                } else {
                    onSuccess()
                }
            },
            onFailure = onFailure
        )
    }

    fun getIssueCountForTask(
        taskId: String,
        onSuccess: (Int) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("issues")
            .whereEqualTo("taskId", taskId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                onSuccess(querySnapshot.size())
            }
            .addOnFailureListener(onFailure)
    }
}