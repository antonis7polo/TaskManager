package app;

import app.controllers.ReminderController;
import app.controllers.TaskController;
import app.models.Reminder;
import app.models.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Popup;
import java.time.LocalDate;
import java.util.List;

public class SummaryPane extends HBox {
    private TaskController taskController;
    private ReminderController reminderController;
    private Label bellIcon;
    private Popup notificationPopup;
    private boolean isPopupVisible = false;

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

        // Bell Icon with Hover Effect
        bellIcon = new Label("\uD83D\uDD14");  // Bell emoji
        bellIcon.setStyle("-fx-font-size: 30px; -fx-text-fill: green; -fx-cursor: hand;");
        Tooltip.install(bellIcon, new Tooltip("No reminders due today"));

        bellIcon.setOnMouseClicked(event -> toggleNotificationPopup());

        notificationPopup = new Popup();

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

    // Toggle the visibility of the notification popup
    private void toggleNotificationPopup() {
        if (isPopupVisible) {
            notificationPopup.hide();
            isPopupVisible = false;
        } else {
            showNotificationPopup();
        }
    }

    private void showNotificationPopup() {
        VBox notificationBox = new VBox();
        notificationBox.setPadding(new Insets(20));
        notificationBox.setSpacing(15);
        notificationBox.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #d1d1d1; " +
                "-fx-border-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        notificationBox.setMinWidth(1000);  // Wider width to prevent cramped look
    
        List<Reminder> remindersDueToday = reminderController.getReminders().stream()
                .filter(reminder -> reminder.getReminderDate().equals(LocalDate.now()))
                .toList();
    
        if (remindersDueToday.isEmpty()) {
            Label noReminderLabel = new Label("No imminent reminders");
            noReminderLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7a7a7a;");
            noReminderLabel.setWrapText(true);
            noReminderLabel.setAlignment(Pos.CENTER);
            notificationBox.getChildren().add(noReminderLabel);
        } else {
            remindersDueToday.forEach(reminder -> {
                Task task = taskController.getTaskById(reminder.getTaskId());
                String taskName = task != null ? task.getTitle() : "Unknown Task";
    
                VBox reminderItem = new VBox();
                reminderItem.setPadding(new Insets(10));
                reminderItem.setStyle("-fx-background-color: #f6f6f6; -fx-border-color: #d1d1d1; -fx-border-radius: 5;");
                
                Label reminderLabel = new Label("• " + taskName + "\n" + reminder.getDescription());
                reminderLabel.setStyle("-fx-font-size: 15px;");
                reminderLabel.setWrapText(true);
                
                reminderItem.getChildren().add(reminderLabel);
                notificationBox.getChildren().add(reminderItem);
            });
        }
    
        ScrollPane scrollPane = new ScrollPane(notificationBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(Math.min(notificationBox.getChildren().size() * 75, 500));  // Dynamic height, max 500px
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    
        notificationPopup.getContent().clear();
        notificationPopup.getContent().add(scrollPane);
    
        // Position the popup properly under the bell icon
        notificationPopup.show(bellIcon, bellIcon.localToScreen(0, bellIcon.getHeight()).getX() - 250,
                bellIcon.localToScreen(0, bellIcon.getHeight()).getY() + 35);
        isPopupVisible = true;
    }
    
}
