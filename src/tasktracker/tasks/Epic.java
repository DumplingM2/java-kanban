package tasktracker.tasks;

import tasktracker.status.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>(); // Список ID подзадач
    private Duration duration = Duration.ZERO; // Расчётное поле: общая продолжительность эпика
    private LocalDateTime startTime; // Расчётное поле: время начала эпика
    private LocalDateTime endTime; // Расчётное поле: время завершения эпика

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

    // Метод для обновления статуса эпика и расчётных полей
    public void updateStatus(Map<Integer, Subtask> subtasks) {
        if (subtaskIds.isEmpty()) {
            setStatus(TaskStatus.NEW);
            duration = Duration.ZERO;
            startTime = null;
            endTime = null;
            return;
        }

        boolean allDone = true;
        boolean allNew = true;

        duration = Duration.ZERO; // Обнуляем продолжительность перед расчетом
        startTime = null; // Обнуляем время начала
        endTime = null; // Обнуляем время окончания

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

                // Рассчитываем duration
                duration = duration.plus(subtask.getDuration());

                // Рассчитываем startTime
                if (startTime == null || (subtask.getStartTime() != null && subtask.getStartTime().isBefore(startTime))) {
                    startTime = subtask.getStartTime();
                }

                // Рассчитываем endTime
                LocalDateTime subtaskEndTime = subtask.getEndTime();
                if (endTime == null || (subtaskEndTime != null && subtaskEndTime.isAfter(endTime))) {
                    endTime = subtaskEndTime;
                }
            }
        }

        // Устанавливаем статус эпика
        if (allDone) {
            setStatus(TaskStatus.DONE);
        } else if (allNew) {
            setStatus(TaskStatus.NEW);
        } else {
            setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    // Переопределяем геттеры для расчётных полей
    @Override
    public Duration getDuration() {
        return duration;
    }

    @Override
    public LocalDateTime getStartTime() {
        return startTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", status=" + getStatus() +
                ", subtaskIds=" + subtaskIds +
                ", duration=" + (duration != null ? duration.toMinutes() + " minutes" : "null") +
                ", startTime=" + (startTime != null ? startTime : "null") +
                ", endTime=" + (endTime != null ? endTime : "null") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Epic)) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subtaskIds, epic.subtaskIds) &&
                Objects.equals(duration, epic.duration) &&
                Objects.equals(startTime, epic.startTime) &&
                Objects.equals(endTime, epic.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtaskIds, duration, startTime, endTime);
    }
}
