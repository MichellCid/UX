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
    private int idEmpleadoActual;

    public void setIdEmpleadoActual(int idEmpleado) {
        this.idEmpleadoActual = idEmpleado;
        System.out.println("ID empleado recibido en InicioEmpleado: " + idEmpleado);
    }

    public int getIdEmpleadoActual() {
        return idEmpleadoActual;
    }

    @FXML
    private void initialize() {
        ventasEmpleado.setOnAction(e -> cargarPantallaVentas());
        productosEmpleados.setOnAction(e -> cargarPantalla("/farmacia/proyectoux/productosEmpleado.fxml"));
        facturasEmpleados.setOnAction(e -> cargarPantallaFacturas());
        corteEmpleado.setOnAction(e -> cargarPantalla("/farmacia/proyectoux/corteDeCaja.fxml"));

        finSesion.setOnAction(e -> cerrarSesion());
    }

    private void cargarPantallaVentas() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/farmacia/proyectoux/Ventas.fxml"));
            Parent pane = loader.load();

            // Obtener el controlador y pasar el ID del empleado
            Ventas controlador = loader.getController();
            controlador.setIdEmpleadoActual(idEmpleadoActual); // Este debe estar definido como variable en esta clase

            mostrarPantallas.getChildren().setAll(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cargarPantallaFacturas() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/farmacia/proyectoux/facturasEmpleado.fxml"));
            Parent pane = loader.load();

            // Obtener el controlador y pasar el ID del empleado
            FacturasEmpleado controlador = loader.getController();
            controlador.setIdEmpleadoYcargarVentas(idEmpleadoActual); // Este debe estar definido como variable en esta clase

            mostrarPantallas.getChildren().setAll(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
