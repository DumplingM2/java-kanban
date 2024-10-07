package tasktracker.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.tasks.Task;
import tasktracker.status.TaskStatus;
import tasktracker.tasks.Epic;
import tasktracker.tasks.Subtask;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private InMemoryTaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();  // Инициализация перед каждым тестом
    }

    @Test
    void addNewTask() {
        Task task = new Task("Test Task", "Description", taskManager.generateId(), TaskStatus.NEW);
        taskManager.createTask(task);

        Task savedTask = taskManager.getTaskById(task.getId());

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        List<Task> tasks = taskManager.getAllTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void shouldNotAddEpicAsSubtaskToItself() {
        Epic epic = new Epic("Epic", "Description", taskManager.generateId());
        taskManager.createEpic(epic);

        assertThrows(IllegalArgumentException.class, () -> {
            epic.addSubtask(epic.getId());  // Попытка добавить эпик в самого себя как подзадачу
        });
    }

    @Test
    void shouldInitializeManagersCorrectly() {
        TaskManager taskManager = Managers.getDefault();
        assertNotNull(taskManager, "TaskManager должен быть проинициализирован.");

        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "HistoryManager должен быть проинициализирован.");
    }

    @Test
    void taskShouldRemainUnchangedAfterAddition() {
        Task task = new Task("Test Task", "Description", taskManager.generateId(), TaskStatus.NEW);
        taskManager.createTask(task);

        Task savedTask = taskManager.getTaskById(task.getId());

        assertEquals(task.getTitle(), savedTask.getTitle(), "Название задачи не должно измениться.");
        assertEquals(task.getDescription(), savedTask.getDescription(), "Описание задачи не должно измениться.");
        assertEquals(task.getStatus(), savedTask.getStatus(), "Статус задачи не должен измениться.");
    }

    @Test
    void shouldDeleteTaskById() {
        Task task = new Task("Test Task", "Description", taskManager.generateId(), TaskStatus.NEW);
        taskManager.createTask(task);
        taskManager.deleteTaskById(task.getId());

        assertNull(taskManager.getTaskById(task.getId()), "Задача не должна существовать после удаления.");
    }

    @Test
    void shouldDeleteEpicAndSubtasksById() {
        Epic epic = new Epic("Epic", "Description", taskManager.generateId());
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description", taskManager.generateId(), TaskStatus.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Description", taskManager.generateId(), TaskStatus.NEW, epic.getId());
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        taskManager.deleteEpicById(epic.getId());

        assertNull(taskManager.getEpicById(epic.getId()), "Эпик не должен существовать после удаления.");
        assertNull(taskManager.getSubtaskById(subtask1.getId()), "Подзадачи эпика должны удаляться вместе с ним.");
        assertNull(taskManager.getSubtaskById(subtask2.getId()), "Подзадачи эпика должны удаляться вместе с ним.");
    }

    @Test
    void shouldNotThrowErrorWhenDeletingNonExistentTask() {
        assertDoesNotThrow(() -> taskManager.deleteTaskById(999), "Удаление несуществующей задачи не должно вызывать ошибки.");
    }

}
