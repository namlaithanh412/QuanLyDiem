package grademanager.app.screen;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import grademanager.app.grade.DataManager;
import grademanager.app.grade.User;
import grademanager.app.grade.UserSession;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private RadioButton studentRadio;
    @FXML private RadioButton professorRadio;
    @FXML private RadioButton adminRadio; // Added
    @FXML private ToggleGroup roleGroup;

    @FXML
    public void initialize() {
        // Default selection
        studentRadio.setSelected(true);
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter username and password");
            return;
        }

        User.UserRole selectedRole;
        if (studentRadio.isSelected()) selectedRole = User.UserRole.STUDENT;
        else if (professorRadio.isSelected()) selectedRole = User.UserRole.PROFESSOR;
        else selectedRole = User.UserRole.ADMIN;

        // Verify against DataManager
        User user = DataManager.getInstance().getUserByUsername(username);
        
        if (user != null && user.getPassword().equals(password)) {
            if (user.getRole() != selectedRole) {
                messageLabel.setText("Invalid role for this user");
                return;
            }
            
            UserSession.getInstance().setCurrentUser(user);
            openGradeManager();
        } else {
            messageLabel.setText("Invalid username or password");
        }
    }

    private void openGradeManager() {
        try {
            UserSession session = UserSession.getInstance();
            String fxmlFile = "";
            
            if (session.isStudent()) fxmlFile = "StudentView.fxml";
            else if (session.isProfessor()) fxmlFile = "ProfessorView.fxml";
            else if (session.isAdmin()) fxmlFile = "AdminView.fxml"; // Route to Admin View
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setTitle("Grade Manager - " + session.getCurrentUser().getRole());
            stage.setScene(new Scene(root, 900, 600));
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Error loading view: " + e.getMessage());
        }
    }
}