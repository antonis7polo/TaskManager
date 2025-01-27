package app.controllers;

import app.models.Priority;
import app.utils.JsonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PriorityController {
    private List<Priority> priorities;
    private TaskController taskController;

    public PriorityController(TaskController taskController) {
        this.taskController = taskController;
        this.priorities = new ArrayList<>();
        // Ensure Default priority exists on initialization
        if (priorities.stream().noneMatch(p -> p.getName().equalsIgnoreCase("Default"))) {
            priorities.add(Priority.defaultPriority());
        }
    }

    public void loadPriorities(String filePath) {
        try {
            priorities = new ArrayList<>(JsonUtils.readJsonFile(filePath, Priority[].class));
            // Ensure Default priority exists after loading
            if (priorities.stream().noneMatch(p -> p.getName().equalsIgnoreCase("Default"))) {
                priorities.add(Priority.defaultPriority());
            }
            System.out.println("Priorities loaded successfully.");
        } catch (IOException e) {
            System.out.println("Failed to load priorities: " + e.getMessage());
            // Ensure priorities list is initialized with Default
            priorities = new ArrayList<>();
            priorities.add(Priority.defaultPriority());
        }
    }

    public void savePriorities(String filePath) {
        try {
            JsonUtils.writeJsonFile(filePath, priorities);
            System.out.println("Priorities saved successfully.");
        } catch (IOException e) {
            System.out.println("Failed to save priorities: " + e.getMessage());
        }
    }

    // Get all priorities
    public List<Priority> getPriorities() {
        return new ArrayList<>(priorities); // Return a copy to avoid external modification
    }

    // Add a new priority
    public boolean addPriority(String name) {
        if (priorities.stream().anyMatch(p -> p.getName().equalsIgnoreCase(name))) {
            System.out.println("Priority already exists.");
            return false;
        }
        priorities.add(new Priority(name));
        return true;
    }

    // Modify a priority name
    public boolean modifyPriority(String oldName, String newName) {
        for (Priority p : priorities) {
            if (p.getName().equalsIgnoreCase(oldName)) {
                if (!"Default".equalsIgnoreCase(oldName)) { // Prevent modifying Default priority
                    p.setName(newName);
                    taskController.updateTasksWithModifiedPriority(oldName, newName); // Update tasks
                    return true;
                } else {
                    System.out.println("Cannot modify the Default priority.");
                    return false;
                }
            }
        }
        System.out.println("Priority not found.");
        return false;
    }

    // Delete a priority and update associated tasks
    public boolean deletePriority(String name) {
        System.out.println("deleteSelectedPriority called"); 
        if ("Default".equalsIgnoreCase(name)) {
            System.out.println("Cannot delete the Default priority.");
            return false;
        }

        boolean removed = priorities.removeIf(p -> p.getName().equalsIgnoreCase(name));
        if (removed) {
            taskController.updateTasksWithDeletedPriority(name); // Update tasks to Default priority
            System.out.println("Priority '" + name + "' deleted. Associated tasks updated to 'Default'.");
        } else {
            System.out.println("Priority not found.");
        }
        return removed;
    }

    public void printPriorities() {
        priorities.forEach(System.out::println);
    }
}
