package grademanager.app.grade;

import javafx.beans.property.*;

public class Course {
    private final StringProperty id;
    private final IntegerProperty code;
    private final StringProperty name;
    private final StringProperty professor;
    private final IntegerProperty credits;
    private final IntegerProperty semester;
    private final IntegerProperty capacity;
    private final DoubleProperty midtermWeight;

    public Course(String id, String name, int code, String professor, int credits, int semester, int capacity, double midtermWeight) {
        this.id = new SimpleStringProperty(id);
        this.code = new SimpleIntegerProperty(code);
        this.name = new SimpleStringProperty(name);
        this.professor = new SimpleStringProperty(professor);
        this.credits = new SimpleIntegerProperty(credits);
        this.semester = new SimpleIntegerProperty(semester);
        this.capacity = new SimpleIntegerProperty(capacity);
        this.midtermWeight = new SimpleDoubleProperty(midtermWeight);
    }

    public String getId() { return id.get(); }
    public StringProperty idProperty() { return id; }

    public int getCode() { return code.get(); }
    public IntegerProperty codeProperty() { return code; }

    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }

    public String getProfessor() { return professor.get(); }
    public StringProperty professorProperty() { return professor; }

    public int getCredits() { return credits.get(); }
    public IntegerProperty creditsProperty() { return credits; }
    
    public int getSemester() { return semester.get(); }
    public IntegerProperty semesterProperty() { return semester; }

    public int getCapacity() { return capacity.get(); }
    public IntegerProperty capacityProperty() { return capacity; }
    
    public double getMidtermWeight() { return midtermWeight.get(); }
    public DoubleProperty midtermWeightProperty() { return midtermWeight; }

    @Override
    public String toString() {
        return code.get() + " - " + name.get();
    }
}