package grademanager.app.screen;

import grademanager.app.grade.Appeal;
import grademanager.app.grade.Course;
import grademanager.app.grade.DataManager;
import grademanager.app.grade.Enrollment;
import grademanager.app.grade.GradeRecord;
import grademanager.app.grade.Student;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Optional;

public class StudentDetailController {
    
    @FXML private Label studentNameLabel;
    @FXML private ComboBox<Integer> semesterComboBox;
    @FXML private TableView<GradeRecord> gradeTable;
    @FXML private TableColumn<GradeRecord, String> semesterColumn;
    @FXML private TableColumn<GradeRecord, String> courseIdColumn;
    @FXML private TableColumn<GradeRecord, String> courseNameColumn;
    @FXML private TableColumn<GradeRecord, Integer> creditsColumn;
    @FXML private TableColumn<GradeRecord, Double> midtermColumn;
    @FXML private TableColumn<GradeRecord, Double> finalColumn;
    @FXML private TableColumn<GradeRecord, Double> gpaColumn;
    @FXML private Label overallGPALabel;

    private ObservableList<GradeRecord> gradeRecords;
    private FilteredList<GradeRecord> filteredGrades;
    private Student currentStudent;

    public void setStudent(Student student) {
        this.currentStudent = student;
        studentNameLabel.setText("Grades for: " + student.getName());
        loadGradeData(student);
    }

    @FXML
    public void initialize() {
        gradeRecords = FXCollections.observableArrayList();
        filteredGrades = new FilteredList<>(gradeRecords, p -> true);
        
        semesterColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getSemester()));
        courseIdColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCourseId()));
        courseNameColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCourseName()));
        creditsColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getCredits()).asObject());
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
        
        for (int year = 2022; year <= 2026; year++) {
            for (int sem = 1; sem <= 3; sem++) semesterComboBox.getItems().add(year * 10 + sem);
        }
        semesterComboBox.getSelectionModel().select(Integer.valueOf(20251));
        
        semesterComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) filteredGrades.setPredicate(record -> record.getSemester().equals(String.valueOf(newVal)));
        });
    }

    private void loadGradeData(Student student) {
        DataManager dataManager = DataManager.getInstance();
        gradeRecords.clear();
        double totalGPA = 0; int totalCredits = 0;
        
        for (Enrollment enrollment : student.getEnrollments()) {
            Course course = dataManager.getCourseById(enrollment.getCourseId());
            if (course != null) {
                // Calculate GPA using Course Weight
                double gpa = enrollment.getGPA(course.getMidtermWeight());
                
                GradeRecord record = new GradeRecord(
                    enrollment.getSemester(), course.getId(), course.getName(), course.getCredits(),
                    enrollment.getMidtermScore(), enrollment.getFinalScore(), gpa
                );
                gradeRecords.add(record);
                totalGPA += gpa * course.getCredits();
                totalCredits += course.getCredits();
            }
        }
        if (totalCredits > 0) overallGPALabel.setText(String.format("Overall GPA: %.2f", totalGPA / totalCredits));
        
        if (semesterComboBox.getValue() != null) {
             filteredGrades.setPredicate(record -> record.getSemester().equals(String.valueOf(semesterComboBox.getValue())));
        }
    }

    @FXML private void handleAppealMidterm() { sendAppeal("Midterm"); }
    @FXML private void handleAppealFinal() { sendAppeal("Final"); }

    private void sendAppeal(String type) {
        GradeRecord selected = gradeTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Select a course first."); return; }
        
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Appeal " + type + " Grade");
        dialog.setHeaderText("Course: " + selected.getCourseName());
        dialog.setContentText("Reason for appeal:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            DataManager.getInstance().getAppeals().add(new Appeal(
                currentStudent.getId(), currentStudent.getName(), 
                selected.getCourseId(), selected.getSemester(), type, result.get()
            ));
            showAlert("Appeal Sent.");
        }
    }
    
    @FXML private void handleClose() { ((Stage)studentNameLabel.getScene().getWindow()).close(); }
    private void showAlert(String m) { new Alert(Alert.AlertType.INFORMATION, m).showAndWait(); }
}