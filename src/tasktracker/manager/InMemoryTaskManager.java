package tasktracker.manager;

import tasktracker.tasks.Task;
import tasktracker.tasks.Epic;
import tasktracker.tasks.Subtask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private int idCounter = 1;

    public int generateId() {
        return idCounter++;
    }

    // Реализуем метод обновления подзадачи
    @Override
    public void updateSubtask(Subtask subtask) {
        // Проверяем, существует ли подзадача
        if (subtasks.containsKey(subtask.getId())) {
            // Обновляем подзадачу в хранилище
            subtasks.put(subtask.getId(), subtask);

            // Получаем эпик, к которому относится подзадача, и обновляем его статус
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.updateStatus(subtasks);
            }
        }
    }

    // Методы создания задач, эпиков и подзадач
    @Override
    public void createTask(Task task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public void createEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    @Override
    public void createSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            subtasks.put(subtask.getId(), subtask);
            epic.addSubtask(subtask.getId());
            epic.updateStatus(subtasks);
        }
    }

    // Методы получения задач и истории
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
        Epic epic = epics.get(epicId); // Получаем эпик по ID
        if (epic == null) {
            return new ArrayList<>(); // Если эпик не найден, возвращаем пустой список
        }

        List<Subtask> epicSubtasks = new ArrayList<>();
        for (int subtaskId : epic.getSubtaskIds()) { // Проходим по всем подзадачам эпика
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                epicSubtasks.add(subtask); // Добавляем подзадачи в список
            }
        }
        return epicSubtasks; // Возвращаем список подзадач эпика
    }


    // Метод обновления эпика
    @Override
    public void updateEpic(Epic epic) {
        // Проверяем, существует ли эпик
        if (epics.containsKey(epic.getId())) {
            // Обновляем эпик в хранилище
            epics.put(epic.getId(), epic);

            // Пересчитываем статус эпика в зависимости от статусов его подзадач
            epic.updateStatus(subtasks);
        }
    }

    @Override
    public void updateTask(Task task) {
        // Проверяем, существует ли задача
        if (tasks.containsKey(task.getId())) {
            // Обновляем задачу в хранилище
            tasks.put(task.getId(), task);
        }
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

    // Методы удаления задач
    @Override
    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtask(id);
                epic.updateStatus(subtasks);
            }
        }
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            epic.updateStatus(subtasks);
        }
    }

    // Метод для получения истории
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();  // Возвращаем историю из менеджера истории
    }

}
