package app.models;


import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class Task {
    public enum Status {
        OPEN, IN_PROGRESS, POSTPONED, COMPLETED, DELAYED
    }

    private String title;
    private String id;
    private String description;
    private Category category;
    private String priority;  
    private LocalDate deadline;  
    private Status status;  

    public Task(String title, String description, Category category, String priority, LocalDate deadline, Status status) {
        this.title = title;
        this.id = UUID.randomUUID().toString();
        this.description = description;
        this.category = category;
        this.priority = (priority != null && !priority.isEmpty()) ? priority : "Default";  
        this.deadline = deadline;
        this.status = (status != null) ? status : Status.OPEN;  
    }

    public Task() {
        this.id = UUID.randomUUID().toString();
        this.status = Status.OPEN;  
        this.priority = "Default";  
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getPriority() {
        return priority;
    }
    public void setPriority(String priority) {
        if (priority == null || priority.isEmpty()) {
            this.priority = "Default"; 
        } else {
            this.priority = priority;
        }
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", category=" + (category != null ? category.getName() : "None") +
                ", priority='" + priority + '\'' +
                ", deadline=" + deadline +
                ", status=" + status +
                '}';
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
