package grademanager.app;

import grademanager.app.grade.DataManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("screen/Login.fxml"));
        primaryStage.setTitle("Grade Manager - Login");
        primaryStage.setScene(new Scene(root, 400, 350));
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        DataManager.getInstance().saveData();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}