package farmacia.proyectoux;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.stage.FileChooser;

public class Reportes {

    // Componentes para reporte de empleados
    @FXML private TableView<EmpleadoReporte> tablaEmpleados;
    @FXML private TableColumn<EmpleadoReporte, Integer> idEmpleado;
    @FXML private TableColumn<EmpleadoReporte, String> NombreEmpleado, ApellidosCol, PuestoCol;
    @FXML private TableColumn<EmpleadoReporte, String> FechaIn, HoraIn, Horacierre;
    @FXML private TableColumn<EmpleadoReporte, Double> TotalVen;
    @FXML private DatePicker FechaInicio, FechaFin;
    @FXML private Button actualizarButton;
    @FXML private Button btnImprimir;

    // Componentes para reporte de ventas
    @FXML private TableView<VentaReporte> TablaVentas;
    @FXML private TableColumn<VentaReporte, Integer> IDcol;
    @FXML private TableColumn<VentaReporte, String> VendedorCol, ProductosCol, DescripcionCol;
    @FXML private TableColumn<VentaReporte, Integer> CantidadCol;
    @FXML private TableColumn<VentaReporte, Double> TotalCol;
    @FXML private DatePicker FechaInicialVen, fechaFinalVen;
    @FXML private TextArea ObservacionesVen;
    @FXML private Button btnImprimirVen;

    // Componentes para reporte de productos
    @FXML private TableView<ProductoObjeto> tablaProductos;
    @FXML private TableColumn<ProductoObjeto, Integer> IdProductoCol, CodigoProductoCol, CantidadProductoCol;
    @FXML private TableColumn<ProductoObjeto, String> NombreProductoCol, DescripcionProductoCol, CategoriaProductoCol;
    @FXML private TableColumn<ProductoObjeto, Double> PrecioComProductoCol, PrecioVenProductoCol;
    @FXML private DatePicker FechaInicioProductos, FechaFinProductos;
    @FXML private TextArea descripcionesproductos;
    @FXML private Button ImprimirProductos;

    private ObservableList<EmpleadoReporte> listaEmpleados = FXCollections.observableArrayList();
    private ObservableList<VentaReporte> listaVentas = FXCollections.observableArrayList();
    private ObservableList<ProductoObjeto> listaProductos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configuración para empleados
        configurarTablaEmpleados();
        FechaInicio.setValue(LocalDate.now().minusDays(7));
        FechaFin.setValue(LocalDate.now());
        actualizarButton.setOnAction(event -> cargarDatosEmpleados());
        btnImprimir.setOnAction(event -> imprimirReporteEmpleados());
        cargarDatosEmpleados();

        // Configuración para ventas
        configurarTablaVentas();
        FechaInicialVen.setValue(LocalDate.now().minusDays(7));
        fechaFinalVen.setValue(LocalDate.now());
        btnImprimirVen.setOnAction(event -> imprimirReporteVentas());

        // Configuración para productos
        configurarTablaProductos();
        FechaInicioProductos.setValue(LocalDate.now().minusDays(7));
        FechaFinProductos.setValue(LocalDate.now());
        ImprimirProductos.setOnAction(event -> imprimirReporteProductos());

        // Listeners para actualización automática
        FechaInicialVen.valueProperty().addListener((obs, oldVal, newVal) -> cargarDatosVentas());
        fechaFinalVen.valueProperty().addListener((obs, oldVal, newVal) -> cargarDatosVentas());
        FechaInicioProductos.valueProperty().addListener((obs, oldVal, newVal) -> cargarDatosProductos());
        FechaFinProductos.valueProperty().addListener((obs, oldVal, newVal) -> cargarDatosProductos());

