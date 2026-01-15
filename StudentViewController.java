package grademanager.app.screen;

import grademanager.app.grade.Appeal;
import grademanager.app.grade.Course;
import grademanager.app.grade.DataManager;
import grademanager.app.grade.Enrollment;
import grademanager.app.grade.GradeRecord;
import grademanager.app.grade.Student;
import grademanager.app.grade.User;
import grademanager.app.grade.UserSession;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.stream.Collectors;

public class StudentViewController {
    @FXML private Label userInfoLabel;
    @FXML private Label studentInfoLabel;
    
    // Grades Tab
    @FXML private ComboBox<Integer> myGradesSemesterComboBox;
    @FXML private TableView<GradeRecord> gradeTable;
    @FXML private TableColumn<GradeRecord, String> courseIdColumn;
    @FXML private TableColumn<GradeRecord, String> courseNameColumn;
    @FXML private TableColumn<GradeRecord, Integer> creditsColumn;
    @FXML private TableColumn<GradeRecord, Double> midtermColumn;
    @FXML private TableColumn<GradeRecord, Double> finalColumn;
    @FXML private TableColumn<GradeRecord, Double> gpaColumn;
    @FXML private Label overallGPALabel;

    // Register Tab
    @FXML private ComboBox<Integer> semesterComboBox;
    @FXML private TableView<Course> availableCourseTable;
    @FXML private TableColumn<Course, String> avIdCol;
    @FXML private TableColumn<Course, Integer> avCodeCol;
    @FXML private TableColumn<Course, String> avNameCol;
    @FXML private TableColumn<Course, String> avProfCol;
    @FXML private TableColumn<Course, Integer> avCreditsCol;
    @FXML private TableColumn<Course, Integer> avCapacityCol;
    @FXML private TableColumn<Course, String> avEnrolledCol;

    private ObservableList<GradeRecord> gradeRecords;
    private FilteredList<GradeRecord> filteredGrades;
    private ObservableList<Course> availableCourses;
    private DataManager dataManager;
    private Student currentStudent; // Store current student for appeal logic

