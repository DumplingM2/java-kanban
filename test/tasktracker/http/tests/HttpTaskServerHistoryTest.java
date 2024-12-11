package tasktracker.http.tests;

import org.junit.jupiter.api.Test;
import tasktracker.tasks.Task;
import tasktracker.status.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerHistoryTest extends BaseHttpTest {

    @Test
    public void testGetHistory() throws IOException, InterruptedException {
        Task task1 = new Task("Test Task 1", "Description 1", manager.generateId(), TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());

        manager.createTask(task1);
        manager.getTaskById(task1.getId());

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Некорректный статус ответа при получении истории");
        Task[] history = gson.fromJson(response.body(), Task[].class);
        assertEquals(1, history.length, "Некорректное количество задач в истории");
        assertEquals(task1, history[0], "Задача в истории не совпадает с ожидаемой");
    }
}
