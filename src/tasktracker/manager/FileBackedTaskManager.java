package tasktracker.manager;

import tasktracker.tasks.Task;
import tasktracker.tasks.Epic;
import tasktracker.tasks.Subtask;
import tasktracker.tasks.TaskType;
import tasktracker.status.TaskStatus;
import tasktracker.exceptions.ManagerSaveException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public static void main(String[] args) {
        File file = new File("src/tasktracker/resources/tasks.csv");

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs(); // Создает каталог resources, если он не существует
        }

        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task1 = new Task("Task 1", "Description of task 1", manager.generateId(), TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2023, 1, 1, 10, 0));
        Task task2 = new Task("Task 2", "Description of task 2", manager.generateId(), TaskStatus.IN_PROGRESS,
                Duration.ofMinutes(60), LocalDateTime.of(2023, 1, 1, 11, 0));
        manager.createTask(task1);
        manager.createTask(task2);

        Epic epic1 = new Epic("Epic 1", "Description of epic 1", manager.generateId());
        manager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Subtask 1", "Description of subtask 1", manager.generateId(), TaskStatus.NEW,
                Duration.ofMinutes(120), LocalDateTime.of(2023, 1, 1, 12, 0), epic1.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Description of subtask 2", manager.generateId(), TaskStatus.DONE,
                Duration.ofMinutes(90), LocalDateTime.of(2023, 1, 1, 14, 0), epic1.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        System.out.println("Данные начального менеджера:");
        System.out.println("Задачи: " + manager.getAllTasks());
        System.out.println("Эпики: " + manager.getAllEpics());
        System.out.println("Подзадачи: " + manager.getAllSubtasks());

        // Создаем новый менеджер, загружая данные из того же файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        System.out.println("\nДанные загруженного менеджера:");
        System.out.println("Задачи: " + loadedManager.getAllTasks());
        System.out.println("Эпики: " + loadedManager.getAllEpics());
        System.out.println("Подзадачи: " + loadedManager.getAllSubtasks());

        // Сравниваем данные старого и нового менеджера
        if (manager.getAllTasks().equals(loadedManager.getAllTasks()) &&
                manager.getAllEpics().equals(loadedManager.getAllEpics()) &&
                manager.getAllSubtasks().equals(loadedManager.getAllSubtasks())) {
            System.out.println("\nВсе данные совпадают между исходным и загруженным менеджерами.");
        } else {
            System.out.println("\nДанные не совпадают между исходным и загруженным менеджерами.");
        }
    }

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    private void save() {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("id,type,name,status,description,duration,startTime,epic\n");
            for (Task task : getAllTasks()) {
                writer.write(toString(task) + "\n");
            }
            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic) + "\n");
                for (Subtask subtask : getSubtasksOfEpic(epic.getId())) {
                    writer.write(toString(subtask) + "\n");
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл", e);
        }
    }

    // Для тестов save
    public void saveToFile() {
        save();
    }

    private String toString(Task task) {
        TaskType type = TaskType.TASK;
        if (task instanceof Epic) {
            type = TaskType.EPIC;
        } else if (task instanceof Subtask) {
            type = TaskType.SUBTASK;
        }
        String duration = task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : "";
        String startTime = task.getStartTime() != null ? task.getStartTime().toString() : "";
        String epicId = task instanceof Subtask ? String.valueOf(((Subtask) task).getEpicId()) : "";
        return String.join(",",
                String.valueOf(task.getId()),
                type.toString(),
                task.getTitle(),
                task.getStatus().toString(),
                task.getDescription(),
                duration,
                startTime,
                epicId
        );
    }

    private static Task fromString(String value) {
        String[] fields = value.split(",");
        if (fields.length < 5) { // Минимум 5 полей: id, type, name, status, description
            throw new IllegalArgumentException("Некорректная строка CSV: " + value);
        }

        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        TaskStatus status = TaskStatus.valueOf(fields[3]);
        String description = fields[4];
        Duration duration = fields.length > 5 && !fields[5].isEmpty() ? Duration.ofMinutes(Long.parseLong(fields[5])) : null;
        LocalDateTime startTime = fields.length > 6 && !fields[6].isEmpty() ? LocalDateTime.parse(fields[6]) : null;

        if (type == TaskType.TASK) {
            return new Task(name, description, id, status, duration, startTime);
        } else if (type == TaskType.EPIC) {
            return new Epic(name, description, id);
        } else if (type == TaskType.SUBTASK) {
            if (fields.length < 8 || fields[7].isEmpty()) {
                throw new IllegalArgumentException("Некорректная строка CSV для Subtask: " + value);
            }
            int epicId = Integer.parseInt(fields[7]);
            return new Subtask(name, description, id, status, duration, startTime, epicId);
        } else {
            throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            for (String line : lines.subList(1, lines.size())) {
                Task task = fromString(line);
                if (task instanceof Epic) {
                    manager.createEpic((Epic) task);
                } else if (task instanceof Subtask) {
                    manager.createSubtask((Subtask) task);
                } else {
                    manager.createTask(task);
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла", e);
        }
        return manager;
    }

    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void createEpic(Epic epic) {
        super.createEpic(epic);
        save();
    }

    @Override
    public void createSubtask(Subtask subtask) {
        super.createSubtask(subtask);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }
}
