package tasktracker.http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tasktracker.manager.TaskManager;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public PrioritizedHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.gson = tasktracker.http.HttpTaskServer.getGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            if ("GET".equals(method)) {
                sendText(exchange, gson.toJson(taskManager.getPrioritizedTasks()), 200);
            } else {
                sendText(exchange, "{\"error\": \"Method not allowed\"}", 405);
            }
        } catch (Exception e) {
            sendText(exchange, "{\"error\": \"" + e.getMessage() + "\"}", 500);
        }
    }
}
