package com.fatec.at2_base

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun App() {
    MaterialTheme {
        val api = remember { TaskApi() }
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

        var tasks by remember { mutableStateOf<List<TaskItem>>(emptyList()) }
        var title by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(true) }
        var isSending by remember { mutableStateOf(false) }

        fun refreshTasks() {
            scope.launch {
                isLoading = true
                runCatching { api.listTasks() }
                    .onSuccess { tasks = it }
                    .onFailure {
                        snackbarHostState.showSnackbar("Nao foi possivel carregar as tarefas.")
                    }
                isLoading = false
            }
        }

        LaunchedEffect(Unit) {
            refreshTasks()
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { contentPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(contentPadding),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Lista de Tarefas",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Dados consumidos do backend Ktor em http://192.168.3.143:8000",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Nova tarefa") },
                                singleLine = true,
                            )
                            Button(
                                onClick = {
                                    val taskTitle = title.trim()
                                    scope.launch {
                                        isSending = true
                                        runCatching { api.createTask(taskTitle) }
                                            .onSuccess {
                                                title = ""
                                                tasks = api.listTasks()
                                                snackbarHostState.showSnackbar("Tarefa adicionada com sucesso.")
                                            }
                                            .onFailure {
                                                snackbarHostState.showSnackbar("Erro ao enviar a tarefa.")
                                            }
                                        isSending = false
                                    }
                                },
                                enabled = title.isNotBlank() && !isSending,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                if (isSending) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Text("Adicionar tarefa")
                                }
                            }
                        }
                    }

                    Text(
                        text = "Tarefas cadastradas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Box(modifier = Modifier.fillMaxSize()) {
                        when {
                            isLoading -> CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                            )

                            tasks.isEmpty() -> Text(
                                text = "Nenhuma tarefa cadastrada.",
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            else -> LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                items(tasks, key = { it.id }) { task ->
                                    TaskRow(
                                        task = task,
                                        onDoneChange = { done ->
                                            scope.launch {
                                                tasks = tasks.map {
                                                    if (it.id == task.id) it.copy(done = done) else it
                                                }

                                                runCatching { api.updateTaskDone(task.id, done) }
                                                    .onSuccess { updatedTask ->
                                                        tasks = tasks.map {
                                                            if (it.id == updatedTask.id) updatedTask else it
                                                        }
                                                        val message = if (updatedTask.done) {
                                                            "Tarefa marcada como concluida."
                                                        } else {
                                                            "Tarefa marcada como pendente."
                                                        }
                                                        snackbarHostState.showSnackbar(message)
                                                    }
                                                    .onFailure {
                                                        tasks = tasks.map {
                                                            if (it.id == task.id) task else it
                                                        }
                                                        snackbarHostState.showSnackbar("Erro ao atualizar a tarefa.")
                                                    }
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskRow(
    task: TaskItem,
    onDoneChange: (Boolean) -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Checkbox(
                checked = task.done,
                onCheckedChange = onDoneChange,
            )
            Text(
                text = "#${task.id}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (task.done) "Concluida" else "Pendente",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
