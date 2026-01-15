package grademanager.app.grade;

import javafx.beans.property.*;

public class Enrollment {
    private final IntegerProperty studentId;
    private final StringProperty courseId;
    private final StringProperty semester;
    private final DoubleProperty midtermScore;
    private final DoubleProperty finalScore;

    public Enrollment(int studentId, String courseId, String semester, double midtermScore, double finalScore) {
        this.studentId = new SimpleIntegerProperty(studentId);
        this.courseId = new SimpleStringProperty(courseId);
        this.semester = new SimpleStringProperty(semester);
        this.midtermScore = new SimpleDoubleProperty(midtermScore);
        this.finalScore = new SimpleDoubleProperty(finalScore);
    }

    public int getStudentId() { return studentId.get(); }
    public IntegerProperty studentIdProperty() { return studentId; }

    public String getCourseId() { return courseId.get(); }
    public StringProperty courseIdProperty() { return courseId; }

    public String getSemester() { return semester.get(); }
    public void setSemester(String semester) { this.semester.set(semester); }
    public StringProperty semesterProperty() { return semester; }

    public double getMidtermScore() { return midtermScore.get(); }
    public void setMidtermScore(double score) { this.midtermScore.set(score); }
    public DoubleProperty midtermScoreProperty() { return midtermScore; }

    public double getFinalScore() { return finalScore.get(); }
    public void setFinalScore(double score) { this.finalScore.set(score); }
    public DoubleProperty finalScoreProperty() { return finalScore; }

    public double getGPA(double midtermWeight) {
        double finalWeight = 1.0 - midtermWeight;
        double weightedScore = (midtermScore.get() * midtermWeight) + (finalScore.get() * finalWeight);
        
        return (weightedScore / 10.0) * 4.0;
    }
}