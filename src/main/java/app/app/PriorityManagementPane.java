package app;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import app.controllers.PriorityController;
import app.models.Priority;

public class PriorityManagementPane extends BorderPane {
    private final PriorityController priorityController;
    private final ObservableList<Priority> priorityList;
    private final MainContentPane mainContentPane;

    public PriorityManagementPane(PriorityController priorityController,MainContentPane mainContentPane) {
        this.priorityController = priorityController;
        this.mainContentPane = mainContentPane;

        if (priorityController.getPriorities().stream().noneMatch(p -> p.getName().equalsIgnoreCase("Default"))) {
            priorityController.addPriority("Default");
        }

        this.priorityList = FXCollections.observableArrayList(priorityController.getPriorities());

        TableView<Priority> priorityTable = createPriorityTable();

        Button addButton = new Button("+ Add Priority");
        addButton.setOnAction(e -> showAddPriorityDialog());

        Button editButton = new Button("Edit Priority");
        editButton.setOnAction(e -> showEditPriorityDialog(priorityTable.getSelectionModel().getSelectedItem()));

        Button deleteButton = new Button("Delete Priority");
        deleteButton.setOnAction(e -> deleteSelectedPriority(priorityTable.getSelectionModel().getSelectedItem()));

        priorityTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            System.out.println("Selected priority: " + (newSelection != null ? newSelection.getName() : "None"));
            if (newSelection != null && "Default".equalsIgnoreCase(newSelection.getName())) {
                editButton.setDisable(true);
                deleteButton.setDisable(true);
            } else {
                editButton.setDisable(false);
                deleteButton.setDisable(false);
            }
        });

        HBox buttonBar = new HBox(10, addButton, editButton, deleteButton);
        buttonBar.setPadding(new Insets(10));

        setTop(buttonBar);
        setCenter(priorityTable);
    }

    private TableView<Priority> createPriorityTable() {
        TableView<Priority> tableView = new TableView<>(priorityList);

        TableColumn<Priority, Number> numberColumn = new TableColumn<>("#");
        numberColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(tableView.getItems().indexOf(data.getValue()) + 1));
        numberColumn.setPrefWidth(50);

        TableColumn<Priority, String> nameColumn = new TableColumn<>("Priority Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(200);

        tableView.getColumns().addAll(numberColumn, nameColumn);
        return tableView;
    }

    private void updatePriorityList() {
        priorityList.setAll(priorityController.getPriorities());
    }

    private void showAddPriorityDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Priority");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter Priority Name:");

        dialog.showAndWait().ifPresent(priorityName -> {
            if (priorityName.trim().isEmpty()) {
                showErrorAlert("Validation Error", "Priority name cannot be empty.");
            } else if ("Default".equalsIgnoreCase(priorityName)) {
                showErrorAlert("Invalid Priority", "'Default' priority cannot be added.");
            } else if (!priorityController.addPriority(priorityName)) {
                showErrorAlert("Duplicate Priority", "Priority already exists.");
            } else {
                updatePriorityList(); 
            }
        });
    }

    private void showEditPriorityDialog(Priority priority) {
        if (priority == null) {
            showErrorAlert("No Selection", "Please select a priority to edit.");
            return;
        }

        if ("Default".equalsIgnoreCase(priority.getName())) {
            showErrorAlert("Invalid Operation", "'Default' priority cannot be edited.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(priority.getName());
        dialog.setTitle("Edit Priority");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter New Priority Name:");

        dialog.showAndWait().ifPresent(newPriorityName -> {
            if (newPriorityName.trim().isEmpty()) {
                showErrorAlert("Validation Error", "Priority name cannot be empty.");
            } else if (!priorityController.modifyPriority(priority.getName(), newPriorityName)) {
                showErrorAlert("Error", "Failed to rename priority.");
            } else {
                updatePriorityList(); 
                mainContentPane.refreshTaskTable();
            }
        });
    }

    private void deleteSelectedPriority(Priority priority) {
        System.out.println("deleteSelectedPriority called");
        if (priority == null) {
            showErrorAlert("No Selection", "Please select a priority to delete.");
            return;
        }

        if ("Default".equalsIgnoreCase(priority.getName())) {
            showErrorAlert("Invalid Operation", "'Default' priority cannot be deleted.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Priority");
        confirmation.setHeaderText("Are you sure you want to delete the priority: " + priority.getName() + "?");
        confirmation.setContentText("Tasks associated with this priority will be reassigned to 'Default'.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (!priorityController.deletePriority(priority.getName())) {
                    showErrorAlert("Error", "Failed to delete priority.");
                } else {
                    updatePriorityList();  
                    mainContentPane.refreshTaskTable();
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
