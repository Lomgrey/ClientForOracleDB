package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import sample.model.Employee;

import java.net.ConnectException;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class Controller {

    @FXML private TextField loginField;

    @FXML private PasswordField pasField;

    @FXML private Button logButton;

    @FXML private Button logoutButton;

    @FXML private TextArea logArea;

    @FXML private TableView<Employee> table;

    @FXML private TableColumn<Employee, Integer> cID;

    @FXML private TableColumn<Employee, Integer> cName;

    @FXML private TableColumn<Employee, Integer> cPos;

    private Connection connection;

    private String currentUser;

    public void initialize(){
        logArea.setEditable(false);

        cID.setCellValueFactory(new PropertyValueFactory<>("id"));
        cName.setCellValueFactory(new PropertyValueFactory<>("Name"));
        cPos.setCellValueFactory(new PropertyValueFactory<>("Position"));

        try {
            registerJDBCDriver();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void registerJDBCDriver() throws ClassNotFoundException {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            logArea.appendText("Cannot find JDBC driver\n");
            logArea.appendText(e.getMessage() + "\n");
            throw e;
        }
    }

    /**
     * not null returned
     * @param login
     * @param password
     * @return
     * @throws ConnectException
     */
    private Connection connection(String login, String password)
            throws ConnectException {
        String jdbcUrl = "jdbc:oracle:thin:@//localhost:1521/orcl";

        connection = null;
        try{
            connection = DriverManager.getConnection(jdbcUrl, login, password);
        } catch (SQLException e) {
            logArea.appendText("Connection Failed! Check output console\n");
            String message = e.getMessage();
            logArea.appendText(message + "\n");
            e.printStackTrace();
        }

        if (connection != null) {
            logArea.appendText("Connection successfully\n");
            return connection;
        } else {
            logArea.appendText("Failed to make connection!\n");
            throw new ConnectException("Failed to make connection!");
        }
    }

    public void login(){

        currentUser = loginField.getText();
        try {
            connection(currentUser, pasField.getText());
            logArea.appendText(String.format("User %s log in\n", currentUser));
        } catch (ConnectException e) {
            logArea.appendText("Error, try again.\n");
            return;
        }

        try {
            Statement statement = connection.createStatement();
            String query = "SELECT * FROM OWNER.WORKERS";

            ResultSet rs = statement.executeQuery(query);

            fillTable(rs);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        logButton.setDisable(true);
        logoutButton.setDisable(false);
    }

    public void logout(){
        try {
            connection.close();
            logArea.appendText(String.format("User %s log out\n", currentUser));
        } catch (SQLException e) {
            logArea.appendText("cannot logout\n");
            e.printStackTrace();
        }

        clearTable();
        logButton.setDisable(false);
        logoutButton.setDisable(true);
    }

    private void fillTable(ResultSet rs) throws SQLException {

        ObservableList<Employee> data = FXCollections.observableArrayList();
        while (rs.next()){
            Integer id = rs.getInt("id");
            String name = rs.getString("name");
            String position = rs.getString("position");

            data.add(new Employee(id, name, position));

        }
        table.getItems().addAll(data);
    }

    private void clearTable(){
        table.getItems().clear();
    }
}
