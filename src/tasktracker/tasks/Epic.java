package tasktracker.tasks;

import tasktracker.status.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>(); // Список ID подзадач

    public Epic(String title, String description, int id) {
        super(title, description, id, TaskStatus.NEW, Duration.ZERO, null);
    }

    // Геттеры для списка подзадач
    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    // Добавление подзадачи
    public void addSubtask(int subtaskId) {
        if (this.getId() == subtaskId) {
            throw new IllegalArgumentException("Эпик не может быть подзадачей самого себя.");
        }
        subtaskIds.add(subtaskId);
    }

    // Удаление подзадачи
    public void removeSubtask(int subtaskId) {
        subtaskIds.remove((Integer) subtaskId);
    }

    public void updateStatus(Map<Integer, Subtask> subtasks) {
        if (subtaskIds.isEmpty()) {
            setStatus(TaskStatus.NEW);
            setDuration(Duration.ZERO);
            setStartTime(null);
            return;
        }

        boolean allDone = true;
        boolean allNew = true;

        Duration calculatedDuration = Duration.ZERO;
        LocalDateTime calculatedStartTime = null;
        LocalDateTime calculatedEndTime = null;

        for (int subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                // Обновляем статус эпика
                if (subtask.getStatus() != TaskStatus.DONE) {
                    allDone = false;
                }
                if (subtask.getStatus() != TaskStatus.NEW) {
                    allNew = false;
                }

                // Рассчитываем duration (проверяем, что getDuration() не null)
                if (subtask.getDuration() != null) {
                    calculatedDuration = calculatedDuration.plus(subtask.getDuration());
                }

                // Рассчитываем startTime
                if (calculatedStartTime == null || (subtask.getStartTime() != null && subtask.getStartTime().isBefore(calculatedStartTime))) {
                    calculatedStartTime = subtask.getStartTime();
                }
            }
        }

        // Устанавливаем расчётные поля
        setDuration(calculatedDuration);
        setStartTime(calculatedStartTime);

        // Устанавливаем статус эпика
        if (allDone) {
            setStatus(TaskStatus.DONE);
        } else if (allNew) {
            setStatus(TaskStatus.NEW);
        } else {
            setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    @Override
    public String toString() {
        return "Epic{" +
                "title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", status=" + getStatus() +
                ", subtaskIds=" + subtaskIds +
                ", duration=" + (getDuration() != null ? getDuration().toMinutes() + " minutes" : "null") +
                ", startTime=" + (getStartTime() != null ? getStartTime() : "null") +
                ", endTime=" + (getEndTime() != null ? getEndTime() : "null") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Epic)) return false;
        Epic epic = (Epic) o;
        return getId() == epic.getId(); // Проверяем только id
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getId()); // Используем только id
    }
}
