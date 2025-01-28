package app;

import app.controllers.ReminderController;
import app.controllers.TaskController;
import app.models.Reminder;
import app.models.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Popup;
import javafx.stage.Stage;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.List;

public class SummaryPane extends HBox {
    private TaskController taskController;
    private ReminderController reminderController;
    private Label bellIcon;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yy");

    public SummaryPane(TaskController taskController, ReminderController reminderController) {
        this.taskController = taskController;
        this.reminderController = reminderController;

        setSpacing(20);
        setPadding(new Insets(20));
        setAlignment(Pos.CENTER_LEFT);
        setStyle("-fx-background-color: #f9f9f9;");

        HBox pillContainer = new HBox(
                createPill("Total Tasks", "0", "#000000", "#000000"),
                createPill("Completed", "0", "#000000", "#2ECC71"),
                createPill("Delayed", "0", "#000000", "#E74C3C"),
                createPill("Upcoming", "0", "#000000", "#9B59B6")
        );
        pillContainer.setSpacing(20);

        
        bellIcon = new Label("\uD83D\uDD14");  
        bellIcon.setStyle("-fx-font-size: 30px; -fx-text-fill: green; -fx-cursor: hand;");
        Tooltip.install(bellIcon, new Tooltip("No reminders due today"));

        bellIcon.setOnMouseClicked(event -> toggleNotificationPopup());

        
        HBox.setHgrow(pillContainer, Priority.ALWAYS);
        getChildren().addAll(pillContainer, bellIcon);

        updateSummary();
        checkReminders();
    }

    private StackPane createPill(String title, String value, String backgroundColor, String badgeColor) {
        Label titleLabel = new Label(title);
        titleLabel.setFont(new Font("Arial", 14));
        titleLabel.setTextFill(Color.web(backgroundColor));

        Label badgeLabel = new Label(value);
        badgeLabel.setFont(new Font("Arial", 14));
        badgeLabel.setStyle("-fx-font-weight: bold;");
        badgeLabel.setTextFill(Color.web("#FFFFFF"));
        badgeLabel.setPadding(new Insets(2, 8, 2, 8));
        badgeLabel.setStyle("-fx-background-color: " + badgeColor + "; -fx-background-radius: 20;");

        HBox pillContent = new HBox(titleLabel, badgeLabel);
        pillContent.setSpacing(10);
        pillContent.setAlignment(Pos.CENTER_LEFT);

        StackPane pill = new StackPane(pillContent);
        pill.setStyle("-fx-border-color: " + backgroundColor + "; -fx-border-width: 2; -fx-border-radius: 25;");
        pill.setPadding(new Insets(5, 15, 5, 15));
        return pill;
    }

    public void updateSummary() {
        HBox pillContainer = (HBox) getChildren().get(0);

        ((Label) ((HBox) ((StackPane) pillContainer.getChildren().get(0)).getChildren().get(0)).getChildren().get(1))
                .setText(String.valueOf(taskController.getTotalTasks()));
        ((Label) ((HBox) ((StackPane) pillContainer.getChildren().get(1)).getChildren().get(0)).getChildren().get(1))
                .setText(String.valueOf(taskController.getCompletedTasks()));
        ((Label) ((HBox) ((StackPane) pillContainer.getChildren().get(2)).getChildren().get(0)).getChildren().get(1))
                .setText(String.valueOf(taskController.getDelayedTasks()));
        ((Label) ((HBox) ((StackPane) pillContainer.getChildren().get(3)).getChildren().get(0)).getChildren().get(1))
                .setText(String.valueOf(taskController.getUpcomingTasks()));

        checkReminders();
    }

    private void checkReminders() {
        List<Reminder> remindersDueToday = reminderController.getReminders().stream()
                .filter(reminder -> reminder.getReminderDate().equals(LocalDate.now()))
                .toList();

        if (!remindersDueToday.isEmpty()) {
            bellIcon.setStyle("-fx-font-size: 30px; -fx-text-fill: red; -fx-cursor: hand;");
            Tooltip tooltip = new Tooltip("Reminders Due Today: Click to view");
            Tooltip.install(bellIcon, tooltip);
        } else {
            bellIcon.setStyle("-fx-font-size: 30px; -fx-text-fill: green; -fx-cursor: hand;");
            Tooltip.install(bellIcon, new Tooltip("No reminders due today"));
        }
    }

    private void toggleNotificationPopup() {
        showPopup(); 
    }
    

    private void showPopup() {
    Stage popupStage = new Stage();
    popupStage.setTitle("Today's Reminders");

    // Root container for the popup window
    VBox popupRoot = new VBox();
    popupRoot.setPadding(new Insets(20));
    popupRoot.setSpacing(15);
    popupRoot.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #d1d1d1; " +
            "-fx-border-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

    
    Label titleLabel = new Label("Reminders for Today");
    titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333333;");
    popupRoot.getChildren().add(titleLabel);

    
    List<Reminder> remindersDueToday = reminderController.getReminders().stream()
            .filter(reminder -> reminder.getReminderDate().equals(LocalDate.now()))
            .toList();

    if (remindersDueToday.isEmpty()) {
        Label noReminderLabel = new Label("You have no reminders due today.");
        noReminderLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7a7a7a;");
        popupRoot.getChildren().add(noReminderLabel);
    } else {
        remindersDueToday.forEach(reminder -> {
            Task task = taskController.getTaskById(reminder.getTaskId());
            String taskName = (task != null) ? task.getTitle() : "Unknown Task";

            VBox reminderItem = new VBox();
            reminderItem.setPadding(new Insets(10));
            reminderItem.setSpacing(5);
            reminderItem.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #d1d1d1; -fx-border-radius: 5;");

            Label taskLabel = new Label("Task: " + taskName);
            taskLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

            Label dateLabel = new Label("Reminder Date: " + reminder.getReminderDate().format(dateFormatter));
            dateLabel.setStyle("-fx-font-size: 14px;");

            Label descriptionLabel = new Label("Description: " + reminder.getDescription());
            descriptionLabel.setStyle("-fx-font-size: 14px;");

            reminderItem.getChildren().addAll(taskLabel, dateLabel, descriptionLabel);
            popupRoot.getChildren().add(reminderItem);
        });
    }

    
    ScrollPane scrollPane = new ScrollPane(popupRoot);
    scrollPane.setFitToWidth(true);
    scrollPane.setPrefWidth(550);
    scrollPane.setPrefHeight(500);
    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

    
    Scene popupScene = new Scene(scrollPane, 550, 600);
    popupStage.setScene(popupScene);
    popupStage.initModality(javafx.stage.Modality.APPLICATION_MODAL); 
    popupStage.show();
}
    
}
