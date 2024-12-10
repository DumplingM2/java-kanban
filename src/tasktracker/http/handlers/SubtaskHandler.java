package tasktracker.http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tasktracker.manager.TaskManager;
import tasktracker.tasks.Subtask;

import java.io.IOException;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public SubtaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.gson = tasktracker.http.HttpTaskServer.getGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("/subtasks".equals(path)) {
                handleSubtasks(exchange, method);
            } else if (path.matches("/subtasks/\\d+")) {
                handleSubtaskById(exchange, method, path);
            } else {
                sendNotFound(exchange, "Endpoint not found");
            }
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange, e.getMessage());
        } catch (Exception e) {
            sendText(exchange, "{\"error\": \"" + e.getMessage() + "\"}", 500);
        }
    }

    private void handleSubtasks(HttpExchange exchange, String method) throws IOException {
        if ("GET".equals(method)) {
            sendText(exchange, gson.toJson(taskManager.getAllSubtasks()), 200);
        } else if ("POST".equals(method)) {
            String body = readRequestBody(exchange);
            Subtask subtask = gson.fromJson(body, Subtask.class);
            if (subtask.getId() == 0) {
                // Создание новой подзадачи
                taskManager.createSubtask(subtask);
                sendText(exchange, gson.toJson(subtask), 201);
            } else {
                // Обновление существующей подзадачи
                Subtask existing = taskManager.getSubtaskById(subtask.getId());
                if (existing == null) {
                    sendNotFound(exchange, "Subtask not found");
                    return;
                }
                taskManager.updateSubtask(subtask);
                sendText(exchange, gson.toJson(subtask), 200);
            }
        } else {
            sendText(exchange, "{\"error\": \"Method not allowed\"}", 405);
        }
    }

    private void handleSubtaskById(HttpExchange exchange, String method, String path) throws IOException {
        try {
            int subtaskId = Integer.parseInt(path.split("/")[2]);
            if ("GET".equals(method)) {
                Subtask subtask = taskManager.getSubtaskById(subtaskId);
                if (subtask == null) {
                    sendNotFound(exchange, "Subtask not found");
                } else {
                    sendText(exchange, gson.toJson(subtask), 200);
                }
            } else if ("DELETE".equals(method)) {
                Subtask subtask = taskManager.getSubtaskById(subtaskId);
                if (subtask == null) {
                    sendNotFound(exchange, "Subtask not found");
                } else {
                    taskManager.deleteSubtaskById(subtaskId);
                    sendText(exchange, "{}", 200);
                }
            } else {
                sendText(exchange, "{\"error\": \"Method not allowed\"}", 405);
            }
        } catch (NumberFormatException e) {
            sendNotFound(exchange, "Invalid subtask ID");
        }
    }
}
