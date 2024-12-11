package tasktracker.http.tests;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.manager.InMemoryTaskManager;
import tasktracker.manager.TaskManager;
import tasktracker.http.HttpTaskServer;
import tasktracker.status.TaskStatus;
import tasktracker.tasks.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerTasksTest {
    TaskManager manager;
    HttpTaskServer taskServer;
    Gson gson;

    public HttpTaskServerTasksTest() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        gson = HttpTaskServer.getGson();
    }

    @BeforeEach
    public void setUp() {
        manager.deleteAllTasks();
        manager.deleteAllSubtasks();
        manager.deleteAllEpics();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testAddTask() throws IOException, InterruptedException {
        // Создаём новую задачу с id = 0
        Task task = new Task(
                "Test 2",
                "Testing task 2",
                0,
                TaskStatus.NEW,
                Duration.ofMinutes(5),
                LocalDateTime.now()
        );

        String taskJson = gson.toJson(task);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Создание новой задачи — 201
        assertEquals(201, response.statusCode(), "Ожидался код ответа 201 при успешном создании задачи");
        List<Task> tasksFromManager = manager.getAllTasks();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.getFirst().getTitle(), "Некорректное имя задачи");
    }

    @Test
    public void testGetNonExistingTask() throws IOException, InterruptedException {
        // Пытаемся получить несуществующую задачу по id = 999
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Ожидаем 404, так как задачи с таким ID нет
        assertEquals(404, response.statusCode(), "Ожидался код ответа 404 при запросе несуществующей задачи");
    }

    @Test
    public void testCreateOverlappingTask() throws IOException, InterruptedException {
        // Создаём первую задачу
        Task task1 = new Task(
                "Task 1",
                "First task",
                0,
                TaskStatus.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.now()
        );
        manager.createTask(task1);

        // Пытаемся создать вторую задачу с пересечением по времени
        Task task2 = new Task(
                "Task 2",
                "Overlapping task",
                0,
                TaskStatus.NEW,
                Duration.ofMinutes(30),
                task1.getStartTime().plusMinutes(15) // Пересекается со временем task1
        );

        String task2Json = gson.toJson(task2);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(task2Json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Ожидаем 406, так как задача пересекается
        assertEquals(406, response.statusCode(), "Ожидался код ответа 406 при попытке создать пересекающуюся задачу");
    }

    @Test
    public void testDeleteTask() throws IOException, InterruptedException {
        // Создаём задачу
        Task task = new Task(
                "Task to delete",
                "Will be deleted",
                0,
                TaskStatus.NEW,
                Duration.ofMinutes(10),
                LocalDateTime.now()
        );
        manager.createTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + task.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Ожидаем 200 при успешном удалении
        assertEquals(200, response.statusCode(), "Ожидался код 200 при удалении существующей задачи");
        assertTrue(manager.getAllTasks().isEmpty(), "Задача не удалена из менеджера");
    }

    @Test
    public void testDeleteNonExistingTask() throws IOException, InterruptedException {
        // Пытаемся удалить несуществующую задачу
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Ожидаем 404, так как задачи с таким ID нет
        assertEquals(404, response.statusCode(), "Ожидался код 404 при удалении несуществующей задачи");
    }

    @Test
    public void testInvalidJson() throws IOException, InterruptedException {
        // Отправляем некорректный JSON
        String invalidJson = "{invalid json}";
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Ожидаем 500, так как JSON некорректен и вызовет ошибку парсинга
        assertEquals(500, response.statusCode(), "Ожидался код 500 при отправке некорректного JSON");
    }
}