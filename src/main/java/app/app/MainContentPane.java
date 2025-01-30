package app;

import app.controllers.TaskController;
import app.controllers.CategoryController;
import app.controllers.PriorityController;
import app.controllers.ReminderController;
import app.models.Category;
import app.models.Priority;
import app.models.Reminder;
import app.models.Task;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.time.LocalDate;

public class MainContentPane extends BorderPane {
    private final TaskController taskController;
    private final CategoryController categoryController;
    private final ObservableList<Task> taskList;
    private final PriorityController priorityController;
    private final SummaryPane summaryPane;
    private final ReminderController reminderController;
    private final ReminderManagementPane reminderManagementPane;
    private final FilteredList<Task> filteredTasks;
    
    
        public MainContentPane(TaskController taskController,CategoryController categoryController, PriorityController priorityController, SummaryPane summaryPane, ReminderController reminderController, ReminderManagementPane reminderManagementPane) {
            this.taskController = taskController;
            this.categoryController = categoryController; 
            this.summaryPane = summaryPane;
            this.reminderController = reminderController;
            this.taskList = FXCollections.observableArrayList(taskController.getTasks());
            this.priorityController = priorityController;
            this.reminderManagementPane = reminderManagementPane;
            this.filteredTasks = new FilteredList<>(taskList, task -> true);

        Button addTaskButton = new Button("+ Add New Task");
        addTaskButton.setOnAction(e -> showAddTaskDialog(summaryPane));

        TextField titleSearchField = new TextField();
        titleSearchField.setPromptText("Search by Title");

        TextField categorySearchField = new TextField();
        categorySearchField.setPromptText("Search by Category");

        TextField prioritySearchField = new TextField();
        prioritySearchField.setPromptText("Search by Priority");

        titleSearchField.textProperty().addListener((obs, oldValue, newValue) -> filterTasks(titleSearchField, categorySearchField, prioritySearchField));
        categorySearchField.textProperty().addListener((obs, oldValue, newValue) -> filterTasks(titleSearchField, categorySearchField, prioritySearchField));
        prioritySearchField.textProperty().addListener((obs, oldValue, newValue) -> filterTasks(titleSearchField, categorySearchField, prioritySearchField));
        
        
        //Button addCategoryButton = new Button("+ Add Category");
        //addCategoryButton.setOnAction(e -> showAddCategoryDialog());


        HBox header = new HBox(10, addTaskButton, titleSearchField, categorySearchField, prioritySearchField);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #dddddd;");
        TableView<Task> taskTable = createTaskTable();

        setTop(header);
        setCenter(taskTable);
    }


    private void filterTasks(TextField titleField, TextField categoryField, TextField priorityField) {
        String titleFilter = titleField.getText().toLowerCase().trim();
        String categoryFilter = categoryField.getText().toLowerCase().trim();
        String priorityFilter = priorityField.getText().toLowerCase().trim();

        filteredTasks.setPredicate(task -> {
            boolean matchesTitle = titleFilter.isEmpty() || task.getTitle().toLowerCase().contains(titleFilter);
            boolean matchesCategory = categoryFilter.isEmpty() || 
                    (task.getCategory() != null && task.getCategory().getName().toLowerCase().contains(categoryFilter));
            boolean matchesPriority = priorityFilter.isEmpty() || task.getPriority().toLowerCase().contains(priorityFilter);

            return matchesTitle && matchesCategory && matchesPriority;
        });
    }



