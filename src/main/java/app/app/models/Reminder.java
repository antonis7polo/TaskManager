package app.models;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class Reminder {
    private String id;
    private String taskId; 
    private LocalDate reminderDate;
    private String description;

    public Reminder() {}

    public Reminder(String taskId, LocalDate reminderDate, String description) {
        this.id = UUID.randomUUID().toString(); 
        this.taskId = taskId;
        this.reminderDate = reminderDate;
        this.description = description;
    }

    public String getId(){
        return id;
    }
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public LocalDate getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(LocalDate reminderDate) {
        this.reminderDate = reminderDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return "Reminder{" +
                "id='" + id + '\'' +
                ", taskId='" + taskId + '\'' +
                ", reminderDate=" + reminderDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reminder reminder = (Reminder) o;
        return Objects.equals(id, reminder.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}