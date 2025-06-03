package farmacia.proyectoux;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.Optional;

public class ABCEmpleados {

    @FXML private TableView<Empleados> tablaEmpleados;
    @FXML private TableColumn<Empleados, Integer> idET;
    @FXML private TableColumn<Empleados, String> nomET;
    @FXML private TableColumn<Empleados, Integer> telET;
    @FXML private TableColumn<Empleados, String> direcET;
    @FXML private TableColumn<Empleados, String> usuarioET;
    @FXML private TableColumn<Empleados, String> contraseñaET;
    @FXML private TableColumn<Empleados, String> rolET;

    @FXML private TextField nomE;
    @FXML private TextField apellidosE;
    @FXML private TextField telE;
    @FXML private TextField direcE;
    @FXML private TextField ContraseñaE;
    @FXML private TextField usuarioE;
    @FXML private TextField buscarEmpleado;
    @FXML private ComboBox<String> rol;
    @FXML private ComboBox<String> BoxParametros;

    @FXML private Button agregarE;
    @FXML private Button editarE;
    @FXML private Button eliminarE;
    @FXML private Button Buscar;
    @FXML private Button btnAtras;

    private ObservableList<Empleados> listaEmpleados = FXCollections.observableArrayList();
    private Empleados empleadoSeleccionado;

    @FXML
    private void initialize() {
        rol.getItems().addAll("admin", "empleado");
        BoxParametros.getItems().addAll("nombre", "apellidos", "direccion", "usuario", "rol", "mostrar todos");

        configurarTabla();
        cargarEmpleados();

        agregarE.setOnAction(e -> agregarEmpleado());
        editarE.setOnAction(e -> editarEmpleado());
        eliminarE.setOnAction(e -> eliminarEmpleado());
        Buscar.setOnAction(e -> buscarPorParametro());
    }

