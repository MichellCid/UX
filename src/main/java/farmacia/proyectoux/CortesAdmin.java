package farmacia.proyectoux;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class CortesAdmin implements Initializable {

    @FXML private DatePicker fechaDesde;
    @FXML private DatePicker fechaHasta;
    @FXML private TextField campoBuscar;
    @FXML private TableView<Object[]> tablaCortes;

    @FXML private TableColumn<Object[], Integer> colId;
    @FXML private TableColumn<Object[], String> colUsuario;
    @FXML private TableColumn<Object[], String> colFecha;
    @FXML private TableColumn<Object[], String> colInicio;
    @FXML private TableColumn<Object[], String> colFin;
    @FXML private TableColumn<Object[], Double> colTotalFinal;

    @FXML private Button btnVerDetalles;  // botón para mostrar detalles

    private ObservableList<Object[]> listaCortes = FXCollections.observableArrayList();

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/ux", "Billie", "1234");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        campoBuscar.textProperty().addListener((obs, oldVal, newVal) -> filtrarCortes());
        fechaDesde.valueProperty().addListener((obs, oldVal, newVal) -> filtrarCortes());
        fechaHasta.valueProperty().addListener((obs, oldVal, newVal) -> filtrarCortes());

        colId.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>((Integer) data.getValue()[0]));
        colUsuario.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>((String) data.getValue()[1]));
        colFecha.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>((String) data.getValue()[2]));
        colInicio.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>((String) data.getValue()[3]));
        colFin.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>((String) data.getValue()[4]));
        colTotalFinal.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>((Double) data.getValue()[5]));

        cargarCortes();

        btnVerDetalles.setOnAction(e -> mostrarDetallesCorte());
    }

    private void cargarCortes() {
        listaCortes.clear();
        try (Connection conn = conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT c.id, CONCAT(e.nombre, ' ', e.apellidos) AS usuario, c.fecha, c.inicio_turno, c.fin_turno, c.total_en_caja " +
                             "FROM corte_caja c " +
                             "JOIN empleados e ON c.id_empleado = e.id")) {

            while (rs.next()) {
                Object[] fila = new Object[] {
                        rs.getInt("id"),
                        rs.getString("usuario"),
                        rs.getDate("fecha").toString(),
                        rs.getTimestamp("inicio_turno").toLocalDateTime().toString(),
                        rs.getTimestamp("fin_turno").toLocalDateTime().toString(),
                        rs.getDouble("total_en_caja")
                };
                listaCortes.add(fila);
            }

            tablaCortes.setItems(listaCortes);
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error cargando cortes: " + e.getMessage());
        }
    }

    private void filtrarCortes() {
        String filtroTexto = campoBuscar.getText() == null ? "" : campoBuscar.getText().toLowerCase();
        LocalDate desde = fechaDesde.getValue();
        LocalDate hasta = fechaHasta.getValue();

        ObservableList<Object[]> filtrados = FXCollections.observableArrayList();

        for (Object[] fila : listaCortes) {
            String usuario = ((String) fila[1]).toLowerCase();
            LocalDate fecha = LocalDate.parse((String) fila[2]);

            boolean coincideTexto = usuario.contains(filtroTexto);
            boolean coincideFecha = true;

            if (desde != null && fecha.isBefore(desde)) coincideFecha = false;
            if (hasta != null && fecha.isAfter(hasta)) coincideFecha = false;

            if (coincideTexto && coincideFecha) {
                filtrados.add(fila);
            }
        }

        tablaCortes.setItems(filtrados);
    }

    private void mostrarDetallesCorte() {
        Object[] seleccion = tablaCortes.getSelectionModel().getSelectedItem();
        if (seleccion != null) {
            int idCorte = (Integer) seleccion[0];

            String sql = "SELECT p.nombre, p.descripcion, dv.cantidad, dv.precio_unitario, dv.total " +
                    "FROM detalle_venta dv " +
                    "JOIN producto p ON dv.id_producto = p.id " +
                    "JOIN ventas v ON dv.id_venta = v.id " +
                    "JOIN corte_caja c ON c.id = ? " +
                    "WHERE v.fecha BETWEEN c.inicio_turno AND c.fin_turno";

            try (Connection conn = conectar();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, idCorte);
                ResultSet rs = stmt.executeQuery();

                StringBuilder detalles = new StringBuilder();
                detalles.append("Detalles del Corte #").append(idCorte).append("\n\n");
                detalles.append(String.format("%-30s %-10s %-10s %-10s%n",
                        "Producto", "Cantidad", "P. Unit.", "Total"));
                detalles.append("------------------------------------------------------------\n");

                boolean hayDatos = false;
                while (rs.next()) {
                    hayDatos = true;
                    detalles.append(String.format("%-30s %-10d $%-9.2f $%-9.2f%n",
                            rs.getString("nombre"),
                            rs.getInt("cantidad"),
                            rs.getDouble("precio_unitario"),
                            rs.getDouble("total")));
                }

                if (!hayDatos) {
                    detalles.append("No hay detalles para este corte.");
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Detalles del Corte");
                alert.setHeaderText(null);
                alert.setContentText(detalles.toString());
                alert.getDialogPane().setPrefWidth(600);
                alert.showAndWait();

            } catch (SQLException e) {
                e.printStackTrace();
                mostrarAlerta("Error al obtener detalles: " + e.getMessage());
            }
        } else {
            mostrarAlerta("Selecciona un corte para ver los detalles.");
        }
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
