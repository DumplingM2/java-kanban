package tasktracker.tasks;

import org.junit.jupiter.api.Test;
import tasktracker.status.TaskStatus;  // Добавляем правильный импорт для TaskStatus

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    @Test
    void epicEquality() {
        Epic epic1 = new Epic("Epic 1", "Description", 1);
        Epic epic2 = new Epic("Epic 2", "Description", 1);

        assertEquals(epic1, epic2, "Эпики с одинаковым id должны быть равны.");
    }

    @Test
    void shouldNotAssignSubtaskToItselfAsEpic() {
        Subtask subtask = new Subtask("Subtask", "Description", 1, TaskStatus.NEW, 1);  // TaskStatus.NEW

        assertThrows(IllegalArgumentException.class, () -> {
            subtask.setEpicId(subtask.getId());  // Попытка назначить подзадачу своим же эпиком
        });
    }
}
