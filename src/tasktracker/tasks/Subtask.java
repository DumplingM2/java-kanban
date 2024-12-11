package tasktracker.tasks;

import tasktracker.status.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String title, String description, int id, TaskStatus status, Duration duration, LocalDateTime startTime, int epicId) {
        super(title, description, id, status, duration, startTime);
        this.epicId = epicId;
    }

    // Добавляем сеттер для id
    public void setId(int id) {
        super.setId(id);
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        if (this.getId() == epicId) {
            throw new IllegalArgumentException("Подзадача не может быть своим эпиком.");
        }
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return String.join(",", String.valueOf(getId()), "SUBTASK", getTitle(), getStatus().toString(),
                getDescription(), String.valueOf(getDuration().toMinutes()),
                getStartTime() != null ? getStartTime().toString() : "", String.valueOf(epicId));
    }
}
