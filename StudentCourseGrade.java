package grademanager.app.grade;

public class StudentCourseGrade {
    private int studentId;
    private String studentName;
    private String username;
    private String semester;
    private double midtermScore;
    private double finalScore;
    private double gpa;

    public StudentCourseGrade(int studentId, String studentName, String username,
                             String semester, double midtermScore, double finalScore, double gpa) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.username = username;
        this.semester = semester;
        this.midtermScore = midtermScore;
        this.finalScore = finalScore;
        this.gpa = gpa;
    }

    public int getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getUsername() { return username; }
    public String getSemester() { return semester; }
    public double getMidtermScore() { return midtermScore; }
    public void setMidtermScore(double score) { this.midtermScore = score; }
    public double getFinalScore() { return finalScore; }
    public void setFinalScore(double score) { this.finalScore = score; }
    public double getGpa() { return gpa; }
    public void setGpa(double gpa) { this.gpa = gpa; }
}

