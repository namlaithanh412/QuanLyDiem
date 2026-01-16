package grademanager.app.screen;

import grademanager.app.grade.Course;
import grademanager.app.grade.DataManager;
import grademanager.app.grade.Student;
import grademanager.app.grade.User;
import grademanager.app.grade.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.stream.Collectors;

public class AdminViewController {
    // ... Existing UI Fields ...
    @FXML private Label userInfoLabel;
    @FXML private TextField newUsernameField;
    @FXML private PasswordField newPasswordField;
    @FXML private TextField realNameField;
    @FXML private VBox studentIdBox;
    @FXML private TextField newStudentIdField;
    @FXML private Label createUserLabel;
    @FXML private RadioButton studentRadio;
    @FXML private RadioButton profRadio;
    @FXML private ToggleGroup userRoleGroup;
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> usernameCol;
    @FXML private TableColumn<User, String> realNameCol;
    
    @FXML private TextField courseIdField;
    @FXML private TextField courseNameField;
    @FXML private TextField classCodeField;
    @FXML private TextField creditsField;
    @FXML private ComboBox<String> professorComboBox; 
    @FXML private TextField capacityField;
    @FXML private TextField midtermWeightField; // Added
    @FXML private ComboBox<Integer> semesterComboBox;
    @FXML private TableView<Course> courseTable;
    @FXML private TableColumn<Course, String> courseIdCol;
    @FXML private TableColumn<Course, String> courseNameCol;
    @FXML private TableColumn<Course, Integer> classCodeCol;
    @FXML private TableColumn<Course, String> classProfCol;
    @FXML private TableColumn<Course, Integer> courseCreditsCol;
    @FXML private TableColumn<Course, Integer> capacityCol;

    private DataManager dataManager;
    private FilteredList<Course> filteredCourses;
    private FilteredList<User> filteredUsers;

    @FXML
    public void initialize() {
        dataManager = DataManager.getInstance();
        User currentUser = UserSession.getInstance().getCurrentUser();
        userInfoLabel.setText("Admin: " + (currentUser != null ? currentUser.getUsername() : "Unknown"));

        // User Setup
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        realNameCol.setCellValueFactory(new PropertyValueFactory<>("realName"));
        filteredUsers = new FilteredList<>(dataManager.getUsers(), p -> true);
        userTable.setItems(filteredUsers);
        userRoleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> updateUserView());
        updateUserView();

        // Course Setup
        setupProfessorComboBox();
        courseIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        courseNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        classCodeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        classProfCol.setCellValueFactory(new PropertyValueFactory<>("professor"));
        courseCreditsCol.setCellValueFactory(new PropertyValueFactory<>("credits"));
        capacityCol.setCellValueFactory(new PropertyValueFactory<>("capacity"));

        for (int year = 2022; year <= 2026; year++) {
            for (int sem = 1; sem <= 3; sem++) semesterComboBox.getItems().add(year * 10 + sem);
        }
        if (!semesterComboBox.getItems().isEmpty()) semesterComboBox.getSelectionModel().select(Integer.valueOf(20251)); 

        filteredCourses = new FilteredList<>(dataManager.getCourses(), p -> true);
        courseTable.setItems(filteredCourses);

        semesterComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) filteredCourses.setPredicate(course -> course.getSemester() == newVal);
        });
        if (semesterComboBox.getValue() != null) {
            filteredCourses.setPredicate(course -> course.getSemester() == semesterComboBox.getValue());
        }
    }
    
    // ... setupProfessorComboBox, updateUserView, handleCreateUser, handleDeleteUser (same as before) ...
    private void setupProfessorComboBox() {
        FilteredList<User> professors = new FilteredList<>(dataManager.getUsers(), u -> u.getRole() == User.UserRole.PROFESSOR);
        professorComboBox.setItems(FXCollections.observableArrayList(professors.stream().map(User::getUsername).collect(Collectors.toList())));
        professorComboBox.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (professorComboBox.getSelectionModel().getSelectedItem() != null && 
                professorComboBox.getSelectionModel().getSelectedItem().equals(newText)) return;
            professorComboBox.setItems(FXCollections.observableArrayList(
                professors.stream().map(User::getUsername).filter(name -> name.toLowerCase().contains(newText.toLowerCase())).collect(Collectors.toList())
            ));
            if (!professorComboBox.getItems().isEmpty()) professorComboBox.show();
        });
        professors.addListener((javafx.collections.ListChangeListener.Change<? extends User> c) -> {
             professorComboBox.setItems(FXCollections.observableArrayList(professors.stream().map(User::getUsername).collect(Collectors.toList())));
        });
    }
    private void updateUserView() {
        if (studentRadio.isSelected()) {
            createUserLabel.setText("Create New Student"); studentIdBox.setVisible(true); studentIdBox.setManaged(true);
            filteredUsers.setPredicate(user -> user.getRole() == User.UserRole.STUDENT);
        } else {
            createUserLabel.setText("Create New Professor"); studentIdBox.setVisible(false); studentIdBox.setManaged(false);
            filteredUsers.setPredicate(user -> user.getRole() == User.UserRole.PROFESSOR);
        }
    }
    @FXML private void handleCreateUser() {
        String username = newUsernameField.getText().trim();
        String password = newPasswordField.getText();
        String realName = realNameField.getText().trim();
        if (username.isEmpty() || password.isEmpty() || realName.isEmpty()) { showAlert("Error", "Fill all fields."); return; }
        if (dataManager.getUserByUsername(username) != null) { showAlert("Error", "Username exists."); return; }
        if (studentRadio.isSelected()) {
            try {
                int sid = Integer.parseInt(newStudentIdField.getText().trim());

                if (dataManager.getUserByUsername(username) != null) {
                    showAlert("Error", "Username already exists.");
                    return;
                }

                if (dataManager.getStudents().stream().anyMatch(s -> s.getId() == sid)) {
                    showAlert("Error", "Student ID already exists.");
                    return;
                }

                dataManager.getUsers().add(
                    new User(username, password, realName, User.UserRole.STUDENT)
                );
                dataManager.getStudents().add(
                    new Student(sid, realName, username)
                );

                showAlert("Success", "Student Created.");
                newStudentIdField.clear();

            } catch (Exception e) {
                showAlert("Error", "Invalid Student ID.");
            }
        }
        else {
            if (dataManager.getUserByUsername(username) != null) {
                showAlert("Error", "Username already exists.");
                return;
            }

            dataManager.getUsers().add(
                new User(username, password, realName, User.UserRole.PROFESSOR)
            );

            showAlert("Success", "Professor Created.");

            // Clear form
            newUsernameField.clear();
            newPasswordField.clear();
            realNameField.clear();
        }
    }
    @FXML private void handleDeleteUser() {
        User u = userTable.getSelectionModel().getSelectedItem();
        if (u != null && !u.getUsername().equals(UserSession.getInstance().getCurrentUser().getUsername())) 
            dataManager.getUsers().remove(u);
    }

    @FXML
    private void handleAddCourse() {
        try {
            String id = courseIdField.getText().trim();
            String name = courseNameField.getText().trim();
            String codeStr = classCodeField.getText().trim();
            String creditsStr = creditsField.getText().trim();
            String prof = professorComboBox.getValue();
            String capStr = capacityField.getText().trim();
            String weightStr = midtermWeightField.getText().trim(); // Get weight
            Integer selectedSemester = semesterComboBox.getValue();

            if (id.isEmpty() || codeStr.isEmpty() || name.isEmpty() || creditsStr.isEmpty() || prof == null || capStr.isEmpty()) {
                showAlert("Error", "All fields are required."); return;
            }
            if (dataManager.getUserByUsername(prof) == null) { showAlert("Error", "Professor not found."); return; }

            int code = Integer.parseInt(codeStr);
            int credits = Integer.parseInt(creditsStr);
            int capacity = Integer.parseInt(capStr);
            
            boolean classCodeExists = dataManager.getCourses().stream()
                    .anyMatch(c -> c.getCode() == code && c.getSemester() == selectedSemester);

            if (classCodeExists) {
                showAlert("Error", "Class code already exists.");
                return;
            }

            if (dataManager.getCourseById(id) != null) {
                showAlert("Error", "Course ID exists.");
                return;
            }
            
            double weight = 0.4; // Default
            if (!weightStr.isEmpty()) {
                weight = Double.parseDouble(weightStr);
                if (weight < 0 || weight > 1) {
                    showAlert("Error", "Weight must be between 0.0 and 1.0"); return;
                }
            }

            if (dataManager.getCourseById(id) != null) { showAlert("Error", "Course ID exists."); return; }

            // Pass weight to constructor
            Course course = new Course(id, name, code, prof, credits, selectedSemester, capacity, weight);
            dataManager.getCourses().add(course);
            
            courseIdField.clear(); courseNameField.clear(); classCodeField.clear();
            creditsField.clear(); professorComboBox.setValue(null); capacityField.clear(); midtermWeightField.clear();
            
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid number format.");
        }
    }

    @FXML private void handleDeleteCourse() {
        Course s = courseTable.getSelectionModel().getSelectedItem();
        if (s != null) dataManager.getCourses().remove(s);
    }

    @FXML private void handleLogout() {
        try { UserSession.getInstance().logout(); 
        Stage s = (Stage) userInfoLabel.getScene().getWindow();
        s.setScene(new Scene(FXMLLoader.load(getClass().getResource("Login.fxml")), 400, 350)); } catch (Exception e){}
    }
    private void showAlert(String t, String m) { new Alert(Alert.AlertType.INFORMATION, m).showAndWait(); }
}
