package tasktracker.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.tasks.Task;
import tasktracker.tasks.Epic;
import tasktracker.tasks.Subtask;
import tasktracker.status.TaskStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    private File tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("test", ".csv");
    }

    @Test
    void shouldSaveAndLoadEmptyFile() {
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);
        manager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loadedManager.getAllTasks().isEmpty(), "Список задач должен быть пустым после загрузки из пустого файла.");
        assertTrue(loadedManager.getAllEpics().isEmpty(), "Список эпиков должен быть пустым после загрузки из пустого файла.");
        assertTrue(loadedManager.getAllSubtasks().isEmpty(), "Список подзадач должен быть пустым после загрузки из пустого файла.");
    }

    @Test
    void shouldSaveAndLoadMultipleTasks() {
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);

        Task task1 = new Task("Task 1", "Description 1", manager.generateId(), TaskStatus.NEW);
        Epic epic1 = new Epic("Epic 1", "Description 2", manager.generateId());
        Subtask subtask1 = new Subtask("Subtask 1", "Description 3", manager.generateId(), TaskStatus.NEW, epic1.getId());

        manager.createTask(task1);
        manager.createEpic(epic1);
        manager.createSubtask(subtask1);

        manager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        List<Task> tasks = loadedManager.getAllTasks();
        List<Epic> epics = loadedManager.getAllEpics();
        List<Subtask> subtasks = loadedManager.getAllSubtasks();

        assertEquals(1, tasks.size(), "Должна быть загружена одна задача.");
        assertEquals(1, epics.size(), "Должен быть загружен один эпик.");
        assertEquals(1, subtasks.size(), "Должна быть загружена одна подзадача.");

        assertEquals(task1, tasks.getFirst(), "Загруженная задача должна совпадать с сохранённой.");
        assertEquals(epic1, epics.getFirst(), "Загруженный эпик должен совпадать с сохранённым.");
        assertEquals(subtask1, subtasks.getFirst(), "Загруженная подзадача должна совпадать с сохранённой.");
    }

    @Test
    void shouldLoadManagerFromFileWithMultipleTasks() throws IOException {
        // Создаем и записываем несколько задач в файл
        String csvContent = String.join("\n",
                "id,type,name,status,description,epic",
                "1,TASK,Task1,NEW,Description task1,",
                "2,EPIC,Epic2,NEW,Description epic2,",
                "3,SUBTASK,Sub Task2,DONE,Description sub task3,2"
        );

        Files.writeString(tempFile.toPath(), csvContent);

        // Загружаем менеджер из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        // Проверяем загруженные данные
        Task loadedTask = loadedManager.getTaskById(1);
        Epic loadedEpic = loadedManager.getEpicById(2);
        Subtask loadedSubtask = loadedManager.getSubtaskById(3);

        assertNotNull(loadedTask, "Задача должна быть загружена.");
        assertNotNull(loadedEpic, "Эпик должен быть загружен.");
        assertNotNull(loadedSubtask, "Подзадача должна быть загружена.");

        assertEquals("Task1", loadedTask.getTitle());
        assertEquals(TaskStatus.NEW, loadedTask.getStatus());

        assertEquals("Epic2", loadedEpic.getTitle());
        assertEquals(TaskStatus.DONE, loadedEpic.getStatus());

        assertEquals("Sub Task2", loadedSubtask.getTitle());
        assertEquals(TaskStatus.DONE, loadedSubtask.getStatus());
        assertEquals(2, loadedSubtask.getEpicId(), "ID эпика у подзадачи должен совпадать.");
    }
}
