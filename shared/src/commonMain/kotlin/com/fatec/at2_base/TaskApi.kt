package com.fatec.at2_base

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class TaskApi(
//    private val baseUrl: String = "http://192.168.3.143:8000",
    private val baseUrl: String = "http://10.0.2.2:8000",
//    private val baseUrl: String = "http://192.168.1.119:8000",
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                },
            )
        }
    }

    suspend fun listTasks(): List<TaskItem> =
        client.get("$baseUrl/tasks").body()

    suspend fun createTask(title: String): TaskItem =
        client.post("$baseUrl/tasks") {
            contentType(ContentType.Application.Json)
            setBody(CreateTaskRequest(title))
        }.body()

    suspend fun updateTaskDone(id: Int, done: Boolean): TaskItem =
        client.put("$baseUrl/tasks/$id") {
            contentType(ContentType.Application.Json)
            setBody(UpdateTaskRequest(done))
        }.body()
}
