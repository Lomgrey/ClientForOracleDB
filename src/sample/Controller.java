package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Controller {

    @FXML private TextField loginField;

    @FXML private PasswordField pasField;

    @FXML private Button logButton;

    @FXML private TableView table;

    @FXML private TextField logField;

    public void initialize(){
        logField.setEditable(false);
    }

    private void connection(String login, String password){
        String jdbcUrl = "jdbc:oracle:thin:@localhost:1521/orcl";

        try {
            Class.forName("oracle.jdbc.OracleDriver"); // Register Oracle JDBC driver
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Cannot find JDBC driver");
            return;
        }

        Connection connection = null;
        try{
            connection = DriverManager.getConnection(jdbcUrl, login, password);
        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console");
            String message = e.getMessage();
            logField.setText(message);
            e.printStackTrace();
            return;
        }

        if (connection != null)
            logField.setText("Connection successfully");
        else
            logField.setText("Failed to make connection!");
    }

    public void login(){
        connection(loginField.getText(), pasField.getText());


    }
}
