package grademanager.app.grade;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.*;

public class DataManager {
    private static DataManager instance;
    private ObservableList<Course> courses;
    private ObservableList<Student> students;
    private ObservableList<User> users;
    private ObservableList<Appeal> appeals; // Added
    private final String DATA_FILE = "grademanager.dat";

    private DataManager() {
        courses = FXCollections.observableArrayList();
        students = FXCollections.observableArrayList();
        users = FXCollections.observableArrayList();
        appeals = FXCollections.observableArrayList();
        loadData();
    }

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    public void saveData() {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(DATA_FILE))) {
            // Save Courses
            out.writeInt(courses.size());
            for (Course c : courses) {
                out.writeUTF(c.getId());
                out.writeInt(c.getCode());
                out.writeUTF(c.getName());
                out.writeUTF(c.getProfessor());
                out.writeInt(c.getCredits());
                out.writeInt(c.getSemester());
                out.writeInt(c.getCapacity());
                out.writeDouble(c.getMidtermWeight()); // Save Weight
            }

            // Save Students
            out.writeInt(students.size());
            for (Student s : students) {
                out.writeInt(s.getId());
                out.writeUTF(s.getName());
                out.writeUTF(s.getUsername());
                
                out.writeInt(s.getEnrollments().size());
                for (Enrollment e : s.getEnrollments()) {
                    out.writeUTF(e.getCourseId());
                    out.writeUTF(e.getSemester());
                    out.writeDouble(e.getMidtermScore());
                    out.writeDouble(e.getFinalScore());
                }
            }

            // Save Users
            out.writeInt(users.size());
            for (User u : users) {
                out.writeUTF(u.getUsername());
                out.writeUTF(u.getPassword());
                out.writeUTF(u.getRealName());
                out.writeUTF(u.getRole().name());
            }

            // Save Appeals
            out.writeInt(appeals.size());
            for (Appeal a : appeals) {
                out.writeInt(a.getId());
                out.writeInt(a.getStudentId());
                out.writeUTF(a.getStudentName());
                out.writeUTF(a.getCourseId());
                out.writeUTF(a.getSemester());
                out.writeUTF(a.getGradeType());
                out.writeUTF(a.getReason());
                out.writeUTF(a.getStatus());
            }
            
            System.out.println("Data saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            createDefaultAdmin();
            return; 
        }

        try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {
            // Load Courses
            int courseCount = in.readInt();
            for (int i = 0; i < courseCount; i++) {
                courses.add(new Course(
                    in.readUTF(), in.readUTF(), in.readInt(), in.readUTF(), 
                    in.readInt(), in.readInt(), in.readInt(), in.readDouble() // Load Weight
                ));
            }

            // Load Students
            int studentCount = in.readInt();
            for (int i = 0; i < studentCount; i++) {
                Student student = new Student(
                    in.readInt(), in.readUTF(), in.readUTF()
                );
                
                int enrollmentCount = in.readInt();
                for (int j = 0; j < enrollmentCount; j++) {
                    student.getEnrollments().add(new Enrollment(
                        student.getId(), in.readUTF(), in.readUTF(), 
                        in.readDouble(), in.readDouble()
                    ));
                }
                students.add(student);
            }

            // Load Users
            if (in.available() > 0) {
                int userCount = in.readInt();
                for (int i = 0; i < userCount; i++) {
                    String username = in.readUTF();
                    String password = in.readUTF();
                    String realName = in.readUTF();
                    String roleStr = in.readUTF();
                    users.add(new User(username, password, realName, User.UserRole.valueOf(roleStr)));
                }
            }

            // Load Appeals
            if (in.available() > 0) {
                int appealCount = in.readInt();
                for (int i = 0; i < appealCount; i++) {
                    appeals.add(new Appeal(
                        in.readInt(), in.readInt(), in.readUTF(), in.readUTF(), 
                        in.readUTF(), in.readUTF(), in.readUTF(), in.readUTF()
                    ));
                }
            }
            
            if (users.stream().noneMatch(u -> u.getRole() == User.UserRole.ADMIN)) {
                createDefaultAdmin();
            }

        } catch (IOException e) {
            e.printStackTrace();
            createDefaultAdmin();
        }
    }

    private void createDefaultAdmin() {
        if (users.stream().noneMatch(u -> u.getUsername().equals("admin"))) {
            users.add(new User("admin", "admin123", "Administrator", User.UserRole.ADMIN));
        }
    }

    public ObservableList<Course> getCourses() { return courses; }
    public ObservableList<Student> getStudents() { return students; }
    public ObservableList<User> getUsers() { return users; }
    public ObservableList<Appeal> getAppeals() { return appeals; }
    
    public Course getCourseById(String id) {
        return courses.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }
    
    public Student getStudentByUsername(String username) {
        return students.stream().filter(s -> s.getUsername().equals(username)).findFirst().orElse(null);
    }
    
    public User getUserByUsername(String username) {
        return users.stream().filter(u -> u.getUsername().equals(username)).findFirst().orElse(null);
    }
    
    public long getEnrolledCount(String courseId) {
        return students.stream()
                .flatMap(s -> s.getEnrollments().stream())
                .filter(e -> e.getCourseId().equals(courseId))
                .count();
    }
}