package app;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

public class SidebarPane extends VBox {
    private final Label manageTasksLabel;
    private final Label manageCategoriesLabel;
    private final Label managePrioritiesLabel;
    private final Label manageRemindersLabel;

    public SidebarPane(Runnable showTasks, Runnable showCategories, Runnable showPriorities, Runnable showReminders) {
        manageTasksLabel = createStyledLabel("Manage Tasks", showTasks);
        manageCategoriesLabel = createStyledLabel("Manage Categories", showCategories);
        managePrioritiesLabel = createStyledLabel("Manage Priorities", showPriorities);
        manageRemindersLabel = createStyledLabel("Manage Reminders", showReminders);

        // Layout and styling
        setSpacing(10);
        setPadding(new Insets(15));
        setAlignment(Pos.TOP_LEFT);
        setStyle("-fx-background-color: #f5f5f5;");

        // Add labels to sidebar
        getChildren().addAll(manageTasksLabel, manageCategoriesLabel, managePrioritiesLabel,manageRemindersLabel);
    }

    private Label createStyledLabel(String text, Runnable onClick) {
        Label label = new Label(text);
        label.setPrefWidth(200); // Set a fixed width to prevent resizing
        label.setStyle("-fx-text-fill: #333333; " +
                       "-fx-font-size: 14px; " +
                       "-fx-padding: 10 20; " +  // Fixed padding
                       "-fx-background-color: transparent;"); // Default background
        label.setOnMouseEntered(e -> label.setStyle("-fx-text-fill: #333333; " +
                                                    "-fx-font-size: 14px; " +
                                                    "-fx-padding: 10 20; " + // Consistent padding
                                                    "-fx-background-color: #e6e6e6;")); // Highlight background
        label.setOnMouseExited(e -> label.setStyle("-fx-text-fill: #333333; " +
                                                   "-fx-font-size: 14px; " +
                                                   "-fx-padding: 10 20; " + // Consistent padding
                                                   "-fx-background-color: transparent;")); // Default background
        label.setOnMouseClicked(e -> onClick.run());
        return label;
    }
}
