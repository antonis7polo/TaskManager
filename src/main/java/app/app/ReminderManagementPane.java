package app;

import app.controllers.ReminderController;
import app.models.Reminder;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import app.models.Task;
import app.controllers.TaskController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ReminderManagementPane extends BorderPane {

    private ReminderController reminderController;
    private TableView<Reminder> reminderTable;
    private TaskController taskController;
    private ObservableList<Reminder> reminderList;

    public ReminderManagementPane(ReminderController reminderController,TaskController taskController) {
        this.reminderController = reminderController;
        this.taskController = taskController;
        this.reminderList = FXCollections.observableArrayList(reminderController.getReminders());

        // Create reminder table
        reminderTable = createReminderTable();

        // Add buttons
        Button modifyReminderButton = new Button("Edit Reminder");
        modifyReminderButton.setOnAction(e -> modifySelectedReminder());

        Button deleteReminderButton = new Button("Delete Reminder");
        deleteReminderButton.setOnAction(e -> deleteSelectedReminder());

        // Disable buttons by default until a selection is made
        modifyReminderButton.setDisable(true);
        deleteReminderButton.setDisable(true);

        // Enable buttons when a selection is made
        reminderTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean disable = newSelection == null;
            modifyReminderButton.setDisable(disable);
            deleteReminderButton.setDisable(disable);
        });

        HBox buttonBar = new HBox(10, modifyReminderButton, deleteReminderButton);
        buttonBar.setPadding(new Insets(10));

        setTop(buttonBar);  // Buttons at the top
        setCenter(reminderTable);  // Table in center
        
    }

    
    private TableView<Reminder> createReminderTable() {
        TableView<Reminder> table = new TableView<>(reminderList);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yy");

        TableColumn<Reminder, String> taskColumn = new TableColumn<>("Task");
        taskColumn.setCellValueFactory(data -> {
            Task task = taskController.getTaskById(data.getValue().getTaskId());
            return new javafx.beans.property.SimpleStringProperty(
                task != null ? task.getTitle() : "Task Not Found");
        });
        taskColumn.setPrefWidth(200);

        TableColumn<Reminder, String> dateColumn = new TableColumn<>("Reminder Date");
        dateColumn.setCellValueFactory(data -> {
        LocalDate reminderDate = data.getValue().getReminderDate();
        return new SimpleStringProperty(reminderDate != null ? reminderDate.format(dateFormatter) : "No Date");
    });
    dateColumn.setPrefWidth(150);

        TableColumn<Reminder, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDescription()));
        descriptionColumn.setPrefWidth(200);

        table.getColumns().addAll(taskColumn, dateColumn, descriptionColumn);
        table.setPrefHeight(400);

        table.setRowFactory(tv -> {
            TableRow<Reminder> row = new TableRow<>();
            row.setOnMouseEntered(event -> {
                if (!row.isEmpty()) {
                    row.setStyle("-fx-background-color: #e0e0e0;");
                    Task task = taskController.getTaskById(row.getItem().getTaskId());
                    row.setTooltip(new Tooltip(task != null ? "Task: " + task.getTitle() : "Task Not Found"));
                }
            });
            row.setOnMouseExited(event -> row.setStyle(""));

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Reminder reminder = row.getItem();
                    Task task = taskController.getTaskById(reminder.getTaskId());
                    if (task != null) {
                        showTaskDetails(task);
                    }
                }
            });
            return row;
        });

        return table;
    }

    private void showTaskDetails(Task task) {
        Alert taskDetails = new Alert(Alert.AlertType.INFORMATION);
        taskDetails.setTitle("Task Details");
        taskDetails.setHeaderText(task.getTitle());
        taskDetails.setContentText("Description: " + task.getDescription() +
                "\nPriority: " + task.getPriority() +
                "\nDeadline: " + (task.getDeadline() != null ? task.getDeadline() : "None") +
                "\nStatus: " + task.getStatus());
        taskDetails.showAndWait();
    }
    
    /*  private void showAddReminderDialog() {
        Dialog<Reminder> dialog = new Dialog<>();
        dialog.setTitle("Add Reminder");

        TextField taskIdField = new TextField();
        taskIdField.setPromptText("Task ID");

        ComboBox<String> reminderTypeComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "1 Day Before Deadline",
                "1 Week Before Deadline",
                "1 Month Before Deadline",
                "Custom Date"
        ));
        reminderTypeComboBox.setPromptText("Select Reminder Type");

        DatePicker customDatePicker = new DatePicker();
        customDatePicker.setPromptText("Custom Reminder Date");
        customDatePicker.setDisable(true);

        reminderTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            customDatePicker.setDisable(!"Custom Date".equals(newVal));
        });

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description (Optional)");

        VBox dialogContent = new VBox(10,
                new Label("Task ID:"), taskIdField,
                new Label("Reminder Type:"), reminderTypeComboBox,
                new Label("Custom Date (if applicable):"), customDatePicker,
                new Label("Description (Optional):"), descriptionField
        );
        dialogContent.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(dialogContent);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                String taskId = taskIdField.getText().trim();
                String reminderType = reminderTypeComboBox.getValue();
                String description = descriptionField.getText().trim();
                LocalDate reminderDate = null;

                if (reminderType == null || taskId.isEmpty()) {
                    showErrorAlert("Validation Error", "Task ID and Reminder Type are required.");
                    return null;
                }

                switch (reminderType) {
                    case "1 Day Before Deadline":
                        reminderDate = LocalDate.now().plusDays(1);
                        break;
                    case "1 Week Before Deadline":
                        reminderDate = LocalDate.now().plusWeeks(1);
                        break;
                    case "1 Month Before Deadline":
                        reminderDate = LocalDate.now().plusMonths(1);
                        break;
                    case "Custom Date":
                        reminderDate = customDatePicker.getValue();
                        break;
                }

                if (reminderDate == null || reminderDate.isBefore(LocalDate.now())) {
                    showErrorAlert("Validation Error", "Invalid reminder date.");
                    return null;
                }

                boolean added = reminderController.addReminder(taskId, reminderDate, description);
                if (added) {
                    reminderList.setAll(reminderController.getReminders());
                    showInformationAlert("Reminder Added", "Reminder added successfully.");
                } else {
                    showErrorAlert("Error", "Failed to add reminder.");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }
        */

    private void deleteSelectedReminder() {
        Reminder selectedReminder = reminderTable.getSelectionModel().getSelectedItem();
        if (selectedReminder == null) {
            showErrorAlert("No Selection", "Please select a reminder to delete.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Reminder");
        confirmation.setHeaderText("Are you sure you want to delete this reminder?");
        confirmation.setContentText("This action will delete only the selected reminder.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean deleted = reminderController.deleteReminder(selectedReminder.getId());
                if (deleted) {
                    reminderList.setAll(reminderController.getReminders());
                    reminderTable.refresh();
                    showInformationAlert("Reminder Deleted", "The selected reminder has been deleted.");
                } else {
                    showErrorAlert("Error", "Failed to delete reminder.");
                }
            }
        });
    }



    private void modifySelectedReminder() {
        Reminder selectedReminder = reminderTable.getSelectionModel().getSelectedItem();
        if (selectedReminder == null) {
            showErrorAlert("No Selection", "Please select a reminder to modify.");
            return;
        }
    
        Task associatedTask = taskController.getTaskById(selectedReminder.getTaskId());
        if (associatedTask == null) {
            showErrorAlert("Task Not Found", "The task associated with this reminder could not be found.");
            return;
        }
    
        if (associatedTask.getStatus() == Task.Status.COMPLETED) {
            showErrorAlert("Invalid Operation", "Cannot edit reminders for completed tasks.");
            return;
        }
    
        Dialog<Reminder> dialog = new Dialog<>();
        dialog.setTitle("Modify Reminder");
        dialog.setHeaderText("Edit the reminder details below for " + associatedTask.getTitle());
    
        // ComboBox for Reminder Type
        ComboBox<String> reminderTypeComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "1 Day Before Deadline",
                "1 Week Before Deadline",
                "1 Month Before Deadline",
                "Custom Date"
        ));
        reminderTypeComboBox.setPromptText("Select Reminder Type");
    
        // DatePicker for Custom Date
        DatePicker customDatePicker = new DatePicker(selectedReminder.getReminderDate());
        customDatePicker.setPromptText("Custom Reminder Date");
        customDatePicker.setDisable(true);  // Initially disabled
    
        // TextField for Description
        TextField descriptionField = new TextField(selectedReminder.getDescription());
        descriptionField.setPromptText("Reminder Description (Optional)");
    
        // Enable DatePicker for Custom Date
        reminderTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            customDatePicker.setDisable(!"Custom Date".equals(newVal));
        });
    
        VBox dialogContent = new VBox(10,
                new Label("Reminder Type:"), reminderTypeComboBox,
                new Label("Custom Date (if applicable):"), customDatePicker,
                new Label("Description (Optional):"), descriptionField
        );
        dialogContent.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(dialogContent);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    
        final LocalDate[] reminderDate = new LocalDate[1];
        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                String reminderType = reminderTypeComboBox.getValue();
    
                if (reminderType == null) {
                    showErrorAlert("Validation Error", "Please select a reminder type.");
                    return null;
                }
    
                String description = descriptionField.getText().trim();
    
                // Calculate the Reminder Date Based on Type
                switch (reminderType) {
                    case "1 Day Before Deadline":
                        reminderDate[0] = associatedTask.getDeadline().minusDays(1);
                        break;
                    case "1 Week Before Deadline":
                        reminderDate[0] = associatedTask.getDeadline().minusWeeks(1);
                        break;
                    case "1 Month Before Deadline":
                        reminderDate[0] = associatedTask.getDeadline().minusMonths(1);
                        break;
                    case "Custom Date":
                        reminderDate[0] = customDatePicker.getValue();
                        break;
                    default:
                        showErrorAlert("Validation Error", "Invalid reminder type selected.");
                        return null;
                }
    
                // Ensure the Reminder Date is Valid
                if (reminderDate[0] == null || reminderDate[0].isBefore(LocalDate.now())) {
                    showErrorAlert("Invalid Date", "Reminder date cannot be in the past.");
                    return null;
                }
    
                // Confirmation Alert
                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Modify Reminder Confirmation");
                confirmation.setHeaderText("Are you sure you want to modify this reminder?");
                confirmation.setContentText("Reminder: " + reminderType +
                        (reminderType.equals("Custom Date") ?
                                (" on " + reminderDate[0].toString()) : ""));
    
                Optional<ButtonType> result = confirmation.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    // Update the Reminder
                    selectedReminder.setReminderDate(reminderDate[0]);
                    selectedReminder.setDescription(description);
    
                    reminderList.setAll(reminderController.getReminders());
                    reminderTable.refresh();
    
                    // Save Changes to JSON
                    reminderController.saveReminders("medialab/reminders.json");
    
                    showInformationAlert("Success", "Reminder modified successfully.");
                }
            }
            return null;
        });
    
        dialog.showAndWait();
    }
    

    public void refreshAfterTaskDeletion(String taskId) {
        reminderList.removeIf(reminder -> reminder.getTaskId().equals(taskId));
        reminderController.deleteRemindersForTask(taskId);
        reminderTable.refresh();
    }


    public void refreshReminderTable() {
        reminderList.setAll(reminderController.getReminders());
        reminderTable.refresh();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInformationAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
