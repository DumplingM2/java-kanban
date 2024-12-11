package tasktracker.http.handlers;

import com.sun.net.httpserver.HttpExchange;
import tasktracker.manager.TaskManager;
import tasktracker.tasks.Subtask;

import java.io.IOException;

public class SubtaskHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public SubtaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    protected void processGet(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if ("/subtasks".equals(path)) {
            sendText(exchange, gson.toJson(taskManager.getAllSubtasks()), 200);
        } else if (path.matches("/subtasks/\\d+")) {
            int subtaskId = Integer.parseInt(path.split("/")[2]);
            Subtask subtask = taskManager.getSubtaskById(subtaskId);
            if (subtask == null) {
                sendNotFound(exchange, "Подзадача не найдена");
            } else {
                sendText(exchange, gson.toJson(subtask), 200);
            }
        } else {
            sendNotFound(exchange, "Эндпоинт не найден");
        }
    }

    @Override
    protected void processPost(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if ("/subtasks".equals(path)) {
            String body = readRequestBody(exchange);
            Subtask subtask = gson.fromJson(body, Subtask.class);
            if (subtask.getId() == 0) {
                taskManager.createSubtask(subtask);
                sendText(exchange, gson.toJson(subtask), 201);
            } else {
                Subtask existing = taskManager.getSubtaskById(subtask.getId());
                if (existing == null) {
                    sendNotFound(exchange, "Subtask not found");
                    return;
                }
                taskManager.updateSubtask(subtask);
                sendText(exchange, gson.toJson(subtask), 200);
            }
        } else {
            sendNotFound(exchange, "Endpoint not found");
        }
    }

    @Override
    protected void processDelete(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.matches("/subtasks/\\d+")) {
            int subtaskId = Integer.parseInt(path.split("/")[2]);
            Subtask subtask = taskManager.getSubtaskById(subtaskId);
            if (subtask == null) {
                sendNotFound(exchange, "Subtask not found");
            } else {
                taskManager.deleteSubtaskById(subtaskId);
                sendText(exchange, "{}", 200);
            }
        } else {
            sendNotFound(exchange, "Endpoint not found");
        }
    }
}
