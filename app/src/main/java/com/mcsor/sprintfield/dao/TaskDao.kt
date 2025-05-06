package com.mcsor.sprintfield.dao

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.mcsor.sprintfield.model.Task

class TaskDao {
    private val db = Firebase.firestore
    private val taskCollection = db.collection("tasks")

    fun addTask(task: Task, onSuccess: (Task) -> Unit, onFailure: (Exception) -> Unit) {
        taskCollection.add(task)
            .addOnSuccessListener { documentReference ->
                val taskWithId = task.copy(id = documentReference.id)
                onSuccess(taskWithId)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun getTasks(onSuccess: (List<Task>) -> Unit, onFailure: (Exception) -> Unit) {
        taskCollection.get()
            .addOnSuccessListener { result ->
                val tasks = result.documents.mapNotNull { document ->
                    val task = document.toObject(Task::class.java)
                    task?.copy(id = document.id)
                }
                onSuccess(tasks)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun updateTask(taskId: String, updates: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        taskCollection.document(taskId)
            .update(updates)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun deleteTask(taskId: String, onComplete: (Boolean) -> Unit) {
        taskCollection.document(taskId)
            .delete()
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }
}
