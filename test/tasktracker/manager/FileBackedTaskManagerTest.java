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
import java.time.Duration;
import java.time.LocalDateTime;
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
        manager.saveToFile();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loadedManager.getAllTasks().isEmpty(), "Список задач должен быть пустым после загрузки из пустого файла.");
        assertTrue(loadedManager.getAllEpics().isEmpty(), "Список эпиков должен быть пустым после загрузки из пустого файла.");
        assertTrue(loadedManager.getAllSubtasks().isEmpty(), "Список подзадач должен быть пустым после загрузки из пустого файла.");
    }

    @Test
    void shouldSaveAndLoadMultipleTasks() {
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);

        Task task1 = new Task(
                "Task 1",
                "Description 1",
                manager.generateId(),
                TaskStatus.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2023, 1, 1, 10, 0)
        );
        Epic epic1 = new Epic("Epic 1", "Description 2", manager.generateId());
        Subtask subtask1 = new Subtask(
                "Subtask 1",
                "Description 3",
                manager.generateId(),
                TaskStatus.NEW,
                Duration.ofMinutes(20),
                LocalDateTime.of(2023, 1, 1, 11, 0), // Начало после Task 1
                epic1.getId()
        );

        manager.createTask(task1);
        manager.createEpic(epic1);
        manager.createSubtask(subtask1);

        manager.saveToFile();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        List<Task> tasks = loadedManager.getAllTasks();
        List<Epic> epics = loadedManager.getAllEpics();
        List<Subtask> subtasks = loadedManager.getAllSubtasks();

        assertEquals(1, tasks.size(), "Должна быть загружена одна задача.");
        assertEquals(1, epics.size(), "Должен быть загружен один эпик.");
        assertEquals(1, subtasks.size(), "Должна быть загружена одна подзадача.");

        assertEquals(task1, tasks.get(0), "Загруженная задача должна совпадать с сохранённой.");
        assertEquals(epic1, epics.get(0), "Загруженный эпик должен совпадать с сохранённым.");
        assertEquals(subtask1, subtasks.get(0), "Загруженная подзадача должна совпадать с сохранённой.");

        assertEquals(Duration.ofMinutes(20), epics.get(0).getDuration(), "Длительность эпика должна быть рассчитана.");
        assertEquals(LocalDateTime.of(2023, 1, 1, 11, 0), epics.get(0).getStartTime(), "Время начала эпика должно быть рассчитано.");
    }

    @Test
    void shouldLoadManagerFromFileWithMultipleTasks() throws IOException {
        String csvContent = String.join("\n",
                "id,type,name,status,description,duration,startTime,epic",
                "1,TASK,Task 1,NEW,Description of task 1,30,2023-01-01T10:00,",
                "2,TASK,Task 2,IN_PROGRESS,Description of task 2,45,2023-01-01T12:30,",
                "3,EPIC,Epic 1,IN_PROGRESS,Description of epic 1,,,,",
                "4,SUBTASK,Subtask 1,NEW,Description of subtask 1,20,2023-01-01T11:00,3",
                "5,SUBTASK,Subtask 2,DONE,Description of subtask 2,25,2023-01-01T11:30,3"
        );

        Files.writeString(tempFile.toPath(), csvContent);;

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        Task loadedTask1 = loadedManager.getTaskById(1);
        Task loadedTask2 = loadedManager.getTaskById(2);
        Epic loadedEpic = loadedManager.getEpicById(3);
        Subtask loadedSubtask1 = loadedManager.getSubtaskById(4);
        Subtask loadedSubtask2 = loadedManager.getSubtaskById(5);

        // Проверяем, что задачи, эпики и подзадачи были загружены
        assertNotNull(loadedTask1, "Task 1 должен быть загружен.");
        assertNotNull(loadedTask2, "Task 2 должен быть загружен.");
        assertNotNull(loadedEpic, "Epic должен быть загружен.");
        assertNotNull(loadedSubtask1, "Subtask 1 должен быть загружен.");
        assertNotNull(loadedSubtask2, "Subtask 2 должен быть загружен.");

        // Проверяем, что значения совпадают с ожидаемыми
        assertEquals("Task 1", loadedTask1.getTitle(), "Название Task 1 должно совпадать.");
        assertEquals(TaskStatus.NEW, loadedTask1.getStatus(), "Статус Task 1 должен быть NEW.");
        assertEquals("Description of task 1", loadedTask1.getDescription(), "Описание Task 1 должно совпадать.");

        assertEquals("Task 2", loadedTask2.getTitle(), "Название Task 2 должно совпадать.");
        assertEquals(TaskStatus.IN_PROGRESS, loadedTask2.getStatus(), "Статус Task 2 должен быть IN_PROGRESS.");
        assertEquals("Description of task 2", loadedTask2.getDescription(), "Описание Task 2 должно совпадать.");

        assertEquals("Epic 1", loadedEpic.getTitle(), "Название Epic должно совпадать.");
        assertEquals(TaskStatus.IN_PROGRESS, loadedEpic.getStatus(), "Статус Epic должен быть IN_PROGRESS.");
        assertEquals("Description of epic 1", loadedEpic.getDescription(), "Описание Epic должно совпадать.");

        assertEquals("Subtask 1", loadedSubtask1.getTitle(), "Название Subtask 1 должно совпадать.");
        assertEquals(TaskStatus.NEW, loadedSubtask1.getStatus(), "Статус Subtask 1 должен быть NEW.");
        assertEquals("Description of subtask 1", loadedSubtask1.getDescription(), "Описание Subtask 1 должно совпадать.");
        assertEquals(3, loadedSubtask1.getEpicId(), "EpicId для Subtask 1 должен быть равен 3.");

        assertEquals("Subtask 2", loadedSubtask2.getTitle(), "Название Subtask 2 должно совпадать.");
        assertEquals(TaskStatus.DONE, loadedSubtask2.getStatus(), "Статус Subtask 2 должен быть DONE.");
        assertEquals("Description of subtask 2", loadedSubtask2.getDescription(), "Описание Subtask 2 должно совпадать.");
        assertEquals(3, loadedSubtask2.getEpicId(), "EpicId для Subtask 2 должен быть равен 3.");
    }


    @Test
    void shouldHandleCorruptedFile() throws IOException {
        String corruptedContent = "id,type,name,status,description,epic,startTime,duration\n1,BROKEN DATA";

        Files.writeString(tempFile.toPath(), corruptedContent);

        assertThrows(RuntimeException.class, () -> FileBackedTaskManager.loadFromFile(tempFile),
                "Загрузка из повреждённого файла должна вызывать исключение.");
    }
}
