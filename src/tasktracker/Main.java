package tasktracker;

import tasktracker.manager.TaskManager;
import tasktracker.manager.Managers;
import tasktracker.tasks.Epic;
import tasktracker.tasks.Subtask;
import tasktracker.tasks.Task;
import tasktracker.status.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        // Создаём задачи с уникальным временем начала
        Task task1 = new Task(
                "Переезд",
                "Организация переезда",
                manager.generateId(),
                TaskStatus.NEW,
                Duration.ofMinutes(120),
                LocalDateTime.now()
        );

        Task task2 = new Task(
                "Покупка квартиры",
                "Покупка новой квартиры",
                manager.generateId(),
                TaskStatus.NEW,
                Duration.ofMinutes(90),
                LocalDateTime.now().plusHours(3) // Не пересекается с task1
        );

        manager.createTask(task1);
        manager.createTask(task2);

        // Создаём эпик с двумя подзадачами
        Epic epic1 = new Epic(
                "Организация праздника",
                "Планирование большого праздника",
                manager.generateId()
        );
        manager.createEpic(epic1);

        Subtask subtask1 = new Subtask(
                        "Аренда зала",
                        "Аренда помещения для праздника",
                        manager.generateId(),
                        TaskStatus.NEW,
                Duration.ofMinutes(60),
                LocalDateTime.now().plusHours(6),
                epic1.getId() // Не пересекается с task2
                );

        Subtask subtask2 = new Subtask(
                        "Заказ еды",
                        "Заказ еды для гостей",
                        manager.generateId(),
                        TaskStatus.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.now().plusHours(7),
                epic1.getId() // Не пересекается с subtask1
                );

        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        // Создаём эпик с одной подзадачей
        Epic epic2 = new Epic(
                "Подготовка к экзамену",
                "Подготовка к сдаче экзамена",
                manager.generateId()
        );
        manager.createEpic(epic2);

        Subtask subtask3 = new Subtask(
                        "Изучение материала",
                        "Изучение всех тем",
                        manager.generateId(),
                        TaskStatus.NEW,
                Duration.ofMinutes(180),
                LocalDateTime.now().plusDays(1),
                epic2.getId() // Не пересекается с другими задачами
                );

        manager.createSubtask(subtask3);

        // Выводим списки задач, эпиков и подзадач
        System.out.println("Все задачи:");
        manager.getAllTasks().forEach(System.out::println);

        System.out.println("Все эпики:");
        manager.getAllEpics().forEach(System.out::println);

        System.out.println("Все подзадачи:");
        manager.getAllSubtasks().forEach(System.out::println);

        // Изменяем статусы подзадач
        subtask1.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask1);

        subtask2.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask2);

        // Проверяем статус эпика
        System.out.println("Статус эпика после завершения подзадач:");
        System.out.println(manager.getEpicById(epic1.getId()));

        // Удаляем одну из задач и один из эпиков
        manager.deleteTaskById(task1.getId());
        manager.deleteEpicById(epic2.getId());

        // Проверяем после удаления
        System.out.println("Все задачи после удаления:");
        manager.getAllTasks().forEach(System.out::println);

        System.out.println("Все эпики после удаления:");
        manager.getAllEpics().forEach(System.out::println);

        // Проверяем список задач в порядке приоритета
        System.out.println("Задачи в порядке приоритета:");
        manager.getPrioritizedTasks().forEach(System.out::println);

        // Вывод истории просмотров
        System.out.println("История просмотров:");
        manager.getHistory().forEach(System.out::println);

        // Проверяем пересечения задач
        Task overlappingTask = new Task(
                "Перекрывающаяся задача",
                "Описание задачи",
                manager.generateId(),
                TaskStatus.NEW,
                Duration.ofMinutes(60),
                task2.getStartTime().plusMinutes(100) // Теперь не пересекается
        );

        try {
            manager.createTask(overlappingTask);
            System.out.println("Перекрывающаяся задача успешно добавлена: " + overlappingTask);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}
