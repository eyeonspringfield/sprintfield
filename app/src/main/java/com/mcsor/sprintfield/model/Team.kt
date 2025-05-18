package com.mcsor.sprintfield.model

data class Team(
    val id: String = "",
    val name: String = "",
    var memberIds: List<String> = emptyList()
)