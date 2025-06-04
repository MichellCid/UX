package farmacia.proyectoux;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Reportes {

    @FXML private TableView<EmpleadoReporte> tablaEmpleados;
    @FXML private TableColumn<EmpleadoReporte, Integer> idEmpleado;
    @FXML private TableColumn<EmpleadoReporte, String> NombreEmpleado, ApellidosCol, PuestoCol;
    @FXML private TableColumn<EmpleadoReporte, String> FechaIn, HoraIn, Horacierre;
    @FXML private TableColumn<EmpleadoReporte, Double> TotalVen;
    @FXML private DatePicker FechaInicio, FechaFin;
    @FXML private Button actualizarButton;

    private ObservableList<EmpleadoReporte> listaEmpleados = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            // Configurar las columnas de la tabla
            idEmpleado.setCellValueFactory(new PropertyValueFactory<>("id"));
            NombreEmpleado.setCellValueFactory(new PropertyValueFactory<>("nombre"));
            ApellidosCol.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
            PuestoCol.setCellValueFactory(new PropertyValueFactory<>("rol"));
            FechaIn.setCellValueFactory(new PropertyValueFactory<>("fechaIngreso"));
            HoraIn.setCellValueFactory(new PropertyValueFactory<>("horaIngreso"));
            Horacierre.setCellValueFactory(new PropertyValueFactory<>("horaCierre"));
            TotalVen.setCellValueFactory(new PropertyValueFactory<>("totalVendido"));

            // Configurar valores por defecto para los DatePicker
            FechaInicio.setValue(LocalDate.now().minusDays(7));
            FechaFin.setValue(LocalDate.now());

            // Configurar el botón de actualización
            actualizarButton.setOnAction(event -> cargarDatosEmpleados());

            // Cargar datos iniciales
            cargarDatosEmpleados();
        } catch (Exception e) {
            mostrarAlerta("Error al inicializar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Connection conectar() throws SQLException {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost/ux", "Billie", "1234");
        } catch (SQLException e) {
            mostrarAlerta("Error de conexión a la base de datos");
            throw e;
        }
    }

    private void cargarDatosEmpleados() {
        try {
            listaEmpleados.clear();

            LocalDate fechaInicio = FechaInicio.getValue();
            LocalDate fechaFin = FechaFin.getValue();

            if (fechaInicio == null || fechaFin == null) {
                mostrarAlerta("Seleccione ambas fechas para filtrar.");
                return;
            }

            if (fechaInicio.isAfter(fechaFin)) {
                mostrarAlerta("La fecha de inicio no puede ser posterior a la fecha fin.");
                return;
            }

            String sql = "SELECT e.id, e.nombre, e.apellidos, e.rol, " +
                    "DATE(MIN(v.fecha)) as fecha_ingreso, " +
                    "TIME(MIN(v.fecha)) as hora_ingreso, " +
                    "TIME(MAX(v.fecha)) as hora_cierre, " +
                    "COALESCE(SUM(v.total), 0) as total_vendido " +
                    "FROM empleados e " +
                    "LEFT JOIN ventas v ON e.id = v.id_empleado AND DATE(v.fecha) BETWEEN ? AND ? " +
                    "GROUP BY e.id, e.nombre, e.apellidos, e.rol " +
                    "ORDER BY e.id";

            try (Connection conn = conectar();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, fechaInicio.format(DateTimeFormatter.ISO_DATE));
                stmt.setString(2, fechaFin.format(DateTimeFormatter.ISO_DATE));

                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String fechaIngreso = rs.getString("fecha_ingreso");
                    String horaIngreso = rs.getString("hora_ingreso");
                    String horaCierre = rs.getString("hora_cierre");

                    if (fechaIngreso == null) {
                        fechaIngreso = "Sin ventas";
                        horaIngreso = "";
                        horaCierre = "";
                    }

                    listaEmpleados.add(new EmpleadoReporte(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("apellidos"),
                            rs.getString("rol"),
                            fechaIngreso,
                            horaIngreso,
                            horaCierre,
                            rs.getDouble("total_vendido")
                    ));
                }
            }

            tablaEmpleados.setItems(listaEmpleados);
        } catch (Exception e) {
            mostrarAlerta("Error al cargar datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public static class EmpleadoReporte {
        private final int id;
        private final String nombre;
        private final String apellidos;
        private final String rol;
        private final String fechaIngreso;
        private final String horaIngreso;
        private final String horaCierre;
        private final double totalVendido;

        public EmpleadoReporte(int id, String nombre, String apellidos, String rol,
                               String fechaIngreso, String horaIngreso, String horaCierre,
                               double totalVendido) {
            this.id = id;
            this.nombre = nombre;
            this.apellidos = apellidos;
            this.rol = rol;
            this.fechaIngreso = fechaIngreso;
            this.horaIngreso = horaIngreso;
            this.horaCierre = horaCierre;
            this.totalVendido = totalVendido;
        }

        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public String getApellidos() { return apellidos; }
        public String getRol() { return rol; }
        public String getFechaIngreso() { return fechaIngreso; }
        public String getHoraIngreso() { return horaIngreso; }
        public String getHoraCierre() { return horaCierre; }
        public double getTotalVendido() { return totalVendido; }
    }
}