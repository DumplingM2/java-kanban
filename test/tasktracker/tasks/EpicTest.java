package tasktracker.tasks;

import org.junit.jupiter.api.Test;
import tasktracker.status.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    @Test
    void epicEquality() {
        Epic epic1 = new Epic("Epic 1", "Description", 1);
        Epic epic2 = new Epic("Epic 1", "Description", 1);

        assertEquals(epic1, epic2, "Эпики с одинаковыми полями должны быть равны.");
    }

    @Test
    void shouldNotAssignSubtaskToItselfAsEpic() {
        Subtask subtask = new Subtask(
                        "Subtask",
                        "Description",
                        1,
                        TaskStatus.NEW,
                Duration.ofMinutes(60),
                LocalDateTime.now(),
                1
                );

        assertThrows(IllegalArgumentException.class, () -> {
            subtask.setEpicId(subtask.getId());  // Попытка назначить подзадачу своим же эпиком
        });
    }
}
