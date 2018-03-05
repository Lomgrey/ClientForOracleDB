package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import sample.model.Employee;

import java.net.ConnectException;
import java.sql.*;
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

    @FXML private TableColumn<Employee, String> cName;

    @FXML private TableColumn<Employee, String> cPos;

    private Connection connection;

    private String currentUser;

    public void initialize(){
        logArea.setEditable(false);

        cID.setCellValueFactory(new PropertyValueFactory<>("id"));
        cName.setCellValueFactory(new PropertyValueFactory<>("Name"));
        cPos.setCellValueFactory(new PropertyValueFactory<>("Position"));

        table.setEditable(true);
        table.setOnKeyPressed(
                e -> {
                    KeyCodeCombination deleteCombination = new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_ANY);
                    KeyCode delete = KeyCode.DELETE;

                    if(deleteCombination.match(e) || e.getCode() == delete){
                        Employee employee = table.getSelectionModel().getSelectedItem();
                        try {
                            delete(employee);
                        } catch (SQLException e1) {
                            showAlert(Alert.AlertType.ERROR, getCleanMessage(e1.getMessage()));
                            return;
                        }
                        showAlert(Alert.AlertType.INFORMATION, "Item deleted!");
                        table.getItems().remove(employee);
                    } else {
                        table.refresh();
                    }
                }
        );

        cName.setCellValueFactory(new PropertyValueFactory<>("Name"));
        cName.setCellFactory(TextFieldTableCell.forTableColumn());
        cName.setOnEditCommit(
                e -> {
                    String newVal = e.getNewValue();
                    int idCurrent = e.getRowValue().getId();

                    try{
                        update("NAME" ,newVal, idCurrent);
                        e.getTableView().getItems()
                                .get(e.getTablePosition().getRow())
                                .setName(newVal);
                    } catch (SQLException e1) {
                        showAlert(
                                Alert.AlertType.ERROR, e1.getMessage()
                        );
                        e.getTableView().refresh();
                    }
                }
        );

        cPos.setCellValueFactory(new PropertyValueFactory<>("Position"));
        cPos.setCellFactory(TextFieldTableCell.forTableColumn());
        cPos.setOnEditCommit(
                e -> {
                    String newVal = e.getNewValue();
                    int idCurrent = e.getRowValue().getId();

                    try{
                        update("POSITION", newVal, idCurrent);
                        e.getTableView().getItems()
                                .get(e.getTablePosition().getRow())
                                .setPosition(newVal);
                    } catch (SQLException e1) {
                        table.refresh();
                        getCleanMessage(e1.getMessage());
                    }
                }
        );

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
            showAlert(Alert.AlertType.ERROR, getCleanMessage(e.getMessage()));
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
            insert(new Employee(idT, nameT, posT));
        } catch (SQLException e) {
            String message = getCleanMessage(e.getMessage());
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

    private void update(String field, String newVal, int idCurrent) throws SQLException {
        String query = "UPDATE OWNER.workers SET %s='%s' WHERE id=%s";
        executeQuery(
                String.format(query, field, newVal, idCurrent)
        );
    }

    private void delete(Employee employee) throws SQLException {
        String query = "DELETE FROM owner.workers WHERE id = %s";
        executeQuery(String.format(query, employee.getId()));
    }

    private void insert(Employee employee) throws SQLException {
        String query = "INSERT into owner.workers VALUES (%s, '%s', '%s')";
        executeQuery(
                String.format(query,
                        employee.getId(),
                        employee.getName(),
                        employee.getPosition())
        );
    }

    private void showAlert(Alert.AlertType alertType, String message){
        Alert alert = new Alert(alertType);
        alert.setTitle("Message");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    private String getCleanMessage(String message){
        Pattern p = Pattern.compile(":(\\d|\\w|\\s)+([\\(]|;|$)");
        Matcher m = p.matcher(message);

        String s = "";
        if (m.find()) {
            s = m.group();
        }
        if (s.equals("")){
            return message;
        }
        if (s.substring(s.length() - 1, s.length()).equals(";") ||
                s.substring(s.length() - 1, s.length()).equals("(")) {
            return s.substring(1, s.length() - 1);
        } else {
            return s.substring(1);
        }
    }
}