    @FXML
    public void initialize() {
        dataManager = DataManager.getInstance();
        gradeRecords = FXCollections.observableArrayList();
        filteredGrades = new FilteredList<>(gradeRecords, p -> true);
        availableCourses = FXCollections.observableArrayList();
        
        // 1. Grade Table Setup
        courseIdColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCourseId()));
        courseNameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCourseName()));
        creditsColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getCredits()).asObject());
        midtermColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().getMidtermScore()).asObject());
        finalColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().getFinalScore()).asObject());
        gpaColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().getGpa()).asObject());
        
        gpaColumn.setCellFactory(column -> new TableCell<GradeRecord, Double>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null); else setText(String.format("%.2f", item));
            }
        });
        gradeTable.setItems(filteredGrades);
        
        // 2. Available Courses Table Setup
        avIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        avCodeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        avNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        avProfCol.setCellValueFactory(new PropertyValueFactory<>("professor"));
        avCreditsCol.setCellValueFactory(new PropertyValueFactory<>("credits"));
        avCapacityCol.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        avEnrolledCol.setCellValueFactory(cell -> {
            long enrolled = dataManager.getEnrolledCount(cell.getValue().getId());
            return new SimpleStringProperty(enrolled + " / " + cell.getValue().getCapacity());
        });
        availableCourseTable.setItems(availableCourses);
        
        // 3. Setup Semesters
        for (int year = 2022; year <= 2026; year++) {
            for (int sem = 1; sem <= 3; sem++) {
                int semester = year * 10 + sem;
                semesterComboBox.getItems().add(semester);
                myGradesSemesterComboBox.getItems().add(semester);
            }
        }
        
        if (!semesterComboBox.getItems().isEmpty()) {
            semesterComboBox.getSelectionModel().select(Integer.valueOf(20251));
            myGradesSemesterComboBox.getSelectionModel().select(Integer.valueOf(20251));
        }

        semesterComboBox.getSelectionModel().selectedItemProperty().addListener((o, old, newVal) -> {
            if (newVal != null) updateAvailableCourses();
        });

        myGradesSemesterComboBox.getSelectionModel().selectedItemProperty().addListener((o, old, newVal) -> {
            if (newVal != null) {
                filteredGrades.setPredicate(record -> record.getSemester().equals(String.valueOf(newVal)));
            }
        });

        loadStudentData();
        updateAvailableCourses();
    }

    private void loadStudentData() {
        User u = UserSession.getInstance().getCurrentUser();
        userInfoLabel.setText("Student: " + u.getUsername());
        currentStudent = dataManager.getStudentByUsername(u.getUsername());
        
        if (currentStudent != null) {
            studentInfoLabel.setText("Student: " + currentStudent.getName() + " (ID: " + currentStudent.getId() + ")");
            gradeRecords.clear();
            double totalGPA = 0; int totalCredits = 0;
            
            for (Enrollment e : currentStudent.getEnrollments()) {
                Course c = dataManager.getCourseById(e.getCourseId());
                if (c != null) {
                    double gpa = e.getGPA(c.getMidtermWeight());
                    GradeRecord rec = new GradeRecord(e.getSemester(), c.getId(), c.getName(), c.getCredits(), 
                                        e.getMidtermScore(), e.getFinalScore(), gpa);
                    gradeRecords.add(rec);
                    totalGPA += gpa * c.getCredits();
                    totalCredits += c.getCredits();
                }
            }
            if (totalCredits > 0) overallGPALabel.setText(String.format("Overall GPA: %.2f", totalGPA / totalCredits));
            
            if (myGradesSemesterComboBox.getValue() != null) {
                filteredGrades.setPredicate(record -> record.getSemester().equals(String.valueOf(myGradesSemesterComboBox.getValue())));
            }
        }
    }

    private void updateAvailableCourses() {
        Integer selectedSemester = semesterComboBox.getValue();
        if (selectedSemester == null) return;
        availableCourses.clear();
        availableCourses.addAll(dataManager.getCourses().stream()
            .filter(c -> c.getSemester() == selectedSemester)
            .collect(Collectors.toList()));
        availableCourseTable.refresh();
    }

    @FXML
    private void handleRegisterCourse() {
        Course selected = availableCourseTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Error", "Select a course."); return; }

        if (currentStudent == null) { showAlert("Error", "Profile not found."); return; }

        if (currentStudent.getEnrollments().stream().anyMatch(e -> e.getCourseId().equals(selected.getId()))) {
            showAlert("Error", "Already enrolled."); return;
        }

        if (dataManager.getEnrolledCount(selected.getId()) >= selected.getCapacity()) {
            showAlert("Error", "Class full."); return;
        }

        currentStudent.getEnrollments().add(new Enrollment(currentStudent.getId(), selected.getId(), 
                                            String.valueOf(selected.getSemester()), 0, 0));
        showAlert("Success", "Registered.");
        updateAvailableCourses();
        loadStudentData();
    }
    
    // --- APPEAL LOGIC ---
    @FXML private void handleAppealMidterm() { sendAppeal("Midterm"); }
    @FXML private void handleAppealFinal() { sendAppeal("Final"); }

    private void sendAppeal(String type) {
        GradeRecord selected = gradeTable.getSelectionModel().getSelectedItem();
        if (selected == null) { 
            showAlert("Error", "Please select a course to appeal."); 
            return; 
        }
        
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Appeal " + type + " Grade");
        dialog.setHeaderText("Course: " + selected.getCourseName());
        dialog.setContentText("Reason for appeal:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            DataManager.getInstance().getAppeals().add(new Appeal(
                currentStudent.getId(), currentStudent.getName(), 
                selected.getCourseId(), selected.getSemester(), type, result.get()
            ));
            showAlert("Success", "Appeal sent to professor.");
        }
    }
    // --------------------

    @FXML private void handleLogout() {
        try {
            UserSession.getInstance().logout();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) userInfoLabel.getScene().getWindow();
            stage.setTitle("Grade Manager - Login");
            stage.setScene(new Scene(root, 400, 350));
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}