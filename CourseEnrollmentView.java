package grademanager.app.grade;

public class CourseEnrollmentView {
    private String courseId;
    private String courseName;
    private String classCode;
    private int credits;
    private int studentCount;

    public CourseEnrollmentView(String courseId, String courseName, String classCode, 
                               int credits, int studentCount) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.classCode = classCode;
        this.credits = credits;
        this.studentCount = studentCount;
    }

    public String getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public String getClassCode() { return classCode; }
    public int getCredits() { return credits; }
    public int getStudentCount() { return studentCount; }
}