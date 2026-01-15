package grademanager.app.grade;

import javafx.beans.property.*;

public class Appeal {
    private final IntegerProperty id;
    private final IntegerProperty studentId;
    private final StringProperty studentName;
    private final StringProperty courseId;
    private final StringProperty semester;
    private final StringProperty gradeType;
    private final StringProperty reason;
    private final StringProperty status;

    private static int nextId = 1;

    public Appeal(int id, int studentId, String studentName, String courseId, String semester, String gradeType, String reason, String status) {
        this.id = new SimpleIntegerProperty(id);
        this.studentId = new SimpleIntegerProperty(studentId);
        this.studentName = new SimpleStringProperty(studentName);
        this.courseId = new SimpleStringProperty(courseId);
        this.semester = new SimpleStringProperty(semester);
        this.gradeType = new SimpleStringProperty(gradeType);
        this.reason = new SimpleStringProperty(reason);
        this.status = new SimpleStringProperty(status);
        if (id >= nextId) nextId = id + 1;
    }

    public Appeal(int studentId, String studentName, String courseId, String semester, String gradeType, String reason) {
        this(nextId++, studentId, studentName, courseId, semester, gradeType, reason, "Pending");
    }

    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }

    public int getStudentId() { return studentId.get(); }
    public String getStudentName() { return studentName.get(); }
    public StringProperty studentNameProperty() { return studentName; }

    public String getCourseId() { return courseId.get(); }
    public StringProperty courseIdProperty() { return courseId; }

    public String getSemester() { return semester.get(); }
    
    public String getGradeType() { return gradeType.get(); }
    public StringProperty gradeTypeProperty() { return gradeType; }

    public String getReason() { return reason.get(); }
    public StringProperty reasonProperty() { return reason; }

    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }
    public StringProperty statusProperty() { return status; }
}