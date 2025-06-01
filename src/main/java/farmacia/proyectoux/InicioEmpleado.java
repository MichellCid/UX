package farmacia.proyectoux;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class InicioEmpleado {

    @FXML private Label usuarioEmpleado;
    @FXML private Label fechaEmpleado;
    @FXML private AnchorPane mostrarPantallas;
    @FXML private Button ventasEmpleado;
    @FXML private Button productosEmpleados;
    @FXML private Button facturasEmpleados;
    @FXML private Button corteEmpleado;
    @FXML private Button finSesion;

    private String usuarioActual;

    public void setUsuarioActual(String usuario) {
        this.usuarioActual = usuario;
        usuarioEmpleado.setText(usuario);
        fechaEmpleado.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    @FXML
    private void initialize() {
        ventasEmpleado.setOnAction(e -> cargarPantalla("/farmacia/proyectoux/Ventas.fxml"));
        productosEmpleados.setOnAction(e -> cargarPantalla("/farmacia/proyectoux/productosEmpleado.fxml"));
        facturasEmpleados.setOnAction(e -> cargarPantalla("/farmacia/proyectoux/facturasEmpleado.fxml"));
        corteEmpleado.setOnAction(e -> cargarPantalla("/farmacia/proyectoux/corteDeCaja.fxml"));

        finSesion.setOnAction(e -> cerrarSesion());
    }

    private void cargarPantalla(String rutaFXML) {
        try {
            Parent pane = FXMLLoader.load(getClass().getResource(rutaFXML));
            mostrarPantallas.getChildren().setAll(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cerrarSesion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/farmacia/proyectoux/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) mostrarPantallas.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