    private void configurarTabla() {
        idET.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomET.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        telET.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        direcET.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        usuarioET.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        contraseñaET.setCellValueFactory(new PropertyValueFactory<>("contraseña"));
        rolET.setCellValueFactory(new PropertyValueFactory<>("rol"));

        tablaEmpleados.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                empleadoSeleccionado = newSelection;
                llenarCampos(empleadoSeleccionado);
            }
        });
    }

    private void cargarEmpleados() {
        listaEmpleados.clear();
        try (Connection connection = conexionBD.getConexion();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM empleados WHERE estado = 'alta'")) {

            while (resultSet.next()) {
                Empleados empleado = new Empleados(
                        resultSet.getInt("id"),
                        resultSet.getString("nombre"),
                        resultSet.getString("apellidos"),
                        resultSet.getInt("telefono"),
                        resultSet.getString("direccion"),
                        resultSet.getString("estado"),
                        resultSet.getString("usuario"),
                        resultSet.getString("contraseña"),
                        resultSet.getString("rol")
                );
                listaEmpleados.add(empleado);
            }

            tablaEmpleados.setItems(listaEmpleados);
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los empleados", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void agregarEmpleado() {
        if (!validarCampos()) return;

        try (Connection connection = conexionBD.getConexion();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO empleados (nombre, apellidos, telefono, direccion, estado, usuario, contraseña, rol) VALUES (?, ?, ?, ?, 'alta', ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, nomE.getText());
            statement.setString(2, apellidosE.getText());
            statement.setInt(3, Integer.parseInt(telE.getText()));
            statement.setString(4, direcE.getText());
            statement.setString(5, usuarioE.getText());
            statement.setString(6, ContraseñaE.getText());
            statement.setString(7, rol.getValue());

            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = statement.getGeneratedKeys();
                if (rs.next()) {
                    int id = rs.getInt(1);
                    Empleados nuevo = new Empleados(id, nomE.getText(), apellidosE.getText(), Integer.parseInt(telE.getText()),
                            direcE.getText(), "alta", usuarioE.getText(), ContraseñaE.getText(), rol.getValue());
                    listaEmpleados.add(nuevo);
                    mostrarAlerta("Éxito", "Empleado agregado correctamente", Alert.AlertType.INFORMATION);
                    limpiarCampos();
                }
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo agregar el empleado", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void editarEmpleado() {
        if (empleadoSeleccionado == null) {
            mostrarAlerta("Advertencia", "Selecciona un empleado para editar", Alert.AlertType.WARNING);
            return;
        }

        if (!validarCampos()) return;

        try (Connection connection = conexionBD.getConexion();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE empleados SET nombre = ?, apellidos = ?, telefono = ?, direccion = ?, usuario = ?, contraseña = ?, rol = ? WHERE id = ?")) {

            statement.setString(1, nomE.getText());
            statement.setString(2, apellidosE.getText());
            statement.setInt(3, Integer.parseInt(telE.getText()));
            statement.setString(4, direcE.getText());
            statement.setString(5, usuarioE.getText());
            statement.setString(6, ContraseñaE.getText());
            statement.setString(7, rol.getValue());
            statement.setInt(8, empleadoSeleccionado.getId());

            if (statement.executeUpdate() > 0) {
                cargarEmpleados();
                mostrarAlerta("Éxito", "Empleado actualizado correctamente", Alert.AlertType.INFORMATION);
                limpiarCampos();
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo actualizar el empleado", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void eliminarEmpleado() {
        if (empleadoSeleccionado == null) {
            mostrarAlerta("Advertencia", "Selecciona un empleado para eliminar", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Estás seguro de eliminar este empleado?");
        Optional<ButtonType> resultado = confirmacion.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try (Connection connection = conexionBD.getConexion();
                 PreparedStatement statement = connection.prepareStatement("UPDATE empleados SET estado = 'baja' WHERE id = ?")) {

                statement.setInt(1, empleadoSeleccionado.getId());
                if (statement.executeUpdate() > 0) {
                    listaEmpleados.remove(empleadoSeleccionado);
                    mostrarAlerta("Éxito", "Empleado eliminado (baja lógica)", Alert.AlertType.INFORMATION);
                    limpiarCampos();
                }
            } catch (SQLException e) {
                mostrarAlerta("Error", "No se pudo eliminar el empleado", Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    private boolean validarCampos() {
        if (nomE.getText().isEmpty() || apellidosE.getText().isEmpty() || telE.getText().isEmpty() ||
                direcE.getText().isEmpty() || usuarioE.getText().isEmpty() || ContraseñaE.getText().isEmpty() || rol.getValue() == null) {
            mostrarAlerta("Campos incompletos", "Todos los campos deben estar llenos", Alert.AlertType.WARNING);
            return false;
        }
        try {
            Integer.parseInt(telE.getText());
        } catch (NumberFormatException e) {
            mostrarAlerta("Teléfono inválido", "El teléfono debe ser un número válido", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private void llenarCampos(Empleados emp) {
        nomE.setText(emp.getNombre());
        apellidosE.setText(emp.getApellido());
        telE.setText(String.valueOf(emp.getTelefono()));
        direcE.setText(emp.getDireccion());
        usuarioE.setText(emp.getUsuario());
        ContraseñaE.setText(emp.getContraseña());
        rol.setValue(emp.getRol());
    }

    private void limpiarCampos() {
        nomE.clear();
        apellidosE.clear();
        telE.clear();
        direcE.clear();
        usuarioE.clear();
        ContraseñaE.clear();
        rol.setValue(null);
        empleadoSeleccionado = null;
        tablaEmpleados.getSelectionModel().clearSelection();
    }

    private void mostrarAlerta(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }

    private void buscarPorParametro() {
        String parametro = BoxParametros.getValue();
        String valorBusqueda = buscarEmpleado.getText().trim();

        if (parametro == null) {
            mostrarAlerta("Parámetro inválido", "Debes seleccionar un parámetro.", Alert.AlertType.WARNING);
            return;
        }

        listaEmpleados.clear();

        if (parametro.equalsIgnoreCase("mostrar todos")) {
            cargarEmpleados();
            return;
        }

        if (valorBusqueda.isEmpty()) {
            mostrarAlerta("Búsqueda vacía", "Escribe algo para buscar.", Alert.AlertType.WARNING);
            return;
        }

        String columnaBD = parametro.toLowerCase();

        String consulta = "SELECT * FROM empleados WHERE estado = 'alta' AND " + columnaBD + " LIKE ?";

        try (Connection connection = conexionBD.getConexion();
             PreparedStatement statement = connection.prepareStatement(consulta)) {

            statement.setString(1, "%" + valorBusqueda + "%");

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Empleados empleado = new Empleados(
                        resultSet.getInt("id"),
                        resultSet.getString("nombre"),
                        resultSet.getString("apellidos"),
                        resultSet.getInt("telefono"),
                        resultSet.getString("direccion"),
                        resultSet.getString("estado"),
                        resultSet.getString("usuario"),
                        resultSet.getString("contraseña"),
                        resultSet.getString("rol")
                );
                listaEmpleados.add(empleado);
            }

            tablaEmpleados.setItems(listaEmpleados);

            if (listaEmpleados.isEmpty()) {
                mostrarAlerta("Sin resultados", "No se encontraron empleados con ese criterio.", Alert.AlertType.INFORMATION);
            }

        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo realizar la búsqueda", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void regresarInicio() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("InicioAdmin.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnAtras.getScene().getWindow();
            Scene nuevaEscena = new Scene(root);
            stage.setScene(nuevaEscena);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al cargar la pantalla de inicio: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}
