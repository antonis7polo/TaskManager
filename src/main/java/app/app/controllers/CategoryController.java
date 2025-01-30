package app.controllers;

import app.models.Category;
import app.utils.JsonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CategoryController {
    private List<Category> categories;
    private TaskController taskController;  

    public CategoryController(TaskController taskController) {
        this.taskController = taskController;
        this.categories = new ArrayList<>();
    }

    public void loadCategories(String filePath) {
        try {
            categories = new ArrayList<>(JsonUtils.readJsonFile(filePath, Category[].class));  // Ensure modifiable list
            System.out.println("Categories loaded successfully.");
        } catch (IOException e) {
            System.out.println("Failed to load categories: " + e.getMessage());
        }
    }
    

    public void saveCategories(String filePath) {
        try {
            JsonUtils.writeJsonFile(filePath, categories);
            System.out.println("Categories saved successfully.");
        } catch (IOException e) {
            System.out.println("Failed to save categories: " + e.getMessage());
        }
    }

    // Get all categories
    public List<Category> getCategories() {
        return new ArrayList<>(categories);  
    }

    // Add a new category
    public boolean addCategory(String name) {
        for (Category c : categories) {
            if (c.getName().equalsIgnoreCase(name)) {
                System.out.println("Category already exists.");
                return false;
            }
        }
        categories.add(new Category(name));
        return true;
    }

    // Modify a category name
    public boolean modifyCategory(String oldName, String newName) {
        for (Category c : categories) {
            if (c.getName().equalsIgnoreCase(oldName)) {
                c.setName(newName);
                taskController.updateTasksWithModifiedCategory(oldName, newName);  
                return true;
            }
        }
        System.out.println("Category not found.");
        return false;
    }

    
    public List<String> deleteCategory(String name) {
        boolean removed = categories.removeIf(c -> c.getName().equalsIgnoreCase(name));
        if (removed) {
            List<String> deletedTaskIds = taskController.deleteTasksByCategory(name); // Get deleted task IDs
            System.out.println("Category '" + name + "' and its associated tasks have been deleted.");
            return deletedTaskIds; // Return the deleted task IDs
        } else {
            System.out.println("Category not found.");
            return new ArrayList<>(); // Return empty list if nothing was deleted
        }
    }

    // Print all categories for testing
    public void printCategories() {
        categories.forEach(System.out::println);
    }
}
