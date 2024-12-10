package tasktracker.tasks;

import tasktracker.status.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();

    public Epic(String title, String description, int id) {
        super(title, description, id, TaskStatus.NEW, Duration.ZERO, null);
    }

    // Добавляем сеттер для id
    public void setId(int id) {
        // Поскольку Epic наследуется от Task, можно либо добавить protected сеттер в Task,
        // либо использовать super.setId(id), если он protected/public
        super.setId(id);
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtask(int subtaskId) {
        if (this.getId() == subtaskId) {
            throw new IllegalArgumentException("Эпик не может быть подзадачей самого себя.");
        }
        subtaskIds.add(subtaskId);
    }

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

        for (int subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                if (subtask.getStatus() != TaskStatus.DONE) {
                    allDone = false;
                }
                if (subtask.getStatus() != TaskStatus.NEW) {
                    allNew = false;
                }
                if (subtask.getDuration() != null) {
                    calculatedDuration = calculatedDuration.plus(subtask.getDuration());
                }
                if (calculatedStartTime == null ||
                        (subtask.getStartTime() != null && subtask.getStartTime().isBefore(calculatedStartTime))) {
                    calculatedStartTime = subtask.getStartTime();
                }
            }
        }

        setDuration(calculatedDuration);
        setStartTime(calculatedStartTime);

        if (allDone) {
            setStatus(TaskStatus.DONE);
        } else if (allNew) {
            setStatus(TaskStatus.NEW);
        } else {
            setStatus(TaskStatus.IN_PROGRESS);
        }
    }
}
