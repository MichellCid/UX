package farmacia.proyectoux;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.sql.*;

public class Login {
    @FXML
    private ComboBox<String> usuarioLogin;

    @FXML
    private TextField contraseñaLogin;

    // Se ejecuta automáticamente al cargar la ventana
    @FXML
    private void initialize() {
        cargarUsuarios();
    }

    // Cargar usuarios activos (estado = 'alta') en el ComboBox
    private void cargarUsuarios() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ux", "Billie", "1234");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT usuario FROM empleados WHERE estado = 'alta'")) {

            while (rs.next()) {
                usuarioLogin.getItems().add(rs.getString("usuario"));
            }

        } catch (SQLException e) {
            mostrarError("Error al cargar los usuarios: " + e.getMessage());
        }
    }

    // Acción al hacer clic en "Entrar"
    @FXML
    private void handleIniciarSesion(ActionEvent event) {
        String usuario = usuarioLogin.getValue();
        String contraseña = contraseñaLogin.getText();

        if (usuario == null || contraseña.isEmpty()) {
            mostrarError("Por favor selecciona un usuario e ingresa la contraseña.");
            return;
        }

        // Validar las credenciales
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ux", "Billie", "1234");
             PreparedStatement stmt = conn.prepareStatement("SELECT rol FROM empleados WHERE usuario = ? AND contraseña = ? AND estado = 'alta'")) {

            stmt.setString(1, usuario);
            stmt.setString(2, contraseña);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String rol = rs.getString("rol");

                // Determinar la vista según el rol
                String fxmlDestino = rol.equals("admin") ? "/farmacia/proyectoux/inicioAdmin.fxml" : "/farmacia/proyectoux/inicioEmpleado.fxml";

                // Cargar nueva vista
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlDestino));
                Scene nuevaEscena = new Scene(loader.load());

                if (rol.equals("admin")) {
                    InicioAdmin controlador = loader.getController();
                    controlador.setUsuarioActual(usuario);  // método que debes tener en InicioAdmin
                } else {
                    InicioEmpleado controlador = loader.getController();
                    controlador.setUsuarioActual(usuario);  // método que debes tener en InicioEmpleado
                }

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(nuevaEscena);
                stage.show();

            } else {
                mostrarError("Usuario o contraseña incorrectos, o el usuario está dado de baja.");
            }

        } catch (Exception e) {
            mostrarError("Error al iniciar sesión: " + e.getMessage());
        }
    }

    // Mostrar alertas de error
    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }



}