        cargarDatosVentas();
        cargarDatosProductos();
    }

    private void configurarTablaEmpleados() {
        idEmpleado.setCellValueFactory(new PropertyValueFactory<>("id"));
        NombreEmpleado.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        ApellidosCol.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        PuestoCol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        FechaIn.setCellValueFactory(new PropertyValueFactory<>("fechaIngreso"));
        HoraIn.setCellValueFactory(new PropertyValueFactory<>("horaIngreso"));
        Horacierre.setCellValueFactory(new PropertyValueFactory<>("horaCierre"));
        TotalVen.setCellValueFactory(new PropertyValueFactory<>("totalVendido"));
    }

    private void configurarTablaVentas() {
        IDcol.setCellValueFactory(new PropertyValueFactory<>("id"));
        VendedorCol.setCellValueFactory(new PropertyValueFactory<>("vendedor"));
        ProductosCol.setCellValueFactory(new PropertyValueFactory<>("productos"));
        CantidadCol.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        DescripcionCol.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        TotalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
    }

    private void configurarTablaProductos() {
        IdProductoCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        CodigoProductoCol.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        NombreProductoCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        DescripcionProductoCol.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        PrecioComProductoCol.setCellValueFactory(new PropertyValueFactory<>("precioCompra"));
        PrecioVenProductoCol.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        CantidadProductoCol.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        CategoriaProductoCol.setCellValueFactory(new PropertyValueFactory<>("categoria"));
    }

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost/ux", "Billie", "1234");
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
            mostrarAlerta("Error al cargar datos de empleados: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cargarDatosVentas() {
        try {
            listaVentas.clear();
            LocalDate fechaInicio = FechaInicialVen.getValue();
            LocalDate fechaFin = fechaFinalVen.getValue();

            if (fechaInicio == null || fechaFin == null) {
                mostrarAlerta("Seleccione ambas fechas para filtrar.");
                return;
            }

            if (fechaInicio.isAfter(fechaFin)) {
                mostrarAlerta("La fecha de inicio no puede ser posterior a la fecha fin.");
                return;
            }

            String sql = "SELECT v.id, CONCAT(e.nombre, ' ', e.apellidos) AS vendedor, " +
                    "GROUP_CONCAT(p.nombre SEPARATOR ', ') AS productos, " +
                    "SUM(dv.cantidad) AS cantidad, " +
                    "GROUP_CONCAT(p.descripcion SEPARATOR '; ') AS descripcion, " +
                    "v.total " +
                    "FROM ventas v " +
                    "JOIN empleados e ON v.id_empleado = e.id " +
                    "JOIN detalle_venta dv ON v.id = dv.id_venta " +
                    "JOIN producto p ON dv.id_producto = p.id " +
                    "WHERE DATE(v.fecha) BETWEEN ? AND ? " +
                    "GROUP BY v.id, vendedor, v.total " +
                    "ORDER BY v.fecha";

            try (Connection conn = conectar();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, fechaInicio.format(DateTimeFormatter.ISO_DATE));
                stmt.setString(2, fechaFin.format(DateTimeFormatter.ISO_DATE));

                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    listaVentas.add(new VentaReporte(
                            rs.getInt("id"),
                            rs.getString("vendedor"),
                            rs.getString("productos"),
                            rs.getInt("cantidad"),
                            rs.getString("descripcion"),
                            rs.getDouble("total")
                    ));
                }
            }

            TablaVentas.setItems(listaVentas);
        } catch (Exception e) {
            mostrarAlerta("Error al cargar datos de ventas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cargarDatosProductos() {
        try {
            listaProductos.clear();
            LocalDate fechaInicio = FechaInicioProductos.getValue();
            LocalDate fechaFin = FechaFinProductos.getValue();

            if (fechaInicio == null || fechaFin == null) {
                mostrarAlerta("Seleccione ambas fechas para filtrar.");
                return;
            }

            if (fechaInicio.isAfter(fechaFin)) {
                mostrarAlerta("La fecha de inicio no puede ser posterior a la fecha fin.");
                return;
            }

            // Consulta modificada - versión sin filtro por fecha
            String sql = "SELECT p.id, p.codigo, p.nombre, p.descripcion, p.precioCompra, " +
                    "p.precioVenta, p.cantidad, c.nombre as categoria " +
                    "FROM producto p " +
                    "LEFT JOIN categoria c ON p.categoria = c.id " +
                    "ORDER BY p.nombre"; // Ordenamos por nombre como alternativa

            try (Connection conn = conectar();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    listaProductos.add(new ProductoObjeto(
                            rs.getInt("id"),
                            rs.getInt("codigo"),
                            rs.getString("nombre"),
                            rs.getString("descripcion"),
                            rs.getDouble("precioCompra"),
                            rs.getDouble("precioVenta"),
                            rs.getInt("cantidad"),
                            rs.getString("categoria"),
                            "", // ubicación vacía ya que no la mostramos en el reporte
                            true // estado activo por defecto
                    ));
                }
            }

            tablaProductos.setItems(listaProductos);
        } catch (Exception e) {
            mostrarAlerta("Error al cargar datos de productos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void imprimirReporteEmpleados() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Reporte de Empleados");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de Texto", "*.txt"));
            fileChooser.setInitialFileName("Reporte_Empleados_" + LocalDate.now() + ".txt");
            File file = fileChooser.showSaveDialog(tablaEmpleados.getScene().getWindow());

            if (file != null) {
                StringBuilder contenido = new StringBuilder();
                contenido.append("REPORTE DE EMPLEADOS\n");
                contenido.append("=====================\n\n");
                contenido.append("Período: ").append(FechaInicio.getValue()).append(" al ").append(FechaFin.getValue()).append("\n\n");

                contenido.append(String.format("%-5s %-20s %-20s %-15s %-12s %-10s %-10s %-15s%n",
                        "ID", "Nombre", "Apellidos", "Puesto", "F. Ingreso", "H. Ingreso", "H. Cierre", "Total Vendido"));
                contenido.append(String.format("%-5s %-20s %-20s %-15s %-12s %-10s %-10s %-15s%n",
                        "----", "--------------------", "--------------------", "---------------", "------------", "----------", "----------", "---------------"));

                for (EmpleadoReporte empleado : listaEmpleados) {
                    contenido.append(String.format("%-5d %-20s %-20s %-15s %-12s %-10s %-10s $%-14.2f%n",
                            empleado.getId(),
                            empleado.getNombre(),
                            empleado.getApellidos(),
                            empleado.getRol(),
                            empleado.getFechaIngreso(),
                            empleado.getHoraIngreso(),
                            empleado.getHoraCierre(),
                            empleado.getTotalVendido()));
                }

                Files.write(file.toPath(), contenido.toString().getBytes(StandardCharsets.UTF_8));
                mostrarAlerta("Éxito", "Reporte generado exitosamente en: " + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
            }
        } catch (IOException e) {
            mostrarAlerta("Error", "Error al generar TXT: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void imprimirReporteVentas() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Reporte de Ventas");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de Texto", "*.txt"));
            fileChooser.setInitialFileName("Reporte_Ventas_" + LocalDate.now() + ".txt");
            File file = fileChooser.showSaveDialog(TablaVentas.getScene().getWindow());

            if (file != null) {
                StringBuilder contenido = new StringBuilder();
                contenido.append("REPORTE DE VENTAS\n");
                contenido.append("================\n\n");
                contenido.append("Período: ").append(FechaInicialVen.getValue()).append(" al ").append(fechaFinalVen.getValue()).append("\n\n");

                if (!ObservacionesVen.getText().isEmpty()) {
                    contenido.append("Observaciones: ").append(ObservacionesVen.getText()).append("\n\n");
                }

                contenido.append(String.format("%-8s %-25s %-40s %-10s %-50s %-10s%n",
                        "ID", "Vendedor", "Productos", "Cantidad", "Descripción", "Total"));
                contenido.append(String.format("%-8s %-25s %-40s %-10s %-50s %-10s%n",
                        "--------", "-------------------------", "----------------------------------------",
                        "----------", "--------------------------------------------------", "----------"));

                for (VentaReporte venta : listaVentas) {
                    contenido.append(String.format("%-8d %-25s %-40s %-10d %-50s $%-9.2f%n",
                            venta.getId(),
                            venta.getVendedor(),
                            venta.getProductos(),
                            venta.getCantidad(),
                            venta.getDescripcion(),
                            venta.getTotal()));
                }

                double totalGeneral = listaVentas.stream().mapToDouble(VentaReporte::getTotal).sum();
                contenido.append("\nTOTAL GENERAL: $").append(String.format("%.2f", totalGeneral));

                Files.write(file.toPath(), contenido.toString().getBytes(StandardCharsets.UTF_8));
                mostrarAlerta("Éxito", "Reporte de ventas generado exitosamente en: " + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
            }
        } catch (IOException e) {
            mostrarAlerta("Error", "Error al generar reporte de ventas: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void imprimirReporteProductos() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Reporte de Productos");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de Texto", "*.txt"));
            fileChooser.setInitialFileName("Reporte_Productos_" + LocalDate.now() + ".txt");
            File file = fileChooser.showSaveDialog(tablaProductos.getScene().getWindow());

            if (file != null) {
                StringBuilder contenido = new StringBuilder();
                contenido.append("REPORTE DE PRODUCTOS\n");
                contenido.append("===================\n\n");
                contenido.append("Período: ").append(FechaInicioProductos.getValue()).append(" al ").append(FechaFinProductos.getValue()).append("\n\n");

                if (!descripcionesproductos.getText().isEmpty()) {
                    contenido.append("Observaciones: ").append(descripcionesproductos.getText()).append("\n\n");
                }

                contenido.append(String.format("%-8s %-10s %-30s %-50s %-12s %-12s %-8s %-20s%n",
                        "ID", "Código", "Nombre", "Descripción", "P. Compra", "P. Venta", "Cantidad", "Categoría"));
                contenido.append(String.format("%-8s %-10s %-30s %-50s %-12s %-12s %-8s %-20s%n",
                        "--------", "----------", "------------------------------", "--------------------------------------------------",
                        "------------", "------------", "--------", "--------------------"));

                for (ProductoObjeto producto : listaProductos) {
                    contenido.append(String.format("%-8d %-10d %-30s %-50s $%-11.2f $%-11.2f %-8d %-20s%n",
                            producto.getId(),
                            producto.getCodigo(),
                            producto.getNombre(),
                            producto.getDescripcion(),
                            producto.getPrecioCompra(),
                            producto.getPrecioVenta(),
                            producto.getCantidad(),
                            producto.getCategoria()));
                }

                int totalProductos = listaProductos.stream().mapToInt(ProductoObjeto::getCantidad).sum();
                contenido.append("\nTOTAL DE PRODUCTOS: ").append(totalProductos);

                Files.write(file.toPath(), contenido.toString().getBytes(StandardCharsets.UTF_8));
                mostrarAlerta("Éxito", "Reporte de productos generado exitosamente en: " + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
            }
        } catch (IOException e) {
            mostrarAlerta("Error", "Error al generar reporte de productos: " + e.getMessage(), Alert.AlertType.ERROR);
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

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
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

    public static class VentaReporte {
        private final int id;
        private final String vendedor;
        private final String productos;
        private final int cantidad;
        private final String descripcion;
        private final double total;

        public VentaReporte(int id, String vendedor, String productos, int cantidad, String descripcion, double total) {
            this.id = id;
            this.vendedor = vendedor;
            this.productos = productos;
            this.cantidad = cantidad;
            this.descripcion = descripcion;
            this.total = total;
        }

        public int getId() { return id; }
        public String getVendedor() { return vendedor; }
        public String getProductos() { return productos; }
        public int getCantidad() { return cantidad; }
        public String getDescripcion() { return descripcion; }
        public double getTotal() { return total; }
    }
}