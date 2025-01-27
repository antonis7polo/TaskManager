package app.models;

import java.util.Objects;

public class Priority {
    private String name;

    public Priority(String name) {
        this.name = name;
    }

    public Priority() {}

    public static Priority defaultPriority() {
        return new Priority("Default");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (!"Default".equalsIgnoreCase(this.name)) { 
            this.name = name;
        } else {
            throw new UnsupportedOperationException("Default priority cannot be renamed.");
        }
    }

    @Override
    public String toString() {
        return "Priority{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Priority priority = (Priority) o;
        return Objects.equals(name, priority.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
