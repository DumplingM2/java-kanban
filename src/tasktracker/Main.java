package tasktracker;

import tasktracker.manager.TaskManager;
import tasktracker.tasks.Epic;
import tasktracker.tasks.Subtask;
import tasktracker.tasks.Task;
import tasktracker.status.TaskStatus;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        // Создаём задачи
        Task task1 = new Task("Переезд", "Организация переезда", manager.generateId(), TaskStatus.NEW);
        Task task2 = new Task("Покупка квартиры", "Покупка новой квартиры", manager.generateId(), TaskStatus.NEW);
        manager.createTask(task1);
        manager.createTask(task2);

        // Создаём эпик с двумя подзадачами
        Epic epic1 = new Epic("Организация праздника", "Планирование большого праздника", manager.generateId());
        manager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Аренда зала", "Аренда помещения для праздника", manager.generateId(), TaskStatus.NEW, epic1.getId());
        Subtask subtask2 = new Subtask("Заказ еды", "Заказ еды для гостей", manager.generateId(), TaskStatus.NEW, epic1.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        // Создаём эпик с одной подзадачей
        Epic epic2 = new Epic("Подготовка к экзамену", "Подготовка к сдаче экзамена", manager.generateId());
        manager.createEpic(epic2);

        Subtask subtask3 = new Subtask("Изучение материала", "Изучение всех тем", manager.generateId(), TaskStatus.NEW, epic2.getId());
        manager.createSubtask(subtask3);

        // Выводим списки
        System.out.println("Все задачи:");
        System.out.println(manager.getAllTasks());

        System.out.println("Все эпики:");
        System.out.println(manager.getAllEpics());

        System.out.println("Все подзадачи:");
        System.out.println(manager.getAllSubtasks());

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
        System.out.println(manager.getAllTasks());

        System.out.println("Все эпики после удаления:");
        System.out.println(manager.getAllEpics());
    }
}
