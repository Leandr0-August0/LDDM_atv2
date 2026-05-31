# Lista de Tarefas - Ktor e Android Compose

Aplicacao criada a partir do projeto base `i-davies/at2_base`.

## Backend Ktor

O backend roda na porta `8000` e guarda as tarefas em memoria.

```bash
./gradlew :server:run
```

Endpoints:

- `GET /tasks`: retorna a lista de tarefas em JSON.
- `POST /tasks`: cadastra uma nova tarefa.

Exemplo de envio:

```json
{
  "title": "Estudar Jetpack Compose"
}
```

## Aplicativo Android

O app usa Jetpack Compose e consome o backend em:

```text
http://10.0.2.2:8000
```

Esse endereco permite que o emulador Android acesse o servidor rodando no computador. Em dispositivo fisico, troque o `baseUrl` em `shared/src/commonMain/kotlin/com/fatec/at2_base/TaskApi.kt` pelo IP da maquina na rede local.

Para rodar:

1. Inicie o backend com `./gradlew :server:run`.
2. Abra o projeto no Android Studio.
3. Execute o modulo `composeApp` em um emulador ou dispositivo.
4. Digite uma tarefa e toque em `Adicionar tarefa`.

O app mostra feedback visual com carregamento, mensagem de sucesso/erro e atualizacao da lista apos o cadastro.
