package tasktracker.http.handlers;

import com.sun.net.httpserver.HttpExchange;
import tasktracker.manager.TaskManager;
import tasktracker.tasks.Epic;

import java.io.IOException;

public class EpicHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public EpicHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    protected void processGet(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if ("/epics".equals(path)) {
            sendText(exchange, gson.toJson(taskManager.getAllEpics()), 200);
        } else if (path.matches("/epics/\\d+")) {
            int epicId = Integer.parseInt(path.split("/")[2]);
            Epic epic = taskManager.getEpicById(epicId);
            if (epic == null) {
                sendNotFound(exchange, "Эпик не найден");
            } else {
                sendText(exchange, gson.toJson(epic), 200);
            }
        } else if (path.matches("/epics/\\d+/subtasks")) {
            int epicId = Integer.parseInt(path.split("/")[2]);
            Epic epic = taskManager.getEpicById(epicId);
            if (epic == null) {
                sendNotFound(exchange, "Эпик не найден");
            } else {
                sendText(exchange, gson.toJson(taskManager.getSubtasksOfEpic(epicId)), 200);
            }
        } else {
            sendNotFound(exchange, "Эндпоинт не найден");
        }
    }

    @Override
    protected void processPost(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if ("/epics".equals(path)) {
            String body = readRequestBody(exchange);
            Epic epic = gson.fromJson(body, Epic.class);
            if (epic.getId() == 0) {
                taskManager.createEpic(epic);
                sendText(exchange, gson.toJson(epic), 201);
            } else {
                Epic existing = taskManager.getEpicById(epic.getId());
                if (existing == null) {
                    sendNotFound(exchange, "Эпик не найден");
                    return;
                }
                taskManager.updateEpic(epic);
                sendText(exchange, gson.toJson(epic), 200);
            }
        } else {
            sendNotFound(exchange, "Эндпоинт не найден");
        }
    }

    @Override
    protected void processDelete(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.matches("/epics/\\d+")) {
            int epicId = Integer.parseInt(path.split("/")[2]);
            Epic epic = taskManager.getEpicById(epicId);
            if (epic == null) {
                sendNotFound(exchange, "Эпик не найден");
            } else {
                taskManager.deleteEpicById(epicId);
                sendText(exchange, "{}", 200);
            }
        } else {
            sendNotFound(exchange, "Эндпоинт не найден");
        }
    }
}
