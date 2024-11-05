package tasktracker.tasks;

import org.junit.jupiter.api.Test;
import tasktracker.manager.InMemoryTaskManager;
import tasktracker.status.TaskStatus;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void shouldNotAffectManagerDataWhenTaskFieldsAreUpdated() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        Task task = new Task("Initial Title", "Initial Description", taskManager.generateId(), TaskStatus.NEW);
        taskManager.createTask(task);

        task.setTitle("Updated Title");
        task.setDescription("Updated Description");
        task.setStatus(TaskStatus.DONE);

        Task taskFromManager = taskManager.getTaskById(task.getId());

        assertEquals("Updated Title", taskFromManager.getTitle(), "Название задачи в менеджере должно обновиться.");
        assertEquals("Updated Description", taskFromManager.getDescription(), "Описание задачи в менеджере должно обновиться.");
        assertEquals(TaskStatus.DONE, taskFromManager.getStatus(), "Статус задачи в менеджере должен обновиться.");
    }
}