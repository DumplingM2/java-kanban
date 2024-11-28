package tasktracker.tasks;

import tasktracker.status.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private int epicId; // ID эпика, к которому относится подзадача

    // Конструктор с новыми полями
    public Subtask(String title, String description, int id, TaskStatus status, Duration duration, LocalDateTime startTime, int epicId) {
        super(title, description, id, status, duration, startTime);
        this.epicId = epicId;
    }

    // Геттеры и сеттеры
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subtask)) return false;
        if (!super.equals(o)) return false;
        Subtask subtask = (Subtask) o;
        return epicId == subtask.epicId;
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + epicId;
    }
}
