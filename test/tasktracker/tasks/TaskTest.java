package tasktracker.tasks;

import org.junit.jupiter.api.Test;
import tasktracker.status.TaskStatus;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void taskEquality() {
        Task task1 = new Task("Task 1", "Description", 1, TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Description", 1, TaskStatus.NEW);

        assertEquals(task1, task2, "Задачи с одинаковым id должны быть равны.");
    }
}
