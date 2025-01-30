package app.controllers;

import app.ReminderManagementPane;
import app.models.Task;
import app.utils.JsonUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskController {
    private List<Task> tasks;
    private ReminderController reminderController;

    public TaskController(ReminderController reminderController) {
        this.tasks = new ArrayList<>();
        this.reminderController = reminderController;
    }

    public void loadTasks(String filePath) {
        try {
            tasks = new ArrayList<>(JsonUtils.readJsonFile(filePath, Task[].class));  
            System.out.println("Tasks loaded successfully.");
        } catch (IOException e) {
            System.out.println("Failed to load tasks: " + e.getMessage());
        }
    }
    public void saveTasks(String filePath) {
        try {
            JsonUtils.writeJsonFile(filePath, tasks);
            System.out.println("Tasks saved successfully.");
        } catch (IOException e) {
            System.out.println("Failed to save tasks: " + e.getMessage());
        }
    }



    public void addTask(Task task) {
        tasks.add(task);
    }

    public void updateTasksWithModifiedCategory(String oldCategory, String newCategory) {
        tasks.forEach(task -> {
            if (task.getCategory() != null && task.getCategory().getName().equalsIgnoreCase(oldCategory)) {
                task.getCategory().setName(newCategory);
            }
        });
    }

    public List<String> deleteTasksByCategory(String categoryName) {
        List<Task> tasksToRemove = new ArrayList<>();
        List<String> deletedTaskIds = new ArrayList<>();
    
        // Collect tasks that belong to the category
        tasks.forEach(task -> {
            if (task.getCategory() != null && task.getCategory().getName().equalsIgnoreCase(categoryName)) {
                tasksToRemove.add(task);
            }
        });
    
        // Call deleteTask for each task and store its ID
        tasksToRemove.forEach(task -> {
            if (deleteTask(task.getId())) {
                deletedTaskIds.add(task.getId()); // Store task ID for refreshing reminders
            }
        });
    
        System.out.println("Tasks and associated reminders for category '" + categoryName + "' have been deleted.");
        return deletedTaskIds; // Return deleted task IDs
    }
    

    public boolean deleteTask(String taskId) {
        Task task = getTaskById(taskId);
        if (task == null) {
            System.out.println("Task not found.");
            return false;
        }

        tasks.remove(task);
        reminderController.deleteRemindersForTask(taskId);
        System.out.println("Task and its associated reminders have been deleted.");
        return true;
    }


    public void updateTasksWithModifiedPriority(String oldPriority, String newPriority) {
        tasks.forEach(task -> {
            if (task.getPriority().equalsIgnoreCase(oldPriority)) {
                task.setPriority(newPriority);
            }
        });
    }
    

    public void updateTasksWithDeletedPriority(String deletedPriority) {
        System.out.println("updateTasksWithDeletedPriority called for: " + deletedPriority);
        tasks.forEach(task -> {
            if (task.getPriority().equalsIgnoreCase(deletedPriority)) {
                task.setPriority("Default"); 
            }
        });
        System.out.println("Tasks with priority '" + deletedPriority + "' updated to 'Default'.");
    }
    

    public boolean updateTaskStatus(String taskId, Task.Status newStatus) {
        Task task = getTaskById(taskId);
        if (task == null) {
            System.out.println("Task not found.");
            return false;
        }

        task.setStatus(newStatus);

        if (newStatus == Task.Status.COMPLETED) {
            reminderController.deleteRemindersForTask(taskId);  
            System.out.println("Task marked as Completed. All associated reminders have been deleted.");
        }
        return true;
    }

    public void updateOverdueTasks() {
    LocalDate today = LocalDate.now();

    tasks.forEach(task -> {
    if (task.getDeadline() != null && task.getDeadline().isBefore(today) && task.getStatus() != Task.Status.COMPLETED) {
            task.setStatus(Task.Status.DELAYED);
        }
    });

    System.out.println("Overdue tasks have been updated to DELAYED.");
}


    public Task getTaskById(String id) {
        return tasks.stream()
                .filter(task -> task.getId().equals(id))
                .findFirst()
                .orElse(null);
    }


    public List<Task> getTasks() {
        return tasks;
    }

    public long getTotalTasks() {
        return tasks.size();
    }
    
    public long getCompletedTasks() {
        return tasks.stream().filter(task -> task.getStatus() == Task.Status.COMPLETED).count();
    }
    
    public long getDelayedTasks() {
        return tasks.stream().filter(task -> task.getStatus() == Task.Status.DELAYED).count();
    }
    
    public long getUpcomingTasks() {
        LocalDate today = LocalDate.now();
        LocalDate oneWeekFromNow = today.plusDays(7);
    
        return tasks.stream()
                .filter(task -> task.getDeadline() != null 
                        && (task.getDeadline().isEqual(today) || (task.getDeadline().isAfter(today) && task.getDeadline().isBefore(oneWeekFromNow.plusDays(1)))))
                .count();
    }
    

    
    public void printTasks() {
        tasks.forEach(System.out::println);
    }
}
