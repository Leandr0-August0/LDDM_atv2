package com.fatec.at2_base

import kotlinx.serialization.Serializable

@Serializable
data class TaskItem(
    val id: Int,
    val title: String,
    val done: Boolean = false,
)

@Serializable
data class CreateTaskRequest(
    val title: String,
)

@Serializable
data class UpdateTaskRequest(
    val done: Boolean,
)
