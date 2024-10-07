package tasktracker.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.tasks.Epic;
import tasktracker.tasks.Subtask;
import tasktracker.tasks.Task;
import tasktracker.status.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerTest {

    private HistoryManager historyManager;
    private TaskManager taskManager;  // Добавляем taskManager

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();  // Инициализация менеджера истории
        taskManager = Managers.getDefault();  // Инициализация менеджера задач
    }

    @Test
    void shouldAddTaskToHistory() {
        Task task = new Task("Test Task", "Description", 1, TaskStatus.NEW);

        historyManager.add(task);
        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История не должна быть пустой.");
        assertEquals(1, history.size(), "История должна содержать одну задачу.");
        assertEquals(task, history.get(0), "Задача в истории не совпадает с оригиналом.");
    }

    @Test
    void shouldLimitHistoryToTenTasks() {
        for (int i = 1; i <= 11; i++) {
            Task task = new Task("Task " + i, "Description", taskManager.generateId(), TaskStatus.NEW);
            taskManager.createTask(task);
            taskManager.getTaskById(task.getId());  // Добавляем задачу в историю
        }

        List<Task> history = taskManager.getHistory();
        assertEquals(10, history.size(), "История должна содержать только 10 задач.");
        assertEquals("Task 2", history.get(0).getTitle(), "Самая старая задача должна быть удалена из истории.");
    }

    @Test
    void shouldUpdateTask() {
        Task task = new Task("Test Task", "Description", taskManager.generateId(), TaskStatus.NEW);
        taskManager.createTask(task);

        task.setTitle("Updated Task");
        task.setDescription("Updated Description");
        task.setStatus(TaskStatus.DONE);
        taskManager.updateTask(task);

        Task updatedTask = taskManager.getTaskById(task.getId());
        assertEquals("Updated Task", updatedTask.getTitle(), "Название задачи должно быть обновлено.");
        assertEquals("Updated Description", updatedTask.getDescription(), "Описание задачи должно быть обновлено.");
        assertEquals(TaskStatus.DONE, updatedTask.getStatus(), "Статус задачи должен быть обновлен.");
    }

    @Test
    void shouldUpdateEpicStatusWhenSubtasksChange() {
        Epic epic = new Epic("Epic", "Description", taskManager.generateId());
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description", taskManager.generateId(), TaskStatus.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Description", taskManager.generateId(), TaskStatus.NEW, epic.getId());
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        subtask1.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1);

        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpicById(epic.getId()).getStatus(), "Статус эпика должен быть IN_PROGRESS.");

        subtask2.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask2);

        assertEquals(TaskStatus.DONE, taskManager.getEpicById(epic.getId()).getStatus(), "Статус эпика должен быть DONE.");
    }
}
