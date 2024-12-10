package tasktracker.http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tasktracker.manager.TaskManager;
import tasktracker.tasks.Epic;

import java.io.IOException;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public EpicHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.gson = tasktracker.http.HttpTaskServer.getGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("/epics".equals(path)) {
                handleEpics(exchange, method);
            } else if (path.matches("/epics/\\d+/subtasks")) {
                handleEpicSubtasks(exchange, method, path);
            } else if (path.matches("/epics/\\d+")) {
                handleEpicById(exchange, method, path);
            } else {
                sendNotFound(exchange, "Endpoint not found");
            }
        } catch (IllegalArgumentException e) {
            // Если задача/эпик пересекается — 406
            sendHasInteractions(exchange, e.getMessage());
        } catch (Exception e) {
            sendText(exchange, "{\"error\": \"" + e.getMessage() + "\"}", 500);
        }
    }

    private void handleEpics(HttpExchange exchange, String method) throws IOException {
        if ("GET".equals(method)) {
            sendText(exchange, gson.toJson(taskManager.getAllEpics()), 200);
        } else if ("POST".equals(method)) {
            String body = readRequestBody(exchange);
            Epic epic = gson.fromJson(body, Epic.class);
            if (epic.getId() == 0) {
                taskManager.createEpic(epic);
                sendText(exchange, gson.toJson(epic), 201);
            } else {
                Epic existing = taskManager.getEpicById(epic.getId());
                if (existing == null) {
                    sendNotFound(exchange, "Epic not found");
                    return;
                }
                taskManager.updateEpic(epic);
                sendText(exchange, gson.toJson(epic), 200);
            }
        } else {
            sendText(exchange, "{\"error\": \"Method not allowed\"}", 405);
        }
    }

    private void handleEpicById(HttpExchange exchange, String method, String path) throws IOException {
        try {
            int epicId = Integer.parseInt(path.split("/")[2]);
            if ("GET".equals(method)) {
                Epic epic = taskManager.getEpicById(epicId);
                if (epic == null) {
                    sendNotFound(exchange, "Epic not found");
                } else {
                    sendText(exchange, gson.toJson(epic), 200);
                }
            } else if ("DELETE".equals(method)) {
                Epic epic = taskManager.getEpicById(epicId);
                if (epic == null) {
                    sendNotFound(exchange, "Epic not found");
                } else {
                    taskManager.deleteEpicById(epicId);
                    sendText(exchange, "{}", 200);
                }
            } else {
                sendText(exchange, "{\"error\": \"Method not allowed\"}", 405);
            }
        } catch (NumberFormatException e) {
            sendNotFound(exchange, "Invalid epic ID");
        }
    }

    private void handleEpicSubtasks(HttpExchange exchange, String method, String path) throws IOException {
        if ("GET".equals(method)) {
            // GET /epics/{id}/subtasks
            int epicId = Integer.parseInt(path.split("/")[2]);
            Epic epic = taskManager.getEpicById(epicId);
            if (epic == null) {
                sendNotFound(exchange, "Epic not found");
            } else {
                sendText(exchange, gson.toJson(taskManager.getSubtasksOfEpic(epicId)), 200);
            }
        } else {
            sendText(exchange, "{\"error\": \"Method not allowed\"}", 405);
        }
    }
}
