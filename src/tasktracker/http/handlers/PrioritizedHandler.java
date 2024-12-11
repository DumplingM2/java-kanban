package tasktracker.http.handlers;

import com.sun.net.httpserver.HttpExchange;
import tasktracker.manager.TaskManager;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public PrioritizedHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    protected void processGet(HttpExchange exchange) throws IOException {
        sendText(exchange, gson.toJson(taskManager.getPrioritizedTasks()), 200);
    }
    // POST и DELETE не переопределяем, они вернут 405 по умолчанию.
}
