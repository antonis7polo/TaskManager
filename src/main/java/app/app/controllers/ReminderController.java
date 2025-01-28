
package app.controllers;

import app.models.Reminder;
import app.models.Task;
import app.utils.JsonUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReminderController {
    private List<Reminder> reminders;
    private TaskController taskController;

    public ReminderController(TaskController taskController) {
        this.taskController = taskController;
        this.reminders = new ArrayList<>();
    }

    public void loadReminders(String filePath) {
        try {
            reminders = new ArrayList<>(JsonUtils.readJsonFile(filePath, Reminder[].class)); // Ensure modifiable list
            System.out.println("Reminders loaded successfully.");
        } catch (IOException e) {
            System.out.println("Failed to load reminders: " + e.getMessage());
        }
    }

    public void saveReminders(String filePath) {
        try {
            JsonUtils.writeJsonFile(filePath, reminders);
            System.out.println("Reminders saved successfully.");
        } catch (IOException e) {
            System.out.println("Failed to save reminders: " + e.getMessage());
        }
    }

    public List<Reminder> getReminders() {
        return new ArrayList<>(reminders);
    }

    public boolean addReminder(String taskId, LocalDate reminderDate, String description) {
        Task task = taskController.getTaskById(taskId);
        if (task == null) {
            System.out.println("Task not found.");
            return false;
        }

        if (task.getStatus() == Task.Status.COMPLETED) {
            System.out.println("Cannot set a reminder for a completed task.");
            return false;
        }

        if (!isReminderValid(task, reminderDate)) {
            System.out.println("Invalid reminder date for the task deadline.");
            return false;
        }

        reminders.add(new Reminder(taskId, reminderDate, description));
        System.out.println("Added reminder for task: " + taskId);
        return true;
    }

    public boolean modifyReminder(String taskId, LocalDate oldDate, LocalDate newDate, String newDescription) {
        for (Reminder reminder : reminders) {
            if (reminder.getTaskId().equals(taskId) && reminder.getReminderDate().equals(oldDate)) {
                Task task = taskController.getTaskById(taskId);
                if (task != null && isReminderValid(task, newDate)) {
                    reminder.setReminderDate(newDate);
                    reminder.setDescription(newDescription);
                    System.out.println("Reminder updated.");
                    return true;
                } else {
                    System.out.println("Invalid new reminder date for the task deadline.");
                    return false;
                }
            }
        }
        System.out.println("Reminder not found.");
        return false;
    }

    public boolean deleteReminder(String reminderId) {
        boolean removed = reminders.removeIf(r -> r.getId().equals(reminderId));
        if (removed) {
            System.out.println("Reminder deleted.");
        } else {
            System.out.println("Reminder not found.");
        }
        return removed;
    }

    public void deleteRemindersForTask(String taskId) {
        reminders.removeIf(r -> r.getTaskId().equals(taskId));
        System.out.println("All reminders for task '" + taskId + "' have been deleted.");  
    }

    private boolean isReminderValid(Task task, LocalDate reminderDate) {
        LocalDate deadline = task.getDeadline();
        return !reminderDate.isAfter(deadline) && !reminderDate.isBefore(deadline.minusMonths(1));
    }

    public List<Reminder> getRemindersForTask(String taskId) {
        return reminders.stream()
                .filter(reminder -> reminder.getTaskId().equals(taskId))
                .toList();
    }


    public void cleanExpiredReminders() {
        LocalDate today = LocalDate.now();
    
        List<Reminder> expiredReminders = reminders.stream()
                .filter(reminder -> reminder.getReminderDate().isBefore(today))
                .toList();
    
        if (!expiredReminders.isEmpty()) {
            reminders.removeAll(expiredReminders);
            System.out.println("Deleted expired reminders: " + expiredReminders.size());
        } else {
            System.out.println("No expired reminders to delete.");
        }
    }
    

    public void printReminders() {
        reminders.forEach(System.out::println);
    }
}
