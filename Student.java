package grademanager.app.grade;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Student {
    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty username;
    private final ObservableList<Enrollment> enrollments;

    public Student(int id, String name, String username) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.username = new SimpleStringProperty(username);
        this.enrollments = FXCollections.observableArrayList();
    }

    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }

    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }

    public String getUsername() { return username.get(); }
    public StringProperty usernameProperty() { return username; }

    public ObservableList<Enrollment> getEnrollments() { return enrollments; }

    public Enrollment getEnrollmentForCourse(String courseId) {
        return enrollments.stream()
                .filter(e -> e.getCourseId().equals(courseId))
                .findFirst()
                .orElse(null);
    }
}