package tasktracker.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.status.TaskStatus;
import tasktracker.tasks.Epic;
import tasktracker.tasks.Subtask;
import tasktracker.tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    protected T taskManager;

    abstract T createTaskManager(); // Метод, который должен быть реализован в наследниках для создания TaskManager

    @BeforeEach
    void setUp() {
        taskManager = createTaskManager(); // Инициализация TaskManager перед каждым тестом
    }

    @Test
    void shouldCalculateEpicStatusWhenAllSubtasksNew() {
        Epic epic = new Epic("Epic 1", "Description", taskManager.generateId());
        taskManager.createEpic(epic);

        LocalDateTime fixedTime = LocalDateTime.of(2023, 1, 1, 10, 0);
        Subtask subtask1 = new Subtask("Subtask 1", "Description", taskManager.generateId(), TaskStatus.NEW,
                Duration.ofMinutes(30), fixedTime, epic.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Description", taskManager.generateId(), TaskStatus.NEW,
                Duration.ofMinutes(30), fixedTime.plusMinutes(40), epic.getId());

        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        assertEquals(TaskStatus.NEW, epic.getStatus(), "Статус эпика должен быть NEW, если все подзадачи NEW.");
        assertEquals(Duration.ofMinutes(60), epic.getDuration(), "Длительность эпика должна быть суммой длительностей подзадач.");
    }

    @Test
    void shouldCalculateEpicStatusWhenAllSubtasksDone() {
        Epic epic = new Epic("Epic 1", "Description", taskManager.generateId());
        taskManager.createEpic(epic);

        LocalDateTime fixedTime = LocalDateTime.of(2023, 1, 1, 10, 0);
        Subtask subtask1 = new Subtask("Subtask 1", "Description", taskManager.generateId(), TaskStatus.DONE,
                Duration.ofMinutes(30), fixedTime, epic.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Description", taskManager.generateId(), TaskStatus.DONE,
                Duration.ofMinutes(30), fixedTime.plusMinutes(40), epic.getId());

        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        assertEquals(TaskStatus.DONE, epic.getStatus(), "Статус эпика должен быть DONE, если все подзадачи DONE.");
        assertEquals(Duration.ofMinutes(60), epic.getDuration(), "Длительность эпика должна быть суммой длительностей подзадач.");
    }

    @Test
    void shouldNotAllowOverlappingTasks() {
        LocalDateTime fixedTime = LocalDateTime.of(2023, 1, 1, 10, 0);
        Task task1 = new Task("Task 1", "Description", taskManager.generateId(), TaskStatus.NEW,
                Duration.ofMinutes(30), fixedTime);
        Task task2 = new Task("Task 2", "Description", taskManager.generateId(), TaskStatus.NEW,
                Duration.ofMinutes(30), fixedTime.plusMinutes(15));

        taskManager.createTask(task1);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> taskManager.createTask(task2));

        assertEquals("Задача пересекается с другой задачей по времени выполнения.", exception.getMessage());
    }

    @Test
    void shouldAllowNonOverlappingTasks() {
        LocalDateTime fixedTime = LocalDateTime.of(2023, 1, 1, 10, 0);
        Task task1 = new Task("Task 1", "Description", taskManager.generateId(), TaskStatus.NEW,
                Duration.ofMinutes(30), fixedTime);
        Task task2 = new Task("Task 2", "Description", taskManager.generateId(), TaskStatus.NEW,
                Duration.ofMinutes(30), fixedTime.plusMinutes(40));

        taskManager.createTask(task1);
        assertDoesNotThrow(() -> taskManager.createTask(task2), "Не должно быть исключений для непересекающихся задач.");
    }

    @Test
    void shouldCalculateEpicStatusWithoutSubtasks() {
        Epic epic = new Epic("Epic 1", "Description", taskManager.generateId());
        taskManager.createEpic(epic);

        assertEquals(TaskStatus.NEW, epic.getStatus(), "Статус эпика без подзадач должен быть NEW.");
    }

    @Test
    void shouldRemoveSubtaskFromEpic() {
        Epic epic = new Epic("Epic 1", "Description", taskManager.generateId());
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description", taskManager.generateId(), TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2023, 1, 1, 10, 0), epic.getId());
        taskManager.createSubtask(subtask1);

        taskManager.deleteSubtaskById(subtask1.getId());
        assertTrue(epic.getSubtaskIds().isEmpty(), "После удаления подзадачи она должна быть удалена из эпика.");
    }

    @Test
    void shouldPrioritizeTasksCorrectly() {
        LocalDateTime fixedTime = LocalDateTime.of(2023, 1, 1, 10, 0);
        Task task1 = new Task("Task 1", "Description", taskManager.generateId(), TaskStatus.NEW,
                Duration.ofMinutes(30), fixedTime);
        Task task2 = new Task("Task 2", "Description", taskManager.generateId(), TaskStatus.NEW,
                Duration.ofMinutes(30), fixedTime.plusMinutes(40));

        taskManager.createTask(task2);
        taskManager.createTask(task1);

        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertEquals(task1, prioritizedTasks.get(0), "Первая задача должна быть Task 1.");
        assertEquals(task2, prioritizedTasks.get(1), "Вторая задача должна быть Task 2.");
    }
}
