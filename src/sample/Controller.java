package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import sample.model.Employee;

import java.net.ConnectException;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller {

    @FXML private TextField loginField;

    @FXML private PasswordField pasField;

    @FXML private Button logButton;

    @FXML private Button logoutButton;

    @FXML private TextArea logArea;

    @FXML private TextField id;

    @FXML private TextField name;

    @FXML private TextField position;

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

        table.setEditable(true);
        table.setOnKeyPressed(new TableEventHandler());

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
            throws SQLException {
        String jdbcUrl = "jdbc:oracle:thin:@//localhost:1521/orcl";

        connection = null;
        connection = DriverManager.getConnection(jdbcUrl, login, password);
        return connection;
    }

    public void login(){

        currentUser = loginField.getText();
        try {
            connection(currentUser, pasField.getText());
            logArea.appendText(String.format("User %s log in\n", currentUser));
        } catch (SQLException e) {
            logArea.appendText("Error, try again.\n");
            showAlert(Alert.AlertType.ERROR, getClearMessage(e.getMessage()));
            return;
        }

        try {
            ResultSet rs = executeQuery("SELECT * FROM OWNER.WORKERS");
            fillTable(rs);

        } catch (SQLException e) {
            logArea.appendText(e.getMessage() + "\n");
            return;
        }

        logButton.setDisable(true);
        logoutButton.setDisable(false);
        pasField.setText("");
    }

    public void logout(){
        try {
            connection.close();
            logArea.appendText(String.format("User %s log out\n", currentUser));
        } catch (SQLException e) {
            logArea.appendText("cannot logout\n");
            return;
        }

        clearTable();
        currentUser = null;
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

    private ResultSet executeQuery(String query) throws SQLException {
        try {
            Statement statement = connection.createStatement();
            return statement.executeQuery(query);

        } catch (SQLException e) {
            System.out.println("Filed execute query");
            throw e;
        }
    }

    public void addItem(){
        if(currentUser == null){
            showAlert(Alert.AlertType.ERROR,"No user connection");
            return;
        }

        String idTemp = id.getText();
        String nameT = name.getText();
        String posT = position.getText();

        if (idTemp.equals("") || idTemp.equals(" ") ||
                nameT.equals("") || nameT.equals(" ") ||
                posT.equals("") || posT.equals(" ")){
            logArea.appendText("Fill all fields for adding");
            showAlert(Alert.AlertType.ERROR, "Fill all fields!");
            return;
        }

        int idT = Integer.valueOf(idTemp);

        try {
            ResultSet rs = executeQuery(
                    String.format("INSERT into owner.workers VALUES (%s, '%s', '%s')",
                            idT, nameT, posT)
            );

        } catch (SQLException e) {
            String message = getClearMessage(e.getMessage());
            logArea.appendText(message + "\n");
            showAlert(Alert.AlertType.ERROR, message);
            return;
        }
        logArea.appendText("Successful add item\n");

        id.setText("");
        name.setText("");
        position.setText("");

        table.getItems().add(new Employee(idT, nameT, posT));
    }

    private void clearTable(){
        table.getItems().clear();
    }

    private void showAlert(Alert.AlertType alertType, String message){
        Alert alert = new Alert(alertType);
        alert.setTitle("Message");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    private String getClearMessage(String message){
        Pattern p = Pattern.compile(":.+([\\(]|;|$)");
        Matcher m = p.matcher(message);

        if(m.find()){
            String s = m.group();
            return s.substring(1, s.length() - 1);
        }

        return message; //хотя такого и не должно случиться
    }

    public class TableEventHandler implements EventHandler<KeyEvent>{
        KeyCodeCombination deleteCombination = new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_ANY);
        KeyCode delete = KeyCode.DELETE;

        @Override
        public void handle(KeyEvent event) {
            if(deleteCombination.match(event) || event.getCode() == delete){
                Employee employee = table.getSelectionModel().getSelectedItem();
                String query = "DELETE FROM workers WHERE id = %s";
                try {
                    executeQuery(String.format(query, employee.getId()));
                } catch (SQLException e1) {
                    showAlert(Alert.AlertType.ERROR, getClearMessage(e1.getMessage()));
                    return;
                }
                showAlert(Alert.AlertType.INFORMATION, "Item deleted!");
            }
        }
    }
}
