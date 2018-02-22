package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        Scene scene = new Scene(root, 390, 475);

        stage.setTitle("Oracle DB Client");
        stage.setScene(scene);
        stage.setMinWidth(390);
        stage.setMinHeight(475);
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
