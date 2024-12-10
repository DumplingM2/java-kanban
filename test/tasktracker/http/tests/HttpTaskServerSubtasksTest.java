package tasktracker.http.tests;

import org.junit.jupiter.api.Test;
import tasktracker.tasks.Epic;
import tasktracker.tasks.Subtask;
import tasktracker.status.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerSubtasksTest extends BaseHttpTest {

    @Test
    public void testAddSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Epic Description", manager.generateId());
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Subtask Description", 0, TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now(), epic.getId());
        String subtaskJson = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Некорректный статус ответа при добавлении подзадачи");
        assertEquals(1, manager.getAllSubtasks().size(), "Подзадача не добавлена в менеджер");
    }
}
