package app;

import app.controllers.TaskController;
import app.controllers.CategoryController;
import app.controllers.PriorityController;
import app.controllers.ReminderController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import app.models.Task;

public class Main extends Application {
    private static final String TASKS_FILE = "medialab/tasks.json";
    private static final String CATEGORIES_FILE = "medialab/categories.json";
    private static final String PRIORITIES_FILE = "medialab/priorities.json";
    private static final String REMINDERS_FILE = "medialab/reminders.json";

    private TaskController taskController;
    private CategoryController categoryController;
    private PriorityController priorityController;
    private ReminderController reminderController;

    @Override
    public void start(Stage primaryStage) {
        reminderController = new ReminderController(null);
        taskController = new TaskController(reminderController);
        categoryController = new CategoryController(taskController);
        priorityController = new PriorityController(taskController);
        reminderController = new ReminderController(taskController);

        categoryController.loadCategories(CATEGORIES_FILE);
        priorityController.loadPriorities(PRIORITIES_FILE);
        taskController.loadTasks(TASKS_FILE);
        reminderController.loadReminders(REMINDERS_FILE);

        taskController.updateOverdueTasks();
        System.out.println("Delayed Tasks Updated");

        /*long delayedTasks = taskController.getTasks().stream()
                .filter(task -> task.getStatus() == Task.Status.DELAYED)
                .count();

        if (delayedTasks > 0) {
            showPopup("You have " + delayedTasks + " overdue tasks.");
        }*/

        BorderPane root = new BorderPane();

        SummaryPane summaryPane = new SummaryPane(taskController, reminderController);
        ReminderManagementPane reminderPane = new ReminderManagementPane(reminderController, taskController);
    
    MainContentPane taskPane = new MainContentPane(taskController, categoryController,priorityController, summaryPane,reminderController,reminderPane);
    CategoryManagementPane categoryPane = new CategoryManagementPane(categoryController, taskController, summaryPane) {
        @Override
                public void refreshTaskTable() {
                    taskPane.refreshTaskTable();
                }
    };
    PriorityManagementPane priorityPane = new PriorityManagementPane(priorityController,taskPane);
    //ReminderManagementPane reminderPane = new ReminderManagementPane(reminderController,taskController);

    
    SidebarPane sidebar = new SidebarPane(
            () -> root.setCenter(taskPane),       
            () -> root.setCenter(categoryPane),   
            () -> root.setCenter(priorityPane),
            () -> root.setCenter(reminderPane)  
    );

    root.setTop(summaryPane);
    root.setLeft(sidebar);
    root.setCenter(taskPane);

    // Scene setup
    Scene scene = new Scene(root, 1200, 800);
    primaryStage.setTitle("MediaLab Assistant");
    primaryStage.setScene(scene);
    primaryStage.show();
}

    private void showPopup(String message) {
        Stage popupStage = new Stage();
        popupStage.setTitle("Notification");
        BorderPane pane = new BorderPane();
        pane.setCenter(new javafx.scene.control.Label(message));
        Scene scene = new Scene(pane, 300, 100);
        popupStage.setScene(scene);
        popupStage.show();
    }

    @Override
    public void stop() {
        categoryController.saveCategories(CATEGORIES_FILE);
        priorityController.savePriorities(PRIORITIES_FILE);
        taskController.saveTasks(TASKS_FILE);
        reminderController.saveReminders(REMINDERS_FILE);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
