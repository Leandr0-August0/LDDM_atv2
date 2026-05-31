package com.fatec.at2_base

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.concurrent.atomic.AtomicInteger

private val taskIds = AtomicInteger(2)
private val tasks = mutableListOf(
    TaskItem(1, "Estudar Ktor"),
    TaskItem(2, "Criar tela em Jetpack Compose"),
)

fun main() {
    embeddedServer(Netty, port = 8000, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
    }

    routing {
        get("/") {
            call.respondText("Backend Ktor da Lista de Tarefas em execução.")
        }
        get("/tasks") {
            call.respond(tasks)
        }
        post("/tasks") {
            val request = call.receive<CreateTaskRequest>()
            val title = request.title.trim()

            if (title.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Informe uma tarefa."))
                return@post
            }

            val task = TaskItem(id = taskIds.incrementAndGet(), title = title)
            tasks += task
            call.respond(HttpStatusCode.Created, task)
        }
        put("/tasks/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Id invalido."))
                return@put
            }

            val index = tasks.indexOfFirst { it.id == id }
            if (index == -1) {
                call.respond(HttpStatusCode.NotFound, mapOf("message" to "Tarefa nao encontrada."))
                return@put
            }

            val request = call.receive<UpdateTaskRequest>()
            val updatedTask = tasks[index].copy(done = request.done)
            tasks[index] = updatedTask
            call.respond(updatedTask)
        }
    }
}
