package grademanager.app.grade;

public class GradeRecord {
    private String semester;
    private String courseId;
    private String courseName;
    private int credits;
    private double midtermScore;
    private double finalScore;
    private double gpa;

    public GradeRecord(String semester, String courseId, String courseName, 
                      int credits, double midtermScore, double finalScore, double gpa) {
        this.semester = semester;
        this.courseId = courseId;
        this.courseName = courseName;
        this.credits = credits;
        this.midtermScore = midtermScore;
        this.finalScore = finalScore;
        this.gpa = gpa;
    }

    public String getSemester() { return semester; }
    public String getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public int getCredits() { return credits; }
    public double getMidtermScore() { return midtermScore; }
    public double getFinalScore() { return finalScore; }
    public double getGpa() { return gpa; }
}