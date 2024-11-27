package tasktracker.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.tasks.Task;
import tasktracker.status.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void shouldHandleEmptyHistory() {
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty(), "История должна быть пустой, если задачи не добавлялись.");
    }

    @Test
    void shouldAddTaskToHistoryAndKeepOrder() {
        Task task1 = new Task("Task 1", "Description 1", 1, TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        Task task2 = new Task("Task 2", "Description 2", 2, TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now().plusMinutes(40));

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1); // Повторный просмотр

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "История должна содержать две уникальные задачи.");
        assertEquals(task2, history.get(0), "Последняя добавленная задача должна быть первой.");
        assertEquals(task1, history.get(1), "Первая добавленная задача должна быть последней после повторного добавления.");
    }

    @Test
    void shouldNotAllowDuplicatesInHistory() {
        Task task = new Task("Task", "Description", 1, TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());

        historyManager.add(task);
        historyManager.add(task); // Повторное добавление

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "История не должна содержать дубликаты.");
        assertEquals(task, history.get(0), "Задача должна присутствовать в истории только один раз.");
    }

    @Test
    void shouldRemoveTaskFromHistoryStart() {
        Task task1 = new Task("Task 1", "Description 1", 1, TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        Task task2 = new Task("Task 2", "Description 2", 2, TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now().plusMinutes(40));
        Task task3 = new Task("Task 3", "Description 3", 3, TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now().plusMinutes(80));

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task1.getId()); // Удаление первой задачи

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "История должна содержать две задачи после удаления.");
        assertFalse(history.contains(task1), "Первая задача должна быть удалена из истории.");
    }

    @Test
    void shouldRemoveTaskFromHistoryMiddle() {
        Task task1 = new Task("Task 1", "Description 1", 1, TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        Task task2 = new Task("Task 2", "Description 2", 2, TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now().plusMinutes(40));
        Task task3 = new Task("Task 3", "Description 3", 3, TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now().plusMinutes(80));

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task2.getId()); // Удаление второй задачи

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "История должна содержать две задачи после удаления.");
        assertFalse(history.contains(task2), "Средняя задача должна быть удалена из истории.");
        assertEquals(task1, history.get(0), "Первая задача должна остаться на месте.");
        assertEquals(task3, history.get(1), "Третья задача должна остаться на месте.");
    }

    @Test
    void shouldRemoveTaskCorrectlyFromHistoryWhenOnlyOneExists() {
        Task task = new Task("Task", "Description", 1, TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());

        historyManager.add(task);
        historyManager.remove(task.getId()); // Удаление единственной задачи

        List<Task> history = historyManager.getHistory();

        assertTrue(history.isEmpty(), "История должна быть пустой после удаления единственной задачи.");
    }
}
