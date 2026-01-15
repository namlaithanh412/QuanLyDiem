package grademanager.app.grade;

public class User {
    private final String username;
    private final String password;
    private final String realName;
    private final UserRole role;

    public enum UserRole {
        STUDENT, PROFESSOR, ADMIN
    }

    public User(String username, String password, String realName, UserRole role) {
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.role = role;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRealName() { return realName; }
    public UserRole getRole() { return role; }
    
    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}