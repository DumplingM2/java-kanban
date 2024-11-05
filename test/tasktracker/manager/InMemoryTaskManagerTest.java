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
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void shouldRemoveTaskFromHistoryAfterDeletion() {
        Task task = new Task("Task 1", "Description", taskManager.generateId(), TaskStatus.NEW);
        taskManager.createTask(task);
        taskManager.getTaskById(task.getId());

        taskManager.deleteTaskById(task.getId());

        List<Task> history = taskManager.getHistory();
        assertTrue(history.isEmpty(), "История должна быть пустой после удаления задачи.");
    }

    @Test
    void shouldRemoveSubtasksFromEpicAfterDeletion() {
        Epic epic = new Epic("Epic", "Description", taskManager.generateId());
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask 1", "Description", taskManager.generateId(), TaskStatus.NEW, epic.getId());
        taskManager.createSubtask(subtask);

        taskManager.deleteSubtaskById(subtask.getId());

        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertTrue(updatedEpic.getSubtaskIds().isEmpty(), "Список подзадач в эпике должен быть пустым после удаления подзадачи.");
    }

    @Test
    void shouldNotRetainOldIdsAfterSubtaskDeletion() {
        Epic epic = new Epic("Epic", "Description", taskManager.generateId());
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Description", taskManager.generateId(), TaskStatus.NEW, epic.getId());
        taskManager.createSubtask(subtask);

        taskManager.deleteSubtaskById(subtask.getId());

        assertFalse(taskManager.getAllSubtasks().contains(subtask), "Удалённая подзадача не должна присутствовать в списке всех подзадач.");
        assertFalse(epic.getSubtaskIds().contains(subtask.getId()), "Идентификатор удалённой подзадачи не должен оставаться в эпике.");
    }

    @Test
    void shouldUpdateEpicStatusCorrectlyWhenSubtasksChange() {
        Epic epic = new Epic("Epic", "Description", taskManager.generateId());
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description", taskManager.generateId(), TaskStatus.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Description", taskManager.generateId(), TaskStatus.NEW, epic.getId());
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        subtask1.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1);

        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpicById(epic.getId()).getStatus(), "Статус эпика должен быть IN_PROGRESS после выполнения одной из подзадач.");

        subtask2.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask2);

        assertEquals(TaskStatus.DONE, taskManager.getEpicById(epic.getId()).getStatus(), "Статус эпика должен быть DONE после выполнения всех подзадач.");
    }
}
