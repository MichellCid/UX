package farmacia.proyectoux;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDate;

public class FacturasEmpleado {

    @FXML private TableView<VentaObjeto> tablaFacturas;
    @FXML private TableColumn<VentaObjeto, Integer> idF;
    @FXML private TableColumn<VentaObjeto, Integer> numF;
    @FXML private TableColumn<VentaObjeto, String> fechaF;
    @FXML private TableColumn<VentaObjeto, String> productosF;
    @FXML private TableColumn<VentaObjeto, Integer> totalPF;
    @FXML private TableColumn<VentaObjeto, Double> totalF;
    @FXML private TextField buscarF;
    @FXML private DatePicker inicioFF, finFF;
    @FXML private Button verF, imprimirF;

    private ObservableList<VentaObjeto> listaVentas = FXCollections.observableArrayList();
    private int idEmpleadoSesion; // debe ser seteado desde el login

    private int idEmpleadoActual;
    public void setIdEmpleadoActual(int idEmpleado) {
        this.idEmpleadoActual = idEmpleado;
    }
    public int getIdEmpleadoActual(){
        return this.idEmpleadoActual;
    }

    public void initialize() {
        idF.setCellValueFactory(data -> data.getValue().idProperty().asObject());
        numF.setCellValueFactory(data -> data.getValue().idProperty().asObject());
        fechaF.setCellValueFactory(data -> data.getValue().fechaProperty());
        productosF.setCellValueFactory(data -> data.getValue().productosProperty());
        totalPF.setCellValueFactory(data -> data.getValue().totalProductosProperty().asObject());
        totalF.setCellValueFactory(data -> data.getValue().totalVentaProperty().asObject());

        //cargarVentas();
        buscarF.textProperty().addListener((obs, oldVal, newVal) -> filtrar());
        inicioFF.valueProperty().addListener((obs, oldVal, newVal) -> filtrar());
        finFF.valueProperty().addListener((obs, oldVal, newVal) -> filtrar());
    }

    public void setIdEmpleadoYcargarVentas(int idEmpleado) {
        this.idEmpleadoActual = idEmpleado;
        cargarVentas();
    }


    private void cargarVentas() {
        System.out.println("Empleado ID: " + getIdEmpleadoActual());

        listaVentas.clear();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/ux", "Billie", "1234");
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT v.id, v.fecha, v.total, GROUP_CONCAT(p.nombre SEPARATOR ', ') as productos, SUM(dv.cantidad) as total_productos " +
                             "FROM ventas v " +
                             "JOIN detalle_venta dv ON v.id = dv.id_venta " +
                             "JOIN producto p ON dv.id_producto = p.id " +
                             "WHERE v.id_empleado = ? " +
                             "GROUP BY v.id"
             )) {
            stmt.setInt(1, getIdEmpleadoActual());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println("Venta: " + rs.getInt("id") + ", Productos: " + rs.getString("productos"));

                VentaObjeto venta = new VentaObjeto(
                        rs.getInt("id"),
                        rs.getTimestamp("fecha").toLocalDateTime().toString(),
                        rs.getString("productos"),
                        rs.getInt("total_productos"),
                        rs.getDouble("total")
                );
                listaVentas.add(venta);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        tablaFacturas.setItems(listaVentas);
    }

    private void filtrar() {
        String filtro = buscarF.getText().toLowerCase();
        LocalDate desde = inicioFF.getValue();
        LocalDate hasta = finFF.getValue();

        tablaFacturas.setItems(listaVentas.filtered(v -> {
            boolean coincideTexto = v.getProductos().toLowerCase().contains(filtro)
                    || String.valueOf(v.getId()).contains(filtro)
                    || String.valueOf(v.getTotalVenta()).contains(filtro);

            boolean enRango = true;
            if (desde != null) {
                enRango = LocalDate.parse(v.getFecha().substring(0, 10)).compareTo(desde) >= 0;
            }
            if (hasta != null) {
                enRango = enRango && LocalDate.parse(v.getFecha().substring(0, 10)).compareTo(hasta) <= 0;
            }

            return coincideTexto && enRango;
        }));
    }

    @FXML
    private void visualizarDetalle() {
        VentaObjeto venta = tablaFacturas.getSelectionModel().getSelectedItem();
        if (venta == null) {
            mostrarAlerta("Selecciona una venta.");
            return;
        }

        StringBuilder detalle = new StringBuilder("Detalle de Venta:\n");
        detalle.append("Número: ").append(venta.getId()).append("\n");
        detalle.append("Fecha: ").append(venta.getFecha()).append("\n");
        detalle.append("Productos: ").append(venta.getProductos()).append("\n");
        detalle.append("Total: $").append(venta.getTotalVenta()).append("\n");

        mostrarAlerta(detalle.toString());
    }

    @FXML
    private void imprimirTicket() {
        VentaObjeto venta = tablaFacturas.getSelectionModel().getSelectedItem();
        if (venta == null) {
            mostrarAlerta("Selecciona una venta.");
            return;
        }

        String nombreEmpleado = "Desconocido";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/ux", "Billie", "1234");
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT e.nombre FROM ventas v JOIN empleados e ON v.id_empleado = e.id WHERE v.id = ?")) {

            stmt.setInt(1, venta.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                nombreEmpleado = rs.getString("nombre");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar ticket");
            fileChooser.setInitialFileName("ticket_venta_" + venta.getId() + ".txt");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos de texto (*.txt)", "*.txt");
            fileChooser.getExtensionFilters().add(extFilter);

            Stage stage = new Stage();
            java.io.File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                PrintWriter writer = new PrintWriter(new FileWriter(file));
                writer.println("=== TICKET DE VENTA ===");
                writer.println("===     Farmacia      ===");
                writer.println("Venta No: " + venta.getId());
                writer.println("Fecha: " + venta.getFecha());

                writer.println();
                writer.println("Producto      Cantidad   Precio Unitario   Total");
                writer.println("------------------------------------------------");

                try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/ux", "Billie", "1234");
                     PreparedStatement stmt = conn.prepareStatement(
                             "SELECT p.nombre, dv.cantidad, dv.precio_unitario " +
                                     "FROM detalle_venta dv " +
                                     "JOIN producto p ON dv.id_producto = p.id " +
                                     "WHERE dv.id_venta = ?")) {

                    stmt.setInt(1, venta.getId());
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        String nombre = rs.getString("nombre");
                        int cantidad = rs.getInt("cantidad");
                        double precioUnitario = rs.getDouble("precio_unitario");
                        double totalProducto = cantidad * precioUnitario;

                        String linea = String.format("%-12s %8d %15.2f %15.2f",
                                nombre, cantidad, precioUnitario, totalProducto);

                        writer.println(linea);
                    }
                }

                writer.println("------------------------------------------------");
                writer.println("Total a Pagar: $" + venta.getTotalVenta());
                writer.println("Atendido por: " + nombreEmpleado);
                writer.close();
                mostrarAlerta("Ticket generado correctamente.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
