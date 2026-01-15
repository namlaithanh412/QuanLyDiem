package grademanager.app.screen;

import grademanager.app.grade.Appeal;
import grademanager.app.grade.Course;
import grademanager.app.grade.DataManager;
import grademanager.app.grade.Enrollment;
import grademanager.app.grade.Student;
import grademanager.app.grade.StudentCourseGrade;
import grademanager.app.grade.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.stream.Collectors;

public class ProfessorViewController {
    @FXML private Label userInfoLabel;
    @FXML private ListView<Course> courseListView;
    @FXML private Label selectedCourseLabel;
    @FXML private ComboBox<Integer> semesterComboBox;
    
    @FXML private TableView<StudentCourseGrade> studentTable;
    @FXML private TableColumn<StudentCourseGrade, Integer> idColumn;
    @FXML private TableColumn<StudentCourseGrade, String> nameColumn;
    @FXML private TableColumn<StudentCourseGrade, String> semesterColumn;
    @FXML private TableColumn<StudentCourseGrade, Double> midtermColumn;
    @FXML private TableColumn<StudentCourseGrade, Double> finalColumn;
    @FXML private TableColumn<StudentCourseGrade, Double> gpaColumn;
    @FXML private TextField midtermField;
    @FXML private TextField finalField;

    // Appeal Table
    @FXML private TableView<Appeal> appealTable;
    @FXML private TableColumn<Appeal, String> appStudentCol;
    @FXML private TableColumn<Appeal, String> appCourseCol;
    @FXML private TableColumn<Appeal, String> appTypeCol;
    @FXML private TableColumn<Appeal, String> appReasonCol;
    @FXML private TableColumn<Appeal, String> appStatusCol;

    private ObservableList<StudentCourseGrade> studentsInCourse;
    private Course selectedCourse;
    private DataManager dataManager;
    private FilteredList<Course> filteredCourses;
    private FilteredList<Appeal> filteredAppeals;

