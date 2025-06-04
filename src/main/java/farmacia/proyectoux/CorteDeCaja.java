package farmacia.proyectoux;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class CorteDeCaja {

    @FXML private Label usuarioCorte, inicioCorte, finCorte, fechaCorte;
    @FXML private Label fondoCaja, entradasCorte, salidasCorte, totalEsperado, diferenciaCaja;
    @FXML private TextField totalCorte;
    @FXML private Button cerrarCorte;

    private int idEmpleado;
    private String nombreEmpleado;
    private LocalDateTime inicioTurno;
    private final double fondo = 100.0;

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/ux", "Billie", "1234");
    }

    public void setIdEmpleadoActual(int id) {
        this.idEmpleado = id;
        this.inicioTurno = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        try (Connection conn = conectar();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT nombre, apellidos FROM empleados WHERE id = ?")) {

            stmt.setInt(1, idEmpleado);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String nombre = rs.getString("nombre");
                String apellidos = rs.getString("apellidos");
                nombreEmpleado = nombre + " " + apellidos;
            } else {
                nombreEmpleado = "Desconocido";
            }

        } catch (SQLException e) {
            nombreEmpleado = "Error";
            e.printStackTrace();
        }

        DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        DateTimeFormatter formatterFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        usuarioCorte.setText(nombreEmpleado);
        inicioCorte.setText(inicioTurno.format(formatterHora));
        fechaCorte.setText(LocalDate.now().format(formatterFecha));
        fondoCaja.setText(String.format("$%.2f", fondo));

        actualizarEntradasYTotalEsperado();
    }

    private void actualizarEntradasYTotalEsperado() {
        try (Connection conn = conectar()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT SUM(total) FROM ventas WHERE id_empleado = ? AND fecha >= ?"
            );
            ps.setInt(1, idEmpleado);
            ps.setTimestamp(2, Timestamp.valueOf(inicioTurno));
            ResultSet rs = ps.executeQuery();

            double entradas = 0;
            if (rs.next()) {
                entradas = rs.getDouble(1);
            }

            double totalEsperadoVal = fondo + entradas;

            entradasCorte.setText(String.format("$%.2f", entradas));
            totalEsperado.setText(String.format("$%.2f", totalEsperadoVal));

        } catch (SQLException e) {
            entradasCorte.setText("Error");
            totalEsperado.setText("Error");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCerrarCorte() {
        DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime finTurno = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        finCorte.setText(finTurno.format(formatterHora));

        try (Connection conn = conectar()) {
            // Consulta entradas desde inicioTurno hasta finTurno
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT SUM(total) FROM ventas WHERE id_empleado = ? AND fecha >= ? AND fecha <= ?"
            );
            ps.setInt(1, idEmpleado);
            ps.setTimestamp(2, Timestamp.valueOf(inicioTurno));
            ps.setTimestamp(3, Timestamp.valueOf(finTurno));
            ResultSet rs = ps.executeQuery();

            double entradas = 0;
            if (rs.next()) {
                entradas = rs.getDouble(1);
            }

            double totalEsperadoVal = fondo + entradas;
            double totalEnCaja = parseDoubleSeguro(totalCorte.getText());
            double diferencia = totalEnCaja - totalEsperadoVal;

            entradasCorte.setText(String.format("$%.2f", entradas));
            salidasCorte.setText("$0.00");
            totalEsperado.setText(String.format("$%.2f", totalEsperadoVal));
            diferenciaCaja.setText(String.format("$%.2f", diferencia));

            PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO corte_caja (id_empleado, fecha, inicio_turno, fin_turno, fondo_caja, entradas, salidas, total_esperado, total_en_caja, diferencia) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );
            insert.setInt(1, idEmpleado);
            insert.setDate(2, Date.valueOf(LocalDate.now()));
            insert.setTimestamp(3, Timestamp.valueOf(inicioTurno));
            insert.setTimestamp(4, Timestamp.valueOf(finTurno));
            insert.setDouble(5, fondo);
            insert.setDouble(6, entradas);
            insert.setDouble(7, 0.0); // salidas
            insert.setDouble(8, totalEsperadoVal);
            insert.setDouble(9, totalEnCaja);
            insert.setDouble(10, diferencia);
            insert.executeUpdate();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Corte registrado");
            alert.setHeaderText(null);
            alert.setContentText("El corte de caja se ha registrado correctamente.");
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Error");
            error.setHeaderText("No se pudo cerrar el corte");
            error.setContentText(e.getMessage());
            error.showAndWait();
        }
    }

    private double parseDoubleSeguro(String texto) {
        if (texto == null || texto.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(texto.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
