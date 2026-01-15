package grademanager.app.grade;

public class UserSession {
    private static UserSession instance;
    private User currentUser;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isProfessor() {
        return currentUser != null && currentUser.getRole() == User.UserRole.PROFESSOR;
    }

    public boolean isStudent() {
        return currentUser != null && currentUser.getRole() == User.UserRole.STUDENT;
    }
    
    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == User.UserRole.ADMIN;
    }

    public void logout() {
        currentUser = null;
    }
}