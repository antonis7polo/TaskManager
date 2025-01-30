package app;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

import app.controllers.*;
import app.models.*;


public class CategoryManagementPane extends BorderPane {
    private final CategoryController categoryController;
    private final TaskController taskController;
    private final ObservableList<Category> categoryList;
    private final SummaryPane summaryPane;
    private final ReminderManagementPane reminderPane;

    public CategoryManagementPane(CategoryController categoryController, TaskController taskController,SummaryPane summaryPane,ReminderManagementPane reminderPane) {
        this.categoryController = categoryController;
        this.taskController = taskController;
        this.categoryList = FXCollections.observableArrayList(categoryController.getCategories());
        this.summaryPane = summaryPane;
        this.reminderPane = reminderPane;
        TableView<Category> categoryTable = createCategoryTable();

        // Add buttons
        Button addButton = new Button("+ Add Category");
        addButton.setOnAction(e -> showAddCategoryDialog());

        Button editButton = new Button("Edit Category");
        editButton.setOnAction(e -> showEditCategoryDialog(categoryTable.getSelectionModel().getSelectedItem()));

        Button deleteButton = new Button("Delete Category");
        deleteButton.setOnAction(e -> deleteSelectedCategory(categoryTable.getSelectionModel().getSelectedItem()));

        HBox buttonBar = new HBox(10, addButton, editButton, deleteButton);
        buttonBar.setPadding(new Insets(10));

        // Layout
        setTop(buttonBar);
        setCenter(categoryTable);
    }

    private TableView<Category> createCategoryTable() {
        TableView<Category> tableView = new TableView<>(categoryList);

        TableColumn<Category, Number> numberColumn = new TableColumn<>("#");
        numberColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(tableView.getItems().indexOf(data.getValue()) + 1));
        numberColumn.setPrefWidth(50);

        TableColumn<Category, String> nameColumn = new TableColumn<>("Category Name");
        nameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getName()));
        nameColumn.setPrefWidth(200);

        tableView.getColumns().addAll(numberColumn, nameColumn);

        return tableView;
    }

    private void updateCategoryList() {
        categoryList.setAll(categoryController.getCategories());
    }

    private void showAddCategoryDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Category");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter Category Name:");
        dialog.showAndWait().ifPresent(categoryName -> {
            if (categoryName.trim().isEmpty()) {
                showErrorAlert("Validation Error", "Category name cannot be empty.");
            } else if (!categoryController.addCategory(categoryName)) {
                showErrorAlert("Duplicate Category", "Category already exists.");
            } else {
                updateCategoryList();
            }
        });
    }

    private void showEditCategoryDialog(Category category) {
        if (category == null) {
            showErrorAlert("No Selection", "Please select a category to edit.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(category.getName());
        dialog.setTitle("Edit Category");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter New Category Name:");

        dialog.showAndWait().ifPresent(newCategoryName -> {
            if (newCategoryName.trim().isEmpty()) {
                showErrorAlert("Validation Error", "Category name cannot be empty.");
            } else if (!categoryController.modifyCategory(category.getName(), newCategoryName)) {
                showErrorAlert("Error", "Failed to rename category.");
            } else {
                taskController.updateTasksWithModifiedCategory(category.getName(), newCategoryName);
                updateCategoryList(); 
                refreshTaskTable();   
            }
        });
    }

    private void deleteSelectedCategory(Category category) {
        if (category == null) {
            showErrorAlert("No Selection", "Please select a category to delete.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Category");
        confirmation.setHeaderText("Are you sure you want to delete the category: " + category.getName() + "?");
        confirmation.setContentText("This will delete all associated tasks and reminders.");

        confirmation.showAndWait().ifPresent(response -> {
        if (response == ButtonType.OK) {
            List<String> deletedTaskIds = categoryController.deleteCategory(category.getName()); // Get deleted task IDs
            
            if (deletedTaskIds.isEmpty()) {
                showErrorAlert("Error", "Failed to delete category.");
            } else {
                // Refresh reminders for each deleted task
                deletedTaskIds.forEach(taskId -> reminderPane.refreshAfterTaskDeletion(taskId));

                updateCategoryList();
                refreshTaskTable();
                summaryPane.updateSummary();
            }
        }
    });
}

    public void refreshTaskTable() {
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
