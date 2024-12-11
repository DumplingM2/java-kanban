package tasktracker.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import tasktracker.http.handlers.*;
import tasktracker.manager.Managers;
import tasktracker.manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private final HttpServer server;

    public HttpTaskServer(TaskManager manager) throws IOException {
        server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/tasks", new TaskHandler(manager));
        server.createContext("/subtasks", new SubtaskHandler(manager));
        server.createContext("/epics", new EpicHandler(manager));
        server.createContext("/history", new HistoryHandler(manager));
        server.createContext("/prioritized", new PrioritizedHandler(manager));

        System.out.println("HTTP сервер запущен на порту 8080...");
    }

    public static Gson getGson() {
        // Теперь получаем Gson из Managers
        return Managers.getGson();
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    public static void main(String[] args) throws IOException {
        TaskManager taskManager = Managers.getDefault();
        HttpTaskServer server = new HttpTaskServer(taskManager);
        server.start();
    }
}
