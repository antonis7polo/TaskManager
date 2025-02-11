
package app.controllers;

import app.models.Reminder;
import app.models.Task;
import app.utils.JsonUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for managing reminders associated with tasks.
 * Provides methods for loading, saving, adding, modifying, and deleting reminders.
 * 
 * @author Antonis Alexiadis
 * @version 1.0
 * @see app.controllers.TaskController
 */

public class ReminderController {
    private List<Reminder> reminders;
    private TaskController taskController;

/**
     * Constructs a new ReminderController with an empty list of reminders.
     *
     * @param taskController The TaskController instance for managing tasks.
     */


    public ReminderController(TaskController taskController) {
        this.taskController = taskController;
        this.reminders = new ArrayList<>();
    }


    /**
     * Loads reminders from a JSON file.
     *
     * @param filePath The path to the JSON file containing reminders.
     */

    public void loadReminders(String filePath) {
        try {
            reminders = new ArrayList<>(JsonUtils.readJsonFile(filePath, Reminder[].class)); 
            System.out.println("Reminders loaded successfully.");
        } catch (IOException e) {
            System.out.println("Failed to load reminders: " + e.getMessage());
        }
    }

/**
     * Saves the current list of reminders to a JSON file.
     *
     * @param filePath The path to the JSON file where reminders will be saved.
     */

    public void saveReminders(String filePath) {
        try {
            JsonUtils.writeJsonFile(filePath, reminders);
            System.out.println("Reminders saved successfully.");
        } catch (IOException e) {
            System.out.println("Failed to save reminders: " + e.getMessage());
        }
    }
/**
     * Retrieves all reminders.
     *
     * @return A list of all reminders.
     */
    public List<Reminder> getReminders() {
        return new ArrayList<>(reminders);
    }


/**
     * Adds a reminder for a specific task.
     *
     * @param taskId The ID of the task for which the reminder is set.
     * @param reminderDate The date of the reminder.
     * @param description A brief description of the reminder.
     * @return {@code true} if the reminder was successfully added, otherwise {@code false}.
     */


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


/**
     * Modifies an existing reminder.
     *
     * @param taskId The ID of the task associated with the reminder.
     * @param oldDate The current(old) date of the reminder.
     * @param newDate The new reminder date.
     * @param newDescription The new description for the reminder.
     * @return {@code true} if the reminder was successfully modified, otherwise {@code false}.
     */

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

/**
     * Deletes a reminder by its ID.
     *
     * @param reminderId The unique ID of the reminder to be deleted.
     * @return {@code true} if the reminder was successfully deleted, otherwise {@code false}.
     */

    public boolean deleteReminder(String reminderId) {
        boolean removed = reminders.removeIf(r -> r.getId().equals(reminderId));
        if (removed) {
            System.out.println("Reminder deleted.");
        } else {
            System.out.println("Reminder not found.");
        }
        return removed;
    }

/**
     * Deletes all reminders associated with a specific task.
     *
     * @param taskId The ID of the task whose reminders should be deleted.
     */


    public void deleteRemindersForTask(String taskId) {
        reminders.removeIf(r -> r.getTaskId().equals(taskId));
        System.out.println("All reminders for task '" + taskId + "' have been deleted.");  
    }

/**
 * Checks if the reminder date is within a valid range for the task.
 * A reminder must be set between one month before the deadline and the deadline itself.
 *
 * @param task The task for which the reminder is set.
 * @param reminderDate The date of the reminder.
 * @return {@code true} if the reminder date is valid, otherwise {@code false}.
 */

    private boolean isReminderValid(Task task, LocalDate reminderDate) {
        LocalDate deadline = task.getDeadline();
        return !reminderDate.isAfter(deadline) && !reminderDate.isBefore(deadline.minusMonths(1));
    }

    /**
    * Retrieves all reminders associated with a specific task.
    *
    * @param taskId The ID of the task.
    * @return A list of reminders for the specified task.
    */

    public List<Reminder> getRemindersForTask(String taskId) {
        return reminders.stream()
                .filter(reminder -> reminder.getTaskId().equals(taskId))
                .toList();
    }


/**
     * Deletes all reminders that are past their scheduled date.
     */

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
    
/**
     * Prints all reminders to the console.
     */

    public void printReminders() {
        reminders.forEach(System.out::println);
    }
}