    @FXML
    public void initialize() {
        dataManager = DataManager.getInstance();
        studentsInCourse = FXCollections.observableArrayList();
        
        // Grading Setup
        idColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        semesterColumn.setCellValueFactory(new PropertyValueFactory<>("semester"));
        midtermColumn.setCellValueFactory(new PropertyValueFactory<>("midtermScore"));
        finalColumn.setCellValueFactory(new PropertyValueFactory<>("finalScore"));
        gpaColumn.setCellValueFactory(new PropertyValueFactory<>("gpa"));
        gpaColumn.setCellFactory(column -> new TableCell<StudentCourseGrade, Double>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty); if (empty || item == null) setText(null); else setText(String.format("%.2f", item));
            }
        });
        studentTable.setItems(studentsInCourse);
        
        for (int year = 2022; year <= 2026; year++) { for (int sem = 1; sem <= 3; sem++) semesterComboBox.getItems().add(year * 10 + sem); }
        semesterComboBox.getSelectionModel().select(Integer.valueOf(20251));

        String currentProfUsername = UserSession.getInstance().getCurrentUser().getUsername();
        userInfoLabel.setText("Professor: " + currentProfUsername);

        filteredCourses = new FilteredList<>(dataManager.getCourses(), c -> 
            c.getProfessor().equalsIgnoreCase(currentProfUsername) && c.getSemester() == semesterComboBox.getValue());
        courseListView.setItems(filteredCourses);
        
        courseListView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> { if (newVal != null) selectCourse(newVal); });
        studentTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> { if (newVal != null) updateScoreFields(newVal); });
        semesterComboBox.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) filteredCourses.setPredicate(c -> c.getProfessor().equalsIgnoreCase(currentProfUsername) && c.getSemester() == newVal);
        });

        // Appeal Setup
        appStudentCol.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        appCourseCol.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        appTypeCol.setCellValueFactory(new PropertyValueFactory<>("gradeType"));
        appReasonCol.setCellValueFactory(new PropertyValueFactory<>("reason"));
        appStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Filter appeals: Only pending, only courses taught by this professor
        filteredAppeals = new FilteredList<>(dataManager.getAppeals(), a -> {
            Course c = dataManager.getCourseById(a.getCourseId());
            return c != null && c.getProfessor().equalsIgnoreCase(currentProfUsername) && "Pending".equals(a.getStatus());
        });
        appealTable.setItems(filteredAppeals);
    }

    // selectCourse, handleUpdateScores, handleViewStudentGrades (updated with getGPA(weight))
    private void selectCourse(Course course) {
        selectedCourse = course;
        selectedCourseLabel.setText("Students in: " + course.getCode() + " - " + course.getName());
        studentsInCourse.clear();
        for (Student student : dataManager.getStudents()) {
            for (Enrollment enrollment : student.getEnrollments()) {
                if (enrollment.getCourseId().equals(course.getId())) {
                    studentsInCourse.add(new StudentCourseGrade(
                        student.getId(), student.getName(), student.getUsername(),
                        enrollment.getSemester(), enrollment.getMidtermScore(),
                        enrollment.getFinalScore(), enrollment.getGPA(course.getMidtermWeight())
                    ));
                }
            }
        }
        studentTable.refresh();
    }
    
    @FXML private void handleUpdateScores() {
        StudentCourseGrade selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null || selectedCourse == null) { showAlert("Error", "Select student."); return; }
        try {
            double mid = Double.parseDouble(midtermField.getText().trim());
            double fin = Double.parseDouble(finalField.getText().trim());
            if (mid < 0 || mid > 10 || fin < 0 || fin > 10) { showAlert("Error", "0-10 range."); return; }
            Student s = dataManager.getStudents().stream().filter(st -> st.getId() == selected.getStudentId()).findFirst().orElse(null);
            if (s != null) {
                Enrollment e = s.getEnrollments().stream().filter(en -> en.getCourseId().equals(selectedCourse.getId()) && en.getSemester().equals(selected.getSemester())).findFirst().orElse(null);
                if (e != null) {
                    e.setMidtermScore(mid); e.setFinalScore(fin);
                    selected.setMidtermScore(mid); selected.setFinalScore(fin); 
                    selected.setGpa(e.getGPA(selectedCourse.getMidtermWeight()));
                    studentTable.refresh(); midtermField.clear(); finalField.clear();
                }
            }
        } catch (Exception e) { showAlert("Error", "Invalid numbers."); }
    }

    @FXML private void handleProcessAppeal() {
        Appeal appeal = appealTable.getSelectionModel().getSelectedItem();
        if (appeal == null) { showAlert("Error", "Select an appeal."); return; }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Process Appeal");
        alert.setHeaderText("Reason: " + appeal.getReason());
        alert.setContentText("Choose action:");
        
        ButtonType approveBtn = new ButtonType("Update Score");
        ButtonType rejectBtn = new ButtonType("Reject");
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(approveBtn, rejectBtn, cancelBtn);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == rejectBtn) {
                appeal.setStatus("Rejected");
                appealTable.refresh();
            } else if (result.get() == approveBtn) {
                TextInputDialog scoreDialog = new TextInputDialog();
                scoreDialog.setTitle("Update Score");
                scoreDialog.setHeaderText("Enter new " + appeal.getGradeType() + " score (0-10):");
                Optional<String> scoreRes = scoreDialog.showAndWait();
                if (scoreRes.isPresent()) {
                    try {
                        double newScore = Double.parseDouble(scoreRes.get());
                        if (newScore < 0 || newScore > 10) { showAlert("Error", "Invalid range."); return; }
                        
                        // Update Enrollment
                        Student s = dataManager.getStudents().stream().filter(st -> st.getId() == appeal.getStudentId()).findFirst().orElse(null);
                        Course c = dataManager.getCourseById(appeal.getCourseId());
                        if (s != null && c != null) {
                            Enrollment e = s.getEnrollments().stream().filter(en -> en.getCourseId().equals(c.getId())).findFirst().orElse(null);
                            if (e != null) {
                                if ("Midterm".equalsIgnoreCase(appeal.getGradeType())) e.setMidtermScore(newScore);
                                else e.setFinalScore(newScore);
                                appeal.setStatus("Approved");
                                appealTable.refresh();
                                if (selectedCourse != null && selectedCourse.getId().equals(c.getId())) selectCourse(selectedCourse); // Refresh view if needed
                            }
                        }
                    } catch (Exception e) { showAlert("Error", "Invalid number."); }
                }
            }
        }
    }

    @FXML
    private void handleViewStudentGrades() {
        StudentCourseGrade selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Error", "Please select a student."); return; }

        Student student = dataManager.getStudents().stream()
            .filter(s -> s.getId() == selected.getStudentId()).findFirst().orElse(null);
        
        if (student == null) { showAlert("Error", "Student not found."); return; }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("StudentDetailView.fxml"));
            Parent root = loader.load();
            StudentDetailController controller = loader.getController();
            controller.setStudent(student);
            
            Stage stage = new Stage();
            stage.setTitle("Student Grades - " + student.getName());
            stage.setScene(new Scene(root, 800, 500));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updateScoreFields(StudentCourseGrade s) {
        midtermField.setText(String.valueOf(s.getMidtermScore()));
        finalField.setText(String.valueOf(s.getFinalScore()));
    }

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
    
    private void showAlert(String t, String m) { new Alert(Alert.AlertType.INFORMATION, m).showAndWait(); }
}