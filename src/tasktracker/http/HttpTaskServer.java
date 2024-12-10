package tasktracker.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import tasktracker.http.adapters.DurationAdapter;
import tasktracker.http.adapters.LocalDateTimeAdapter;
import tasktracker.http.handlers.*;
import tasktracker.manager.Managers;
import tasktracker.manager.TaskManager;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private final HttpServer server;
    private static Gson gson;

    public HttpTaskServer(TaskManager manager) throws IOException {
        server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/tasks", new TaskHandler(manager));
        server.createContext("/subtasks", new SubtaskHandler(manager));
        server.createContext("/epics", new EpicHandler(manager));
        server.createContext("/history", new HistoryHandler(manager));
        server.createContext("/prioritized", new PrioritizedHandler(manager));

        gson = new GsonBuilder()
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();

        System.out.println("HTTP server started on port 8080...");
    }

    public static Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder()
                    .registerTypeAdapter(Duration.class, new tasktracker.http.adapters.DurationAdapter())
                    .registerTypeAdapter(LocalDateTime.class, new tasktracker.http.adapters.LocalDateTimeAdapter())
                    .create();
        }
        return gson;
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