    private TableView<Task> createTaskTable() {
        TableView<Task> tableView = new TableView<>(filteredTasks);
    
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yy");
    
        TableColumn<Task, String> titleColumn = new TableColumn<>("Task");
        titleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        titleColumn.setPrefWidth(200);
    
        TableColumn<Task, String> deadlineColumn = new TableColumn<>("Deadline");
        deadlineColumn.setCellValueFactory(data -> {
            LocalDate deadline = data.getValue().getDeadline();
            return new SimpleStringProperty(deadline != null ? deadline.format(dateFormatter) : "No Deadline");
        });
        deadlineColumn.setPrefWidth(150);
    
        TableColumn<Task, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    if (status.equals(Task.Status.DELAYED.name())) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else if(status.equals(Task.Status.COMPLETED.name())){
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if(status.equals(Task.Status.POSTPONED.name())){
                        setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
                    } else if(status.equals(Task.Status.OPEN.name())){
                        setStyle("-fx-text-fill: purple; -fx-font-weight: bold;");
                    } else if(status.equals(Task.Status.IN_PROGRESS.name())){
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    }
                    else {
                        setStyle("");
                    }
                }
            }
        });
        statusColumn.setPrefWidth(150);
    
        TableColumn<Task, String> priorityColumn = new TableColumn<>("Priority");
        priorityColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPriority()));
        priorityColumn.setPrefWidth(150);
    
        TableColumn<Task, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(data -> {
            if (data.getValue().getCategory() != null) {
                return new SimpleStringProperty(data.getValue().getCategory().getName());
            } else {
                return new SimpleStringProperty("None");
            }
        });
        categoryColumn.setPrefWidth(150);

        TableColumn<Task, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));
        descriptionColumn.setPrefWidth(200);
    
        tableView.getColumns().addAll(titleColumn, deadlineColumn, statusColumn, priorityColumn, categoryColumn,descriptionColumn);
    
        // Add double-click listener to rows
        tableView.setRowFactory(tv -> {
            TableRow<Task> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Task rowData = row.getItem();
                    showEditTaskDialog(rowData,summaryPane, reminderController,reminderManagementPane);
                }
            });
            return row;
        });
    
        return tableView;
    }
    

    private void showEditTaskDialog(Task task, SummaryPane summaryPane, ReminderController reminderController,ReminderManagementPane reminderPane) {
        if (task == null) {
            showErrorAlert("No Task Selected", "Please select a task to edit.");
            return;
        }
    
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Edit Task");
    
        TextField titleField = new TextField(task.getTitle());
        titleField.setPromptText("Title");
    
        TextArea descriptionField = new TextArea(task.getDescription());
        descriptionField.setPromptText("Description (Optional)");
    
        ComboBox<String> priorityComboBox = new ComboBox<>();
        priorityComboBox.setPromptText("Select Priority");
        priorityComboBox.setItems(FXCollections.observableArrayList(
                priorityController.getPriorities().stream().map(Priority::getName).toList()
        ));
        priorityComboBox.setValue(task.getPriority());
    
        DatePicker deadlinePicker = new DatePicker(task.getDeadline());
        deadlinePicker.setPromptText("Select Deadline");
        deadlinePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
                }
            }
        });
    
        ComboBox<String> categoryComboBox = new ComboBox<>();
        categoryComboBox.setPromptText("Select Category");
        categoryComboBox.setItems(FXCollections.observableArrayList(
                categoryController.getCategories().stream().map(Category::getName).toList()
        ));
        categoryComboBox.setValue(task.getCategory() != null ? task.getCategory().getName() : null);
    
        ComboBox<Task.Status> statusComboBox = new ComboBox<>();
        statusComboBox.setPromptText("Select Status");
        statusComboBox.setItems(FXCollections.observableArrayList(
                Arrays.stream(Task.Status.values())
                        .filter(status -> status != Task.Status.DELAYED)
                        .toList()
        ));
        statusComboBox.setValue(task.getStatus());
    
        
        Label binIcon = new Label("\uD83D\uDDD1"); 
        binIcon.setStyle("-fx-font-size: 18px; -fx-text-fill: red; -fx-cursor: hand;");
        binIcon.setOnMouseClicked(event -> {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Delete Task");
            confirmation.setHeaderText("Are you sure you want to delete this task?");
            confirmation.setContentText("This action will also delete all associated reminders.");
    
            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    taskController.deleteTask(task.getId()); 
                    reminderController.deleteRemindersForTask(task.getId()); 
                    reminderManagementPane.refreshAfterTaskDeletion(task.getId());
                    refreshTaskTable(); 
                    summaryPane.updateSummary(); 
                    dialog.close(); 
                }
            });
        });
    
        Label bellIcon = new Label("\uD83D\uDD14"); 
        bellIcon.setStyle("-fx-font-size: 18px; -fx-text-fill: green; -fx-cursor: hand;");
        bellIcon.setOnMouseClicked(event -> showAddReminderDialog(task, reminderController));
    
        HBox header = new HBox(new Label(), bellIcon, binIcon);
        header.setSpacing(10);
        header.setAlignment(Pos.CENTER_RIGHT);
    
        VBox dialogContent = new VBox(10,
                header,
                new Label("Title:"), titleField,
                new Label("Description (Optional):"), descriptionField,
                new Label("Priority:"), priorityComboBox,
                new Label("Deadline:"), deadlinePicker,
                new Label("Category:"), categoryComboBox,
                new Label("Status:"), statusComboBox);
        dialogContent.setPadding(new Insets(10));
    
        dialog.getDialogPane().setContent(dialogContent);
    
    
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    
        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                if (titleField.getText().trim().isEmpty() ||
                    priorityComboBox.getValue() == null ||
                    deadlinePicker.getValue() == null ||
                    statusComboBox.getValue() == null) {
                    showErrorAlert("Validation Error", "All required fields must be filled!");
                    return null;
                }
    
        
                String selectedCategoryName = categoryComboBox.getValue();
                Category selectedCategory = categoryController.getCategories().stream()
                        .filter(category -> category.getName().equals(selectedCategoryName))
                        .findFirst()
                        .orElse(null);
    
            
                task.setTitle(titleField.getText().trim());
                task.setDescription(descriptionField.getText().trim());
                task.setPriority(priorityComboBox.getValue());
                task.setDeadline(deadlinePicker.getValue());
                task.setCategory(selectedCategory);
    
                Task.Status selectedStatus = statusComboBox.getValue();
                if (selectedStatus == Task.Status.COMPLETED) {
                    taskController.updateTaskStatus(task.getId(), Task.Status.COMPLETED); 
                    reminderController.deleteRemindersForTask(task.getId()); 
                    reminderManagementPane.refreshAfterTaskDeletion(task.getId());
                } else {
                    task.setStatus(selectedStatus);
                }
                refreshTaskTable(); 
                summaryPane.updateSummary(); 
            }
            return null;
        });
    
        dialog.showAndWait();
    }
    
    
    


    
    
    
    
    /*private void deleteSelectedTask(Task task, SummaryPane summaryPane) {
        if (task == null) {
            showErrorAlert("No Task Selected", "Please select a task to delete.");
            return;
        }
    
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Task");
        confirmation.setHeaderText("Are you sure you want to delete the task: " + task.getTitle() + "?");
        confirmation.setContentText("This action cannot be undone.");
    
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (taskController.deleteTask(task.getId())) {
                    refreshTaskTable(); 
                    summaryPane.updateSummary(); 
                    reminderManagementPane.refreshAfterTaskDeletion(task.getId());
                } else {
                    showErrorAlert("Error", "Failed to delete task.");
                }
            }
        });
    }
    */
    
    private void showAddReminderDialog(Task task, ReminderController reminderController) {
        if (task == null) {
            showErrorAlert("No Task Selected", "Please select a task to add a reminder.");
            return;
        }
    
        if (task.getStatus() == Task.Status.COMPLETED) {
            showErrorAlert("Invalid Operation", "Cannot add reminders to a completed task.");
            return;
        }
    
        if (task.getStatus() == Task.Status.DELAYED) {
            showErrorAlert("Invalid Operation", "Cannot add reminders to a delayed task.");
            return;
        }
    
        Dialog<Reminder> dialog = new Dialog<>();
        dialog.setTitle("Add Reminder - " + task.getTitle());
    
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
    
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Reminder Description (Optional)");
    
        reminderTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("Custom Date".equals(newVal)) {
                customDatePicker.setDisable(false);
            } else {
                customDatePicker.setDisable(true);
            }
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
                    showErrorAlert("Validation Error", "You must select a reminder type.");
                    return null;
                }
    
                String description = descriptionField.getText().trim();
    
                switch (reminderType) {
                    case "1 Day Before Deadline":
                        reminderDate[0] = task.getDeadline().minusDays(1);
                        break;
                    case "1 Week Before Deadline":
                        reminderDate[0] = task.getDeadline().minusWeeks(1);
                        break;
                    case "1 Month Before Deadline":
                        reminderDate[0] = task.getDeadline().minusMonths(1);
                        break;
                    case "Custom Date":
                        reminderDate[0] = customDatePicker.getValue();
                        break;
                    default:
                        showErrorAlert("Validation Error", "Invalid reminder type.");
                        return null;
                }
    
                if (reminderDate[0] == null || reminderDate[0].isBefore(LocalDate.now())) {
                    showErrorAlert("Validation Error", "Reminder date cannot be in the past.");
                    return null;
                }
    
                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Set Reminder Confirmation");
                confirmation.setHeaderText("Are you sure you want to set this reminder?");
                confirmation.setContentText("Reminder: " + reminderType +
                                             (reminderType.equals("Custom Date") ?
                                                     (" on " + reminderDate[0].toString()) : ""));
    
                confirmation.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        reminderController.addReminder(task.getId(), reminderDate[0], description);
                        reminderManagementPane.refreshReminderTable();
    
                        Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                        infoAlert.setTitle("Reminder Set");
                        infoAlert.setHeaderText(null);
                        infoAlert.setContentText("Reminder set successfully for " + reminderType + ".");
                        infoAlert.showAndWait();
                    }
                });
    
                return null; 
            }
            return null;
        });
    
        dialog.showAndWait();
    }
    


    private void showAddTaskDialog(SummaryPane summaryPane) {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Add New Task");
    
        TextField titleField = new TextField();
        titleField.setPromptText("Title");
    
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Description (Optional)");
    
        ComboBox<String> priorityComboBox = new ComboBox<>();
        priorityComboBox.setPromptText("Select Priority");
        priorityComboBox.setItems(FXCollections.observableArrayList(
                priorityController.getPriorities().stream().map(Priority::getName).toList()
        ));
        priorityComboBox.setValue("Default"); 
    
        DatePicker deadlinePicker = new DatePicker();
        deadlinePicker.setPromptText("Select Deadline");
        deadlinePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;"); 
                }
            }
        });
    
        ComboBox<String> categoryComboBox = new ComboBox<>();
        categoryComboBox.setPromptText("Select Category");
        categoryComboBox.setItems(FXCollections.observableArrayList(
                categoryController.getCategories().stream().map(Category::getName).toList()
        ));
    
        VBox dialogContent = new VBox(10,
                new Label("Title:"), titleField,
                new Label("Description (Optional):"), descriptionField,
                new Label("Priority:"), priorityComboBox,
                new Label("Deadline:"), deadlinePicker,
                new Label("Category:"), categoryComboBox);
        dialogContent.setPadding(new Insets(10));
    
        dialog.getDialogPane().setContent(dialogContent);
    
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    
        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                if (titleField.getText().trim().isEmpty() || 
                    priorityComboBox.getValue() == null || 
                    deadlinePicker.getValue() == null) {
                    showErrorAlert("Validation Error", "All required fields must be filled!");
                    return null;
                }
    
                String selectedCategoryName = categoryComboBox.getValue();
                Category selectedCategory = categoryController.getCategories().stream()
                        .filter(category -> category.getName().equals(selectedCategoryName))
                        .findFirst()
                        .orElse(null);
    
                Task newTask = new Task(
                        titleField.getText().trim(),
                        descriptionField.getText().trim(),
                        selectedCategory,
                        priorityComboBox.getValue(),
                        deadlinePicker.getValue(),
                        Task.Status.OPEN
                );
    
                taskController.addTask(newTask);
                taskList.add(newTask);
                summaryPane.updateSummary();
    
                return newTask;
            }
            return null;
        });
    
        dialog.showAndWait();
    }
    
    public void refreshTaskTable() {
        taskList.setAll(taskController.getTasks());
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
