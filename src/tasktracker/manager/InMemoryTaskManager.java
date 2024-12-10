package tasktracker.manager;

import tasktracker.tasks.Task;
import tasktracker.tasks.Epic;
import tasktracker.tasks.Subtask;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    private final Set<Task> prioritizedTasks = new TreeSet<>((task1, task2) -> {
        if (task1.getStartTime() == null && task2.getStartTime() == null) return 0;
        if (task1.getStartTime() == null) return 1;
        if (task2.getStartTime() == null) return -1;
        return task1.getStartTime().compareTo(task2.getStartTime());
    });

    private int idCounter = 1;

    // Генерация ID
    public int generateId() {
        return idCounter++;
    }

    // Проверка пересечения задач
    private boolean isOverlapping(Task task1, Task task2) {
        if (task1.getStartTime() == null || task2.getStartTime() == null) {
            return false;
        }
        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = task2.getEndTime();

        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    private boolean isValidTask(Task newTask) {
        return prioritizedTasks.stream()
                .filter(existingTask -> existingTask.getId() != newTask.getId()) // Исключаем текущую задачу
                .noneMatch(existingTask -> isOverlapping(newTask, existingTask));
    }

    // Создание задач
    @Override
    public void createTask(Task task) {
        if (task.getId() == 0) {
            task.setId(generateId()); // Генерируем новый ID, если id == 0
        }

        if (!isValidTask(task)) {
            throw new IllegalArgumentException("Задача пересекается с другой задачей по времени выполнения.");
        }
        tasks.put(task.getId(), task);
        prioritizedTasks.add(task);
    }

    @Override
    public void createEpic(Epic epic) {
        if (epic.getId() == 0) {
            epic.setId(generateId()); // Генерируем новый ID для эпика, если id == 0
        }
        epics.put(epic.getId(), epic);
    }

    @Override
    public void createSubtask(Subtask subtask) {
        if (subtask.getId() == 0) {
            subtask.setId(generateId()); // Генерируем новый ID для подзадачи, если id == 0
        }
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null && isValidTask(subtask)) {
            subtasks.put(subtask.getId(), subtask);
            if (subtask.getStartTime() != null) {
                prioritizedTasks.add(subtask);
            }
            epic.addSubtask(subtask.getId());
            epic.updateStatus(subtasks);
        } else {
            throw new IllegalArgumentException("Подзадача пересекается с другой задачей по времени выполнения.");
        }
    }

    // Обновление задач
    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId()) && isValidTask(task)) {
            // Удаляем старую версию задачи из приоритетного набора, прежде чем добавить обновлённую
            prioritizedTasks.removeIf(t -> t.getId() == task.getId());
            tasks.put(task.getId(), task);
            if (task.getStartTime() != null) {
                prioritizedTasks.add(task);
            }
        } else {
            throw new IllegalArgumentException("Задача пересекается с другой задачей по времени выполнения.");
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId()) && isValidTask(subtask)) {
            prioritizedTasks.removeIf(t -> t.getId() == subtask.getId());
            subtasks.put(subtask.getId(), subtask);
            if (subtask.getStartTime() != null) {
                prioritizedTasks.add(subtask);
            }
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.updateStatus(subtasks);
            }
        } else {
            throw new IllegalArgumentException("Подзадача пересекается с другой задачей по времени выполнения.");
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
            epic.updateStatus(subtasks);
        }
    }

    // Получение задач
    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public List<Subtask> getSubtasksOfEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return Collections.emptyList();
        }
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    // Удаление задач
    @Override
    public void deleteTaskById(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            prioritizedTasks.remove(task);
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subtaskId : epic.getSubtaskIds()) {
                Subtask subtask = subtasks.remove(subtaskId);
                if (subtask != null) {
                    prioritizedTasks.remove(subtask);
                }
            }
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            prioritizedTasks.remove(subtask);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtask(id);
                epic.updateStatus(subtasks);
            }
        }
    }

    @Override
    public void deleteAllTasks() {
        List<Task> allTasks = new ArrayList<>(tasks.values());
        tasks.clear();
        allTasks.forEach(prioritizedTasks::remove);
    }

    @Override
    public void deleteAllEpics() {
        List<Subtask> allSubtasks = new ArrayList<>(subtasks.values());
        subtasks.clear();
        allSubtasks.forEach(prioritizedTasks::remove);
        epics.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        List<Subtask> allSubtasks = new ArrayList<>(subtasks.values());
        subtasks.clear();
        allSubtasks.forEach(prioritizedTasks::remove);
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            epic.updateStatus(subtasks);
        }
    }

    // История
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    // Новый метод: задачи в порядке приоритета
    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }
}
