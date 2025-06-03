package farmacia.proyectoux;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class VentasAdmin {

    @FXML
    private TableView<Venta> tablaVentas;
    @FXML
    private TableColumn<Venta, Integer> colId;
    @FXML
    private TableColumn<Venta, String> colUsuario;
    @FXML
    private TableColumn<Venta, String> colFecha;
    @FXML
    private TableColumn<Venta, String> colHora;
    @FXML
    private TableColumn<Venta, Double> colTotal;
    @FXML
    private DatePicker dateDesde;
    @FXML
    private DatePicker dateHasta;
    @FXML
    private TextField campoBusqueda;
    @FXML
    private Button btnMostrarDetalles;
    @FXML
    private Button btnCancelarVenta;

    private ObservableList<Venta> listaVentas = FXCollections.observableArrayList();

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost/ux", "Billie", "1234");
    }

    @FXML
    public void initialize() {
        // Configurar las columnas de la tabla
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colHora.setCellValueFactory(new PropertyValueFactory<>("hora"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        // Cargar todas las ventas al iniciar
        cargarVentas();

        // Configurar listeners para los filtros
        dateDesde.valueProperty().addListener((obs, oldVal, newVal) -> filtrarVentas());
        dateHasta.valueProperty().addListener((obs, oldVal, newVal) -> filtrarVentas());
        campoBusqueda.textProperty().addListener((obs, oldVal, newVal) -> buscarVentas(newVal));
    }

    private void cargarVentas() {
        listaVentas.clear();
        try (Connection conn = conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT v.id, CONCAT(e.nombre, ' ', e.apellidos) AS usuario, " +
                             "v.fecha, v.total " +
                             "FROM ventas v " +
                             "JOIN empleados e ON v.id_empleado = e.id " +
                             "ORDER BY v.fecha DESC")) {

            while (rs.next()) {
                Timestamp fechaHora = rs.getTimestamp("fecha");
                String fecha = fechaHora.toLocalDateTime().toLocalDate().toString();
                String hora = fechaHora.toLocalDateTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

                listaVentas.add(new Venta(
                        rs.getInt("id"),
                        rs.getString("usuario"),
                        fecha,
                        hora,
                        rs.getDouble("total")
                ));
            }
            tablaVentas.setItems(listaVentas);
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al cargar las ventas: " + e.getMessage());
        }
    }

    private void filtrarVentas() {
        LocalDate desde = dateDesde.getValue();
        LocalDate hasta = dateHasta.getValue();

        if (desde != null && hasta != null) {
            if (desde.isAfter(hasta)) {
                mostrarAlerta("La fecha 'Desde' no puede ser posterior a la fecha 'Hasta'");
                return;
            }

            try (Connection conn = conectar();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT v.id, CONCAT(e.nombre, ' ', e.apellidos) AS usuario, " +
                                 "v.fecha, v.total " +
                                 "FROM ventas v " +
                                 "JOIN empleados e ON v.id_empleado = e.id " +
                                 "WHERE DATE(v.fecha) BETWEEN ? AND ? " +
                                 "ORDER BY v.fecha DESC")) {

                stmt.setDate(1, Date.valueOf(desde));
                stmt.setDate(2, Date.valueOf(hasta));

                ResultSet rs = stmt.executeQuery();
                listaVentas.clear();

                while (rs.next()) {
                    Timestamp fechaHora = rs.getTimestamp("fecha");
                    String fecha = fechaHora.toLocalDateTime().toLocalDate().toString();
                    String hora = fechaHora.toLocalDateTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

                    listaVentas.add(new Venta(
                            rs.getInt("id"),
                            rs.getString("usuario"),
                            fecha,
                            hora,
                            rs.getDouble("total")
                    ));
                }
                tablaVentas.setItems(listaVentas);
            } catch (SQLException e) {
                e.printStackTrace();
                mostrarAlerta("Error al filtrar ventas: " + e.getMessage());
            }
        }
    }

    private void buscarVentas(String criterio) {
        if (criterio == null || criterio.trim().isEmpty()) {
            cargarVentas();
            return;
        }

        try (Connection conn = conectar();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT v.id, CONCAT(e.nombre, ' ', e.apellidos) AS usuario, " +
                             "v.fecha, v.total " +
                             "FROM ventas v " +
                             "JOIN empleados e ON v.id_empleado = e.id " +
                             "WHERE v.id LIKE ? OR e.nombre LIKE ? OR e.apellidos LIKE ? " +
                             "ORDER BY v.fecha DESC")) {

            stmt.setString(1, "%" + criterio + "%");
            stmt.setString(2, "%" + criterio + "%");
            stmt.setString(3, "%" + criterio + "%");

            ResultSet rs = stmt.executeQuery();
            listaVentas.clear();

            while (rs.next()) {
                Timestamp fechaHora = rs.getTimestamp("fecha");
                String fecha = fechaHora.toLocalDateTime().toLocalDate().toString();
                String hora = fechaHora.toLocalDateTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

                listaVentas.add(new Venta(
                        rs.getInt("id"),
                        rs.getString("usuario"),
                        fecha,
                        hora,
                        rs.getDouble("total")
                ));
            }
            tablaVentas.setItems(listaVentas);
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al buscar ventas: " + e.getMessage());
        }
    }

    @FXML
    private void mostrarDetallesVenta() {
        Venta seleccionada = tablaVentas.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            try (Connection conn = conectar();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT p.nombre, p.descripcion, dv.cantidad, dv.precio_unitario, dv.total " +
                                 "FROM detalle_venta dv " +
                                 "JOIN producto p ON dv.id_producto = p.id " +
                                 "WHERE dv.id_venta = ?")) {

                stmt.setInt(1, seleccionada.getId());
                ResultSet rs = stmt.executeQuery();

                StringBuilder detalles = new StringBuilder();
                detalles.append("Detalles de la venta #").append(seleccionada.getId()).append("\n\n");
                detalles.append(String.format("%-30s %-10s %-10s %-10s%n",
                        "Producto", "Cantidad", "P. Unit.", "Total"));
                detalles.append("------------------------------------------------------------\n");

                while (rs.next()) {
                    detalles.append(String.format("%-30s %-10d $%-9.2f $%-9.2f%n",
                            rs.getString("nombre"),
                            rs.getInt("cantidad"),
                            rs.getDouble("precio_unitario"),
                            rs.getDouble("total")));
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Detalles de Venta");
                alert.setHeaderText(null);
                alert.setContentText(detalles.toString());
                alert.showAndWait();

            } catch (SQLException e) {
                e.printStackTrace();
                mostrarAlerta("Error al obtener detalles: " + e.getMessage());
            }
        } else {
            mostrarAlerta("Selecciona una venta para ver los detalles.");
        }
    }

    @FXML
    private void cancelarVentaSeleccionada() {
        Venta seleccionada = tablaVentas.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar cancelación");
            confirmacion.setHeaderText("Cancelar venta #" + seleccionada.getId());
            confirmacion.setContentText("¿Estás seguro de que deseas cancelar esta venta? Esta acción no se puede deshacer.");

            if (confirmacion.showAndWait().get() == ButtonType.OK) {
                try (Connection conn = conectar()) {
                    conn.setAutoCommit(false);

                    // 1. Obtener los detalles de la venta para restablecer el stock
                    try (PreparedStatement stmtDetalles = conn.prepareStatement(
                            "SELECT id_producto, cantidad FROM detalle_venta WHERE id_venta = ?")) {
                        stmtDetalles.setInt(1, seleccionada.getId());
                        ResultSet rs = stmtDetalles.executeQuery();

                        // 2. Restablecer el stock de cada producto
                        while (rs.next()) {
                            try (PreparedStatement stmtStock = conn.prepareStatement(
                                    "UPDATE producto SET cantidad = cantidad + ? WHERE id = ?")) {
                                stmtStock.setInt(1, rs.getInt("cantidad"));
                                stmtStock.setInt(2, rs.getInt("id_producto"));
                                stmtStock.executeUpdate();
                            }
                        }
                    }

                    // 3. Eliminar los detalles de la venta
                    try (PreparedStatement stmtEliminarDetalles = conn.prepareStatement(
                            "DELETE FROM detalle_venta WHERE id_venta = ?")) {
                        stmtEliminarDetalles.setInt(1, seleccionada.getId());
                        stmtEliminarDetalles.executeUpdate();
                    }

                    // 4. Eliminar la venta
                    try (PreparedStatement stmtEliminarVenta = conn.prepareStatement(
                            "DELETE FROM ventas WHERE id = ?")) {
                        stmtEliminarVenta.setInt(1, seleccionada.getId());
                        stmtEliminarVenta.executeUpdate();
                    }

                    conn.commit();
                    mostrarAlerta("Venta cancelada exitosamente.");
                    cargarVentas(); // Refrescar la tabla

                } catch (SQLException e) {
                    e.printStackTrace();
                    mostrarAlerta("Error al cancelar la venta: " + e.getMessage());
                }
            }
        } else {
            mostrarAlerta("Selecciona una venta para cancelar.");
        }
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mensaje");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Clase interna para representar las ventas en la tabla
    public static class Venta {
        private final int id;
        private final String usuario;
        private final String fecha;
        private final String hora;
        private final double total;

        public Venta(int id, String usuario, String fecha, String hora, double total) {
            this.id = id;
            this.usuario = usuario;
            this.fecha = fecha;
            this.hora = hora;
            this.total = total;
        }

        public int getId() { return id; }
        public String getUsuario() { return usuario; }
        public String getFecha() { return fecha; }
        public String getHora() { return hora; }
        public double getTotal() { return total; }
    }
}