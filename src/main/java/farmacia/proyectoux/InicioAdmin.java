package farmacia.proyectoux;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class InicioAdmin {

    @FXML private AnchorPane mostrarPantallas;
    @FXML private Button ventasAdmin;
    @FXML private Button productosAdmin;
    @FXML private Button empleadosAdmin;
    @FXML private Button cortesAdmin;
    @FXML private Button reportesAdmin;
    @FXML private Button cajaAdmin;
    @FXML private Button finSesion;
    @FXML private Label usuario;
    @FXML private Label fecha;

    private String usuarioActual;

    public void setUsuarioActual(String usuarioNombre) {
        this.usuarioActual = usuarioNombre;
        usuario.setText(usuarioNombre);
    }

    @FXML
    public void initialize() {
        // Muestra la fecha actual
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        fecha.setText(LocalDate.now().format(formatter));

        // Eventos de botones para cargar pantallas
        ventasAdmin.setOnAction(e -> cargarPantalla("/farmacia/proyectoux/ventasAdmin.fxml"));
        productosAdmin.setOnAction(e -> cargarPantalla("/farmacia/proyectoux/ABCProducto.fxml"));
        empleadosAdmin.setOnAction(e -> cargarPantalla("/farmacia/proyectoux/ABCEmpleados.fxml"));
        cortesAdmin.setOnAction(e -> cargarPantalla("/farmacia/proyectoux/cortesAdmin.fxml"));
        reportesAdmin.setOnAction(e -> cargarPantalla("/farmacia/proyectoux/reportes.fxml"));
        //cajaAdmin.setOnAction(e -> cargarPantalla("/farmacia/proyectoux/caja.fxml"));

        // Cerrar sesión
        finSesion.setOnAction(e -> cerrarSesion());
    }

    private void cargarPantalla(String rutaFXML) {
        try {
            Parent nuevaVista = FXMLLoader.load(getClass().getResource(rutaFXML));
            mostrarPantallas.getChildren().clear();
            mostrarPantallas.getChildren().add(nuevaVista);
            AnchorPane.setTopAnchor(nuevaVista, 0.0);
            AnchorPane.setBottomAnchor(nuevaVista, 0.0);
            AnchorPane.setLeftAnchor(nuevaVista, 0.0);
            AnchorPane.setRightAnchor(nuevaVista, 0.0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cerrarSesion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/farmacia/proyectoux/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) finSesion.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
