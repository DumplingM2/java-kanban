package tasktracker.http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tasktracker.manager.TaskManager;
import tasktracker.tasks.Task;

import java.io.IOException;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public TaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.gson = tasktracker.http.HttpTaskServer.getGson(); // Используем единый Gson
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("/tasks".equals(path)) {
                // GET для получения всех задач
                if ("GET".equals(method)) {
                    sendText(exchange, gson.toJson(taskManager.getAllTasks()), 200);
                }
                // POST для создания или обновления задачи
                else if ("POST".equals(method)) {
                    handleCreateOrUpdateTask(exchange);
                } else {
                    sendText(exchange, "{\"error\": \"Method not allowed\"}", 405);
                }
            } else if (path.matches("/tasks/\\d+")) {
                // GET для получения задачи по id
                // DELETE для удаления задачи по id
                handleTaskById(exchange, method, path);
            } else {
                sendNotFound(exchange, "Endpoint not found");
            }
        } catch (IllegalArgumentException e) {
            // Если задача пересекается с другими — 406
            sendHasInteractions(exchange, e.getMessage());
        } catch (Exception e) {
            sendText(exchange, "{\"error\": \"" + e.getMessage() + "\"}", 500);
        }
    }

    private void handleCreateOrUpdateTask(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        Task task = gson.fromJson(body, Task.class);

        if (task.getId() == 0) {
            // Создаем новую задачу
            taskManager.createTask(task);
            sendText(exchange, gson.toJson(task), 201);
        } else {
            // Обновляем существующую задачу
            // Проверим, существует ли такая задача
            Task existing = taskManager.getTaskById(task.getId());
            if (existing == null) {
                sendNotFound(exchange, "Task not found");
                return;
            }
            taskManager.updateTask(task);
            sendText(exchange, gson.toJson(task), 200);
        }
    }

    private void handleTaskById(HttpExchange exchange, String method, String path) throws IOException {
        int taskId = Integer.parseInt(path.split("/")[2]);

        if ("GET".equals(method)) {
            Task task = taskManager.getTaskById(taskId);
            if (task != null) {
                sendText(exchange, gson.toJson(task), 200);
            } else {
                sendNotFound(exchange, "Task not found");
            }
        } else if ("DELETE".equals(method)) {
            Task task = taskManager.getTaskById(taskId);
            if (task == null) {
                sendNotFound(exchange, "Task not found");
            } else {
                taskManager.deleteTaskById(taskId);
                sendText(exchange, "{}", 200);
            }
        } else {
            sendText(exchange, "{\"error\": \"Method not allowed\"}", 405);
        }
    }
}
