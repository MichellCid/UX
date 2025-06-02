package farmacia.proyectoux;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Ventas {

    @FXML private TextField NomPVE, CantidadPVE, ImporteCliente, busquedaAgregarVenta;
    @FXML private Label totalP, cambioV;
    @FXML private TableView<ProductoVenta> tablaProductosVenta;
    @FXML private TableColumn<ProductoVenta, Integer> idP, cantVendida;
    @FXML private TableColumn<ProductoVenta, String> nomP, descP;
    @FXML private TableColumn<ProductoVenta, Double> precioUnitario, totalPV;
    @FXML private Button buscarProductoV, guardarVenta, cancelarVenta, EliminarPV, ImprimirTicket;
    @FXML private AnchorPane busquedaPV;
    @FXML private TableView<ProductoObjeto> tablaBusqueda;
    @FXML private TableColumn<ProductoObjeto, Integer> idBusqueda, dispBusquedaVenta;
    @FXML private TableColumn<ProductoObjeto, String> nomBusquedaVenta, descBusquedaVenta;
    @FXML private TableColumn<ProductoObjeto, Double> precioBPV;

    private ObservableList<ProductoVenta> listaVenta = FXCollections.observableArrayList();
    private ObservableList<ProductoObjeto> listaBusqueda = FXCollections.observableArrayList();

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost/ux", "Billie", "1234"); // Cambia según tu config
    }

    @FXML
    public void initialize() {

        idP.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomP.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        descP.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        cantVendida.setCellValueFactory(new PropertyValueFactory<>("cantidadVendida"));
        precioUnitario.setCellValueFactory(new PropertyValueFactory<>("precio"));
        totalPV.setCellValueFactory(new PropertyValueFactory<>("total"));

        //ObservableList<ProductoVenta> lista = FXCollections.observableArrayList();
        tablaProductosVenta.setItems(listaVenta);

        idBusqueda.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomBusquedaVenta.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        descBusquedaVenta.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        dispBusquedaVenta.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        precioBPV.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));

        //totalPV.setCellValueFactory(new PropertyValueFactory<>("total"));

        tablaBusqueda.setItems(listaBusqueda);

        busquedaAgregarVenta.textProperty().addListener((obs, oldVal, newVal) -> buscarProductoEnBD(newVal));

        ImporteCliente.setOnAction(e -> calcularCambio());

        ImporteCliente.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // Perdió el foco
                calcularCambio();
            }
        });
    }

    private void buscarProductoEnBD(String valor) {
        listaBusqueda.clear();
        try (Connection conn = conectar();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM producto WHERE estado = 1 AND (nombre LIKE ? OR id LIKE ? OR codigo LIKE ?)")) {
            stmt.setString(1, "%" + valor + "%");
            stmt.setString(2, "%" + valor + "%");
            stmt.setString(3, "%" + valor + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println("Precio en ResultSet: " + rs.getDouble("precioVenta"));

                ProductoObjeto p = new ProductoObjeto(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getDouble("precioVenta"),
                        rs.getInt("cantidad")
                );
                //System.out.println("Precio del producto: " + p.getPrecioVenta());
                listaBusqueda.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void agregarProductoVenta() {
        ProductoObjeto seleccionado = tablaBusqueda.getSelectionModel().getSelectedItem();

        if (seleccionado != null) {
            try {
                System.out.println("Precio del producto: " + seleccionado.getPrecioVenta());

                int cantidad = Integer.parseInt(CantidadPVE.getText());
                if (cantidad > 0 && cantidad <= seleccionado.getCantidad()) {
                    double total = cantidad * seleccionado.getPrecioVenta();
                    double precio = seleccionado.getPrecioVenta();
                    ProductoVenta venta = new ProductoVenta(seleccionado.getId(), seleccionado.getNombre(),
                            seleccionado.getDescripcion(), cantidad, seleccionado.getPrecioVenta(), total);
                    listaVenta.add(venta);
                    actualizarTotal();
                    busquedaPV.setVisible(false);
                } else {
                    mostrarAlerta("Cantidad inválida o insuficiente.");
                }
            } catch (NumberFormatException e) {
                mostrarAlerta("Ingresa una cantidad válida.");
            }
        } else {
            mostrarAlerta("Selecciona un producto de la búsqueda.");
        }
    }

    private void actualizarTotal() {
        double total = listaVenta.stream().mapToDouble(ProductoVenta::getTotal).sum();
        totalP.setText(String.format("$%.2f", total));
        calcularCambio();
    }

    @FXML
    private void calcularCambio() {
        try {
            double total = listaVenta.stream().mapToDouble(ProductoVenta::getTotal).sum();
            double importe = Double.parseDouble(ImporteCliente.getText());
            if (importe >= total) {
                double cambio = importe - total;
                cambioV.setText(String.format("$%.2f", cambio));
            } else {
                cambioV.setText("Insuficiente");
            }
        } catch (NumberFormatException e) {
            cambioV.setText("Error");
        }
    }

    @FXML
    private void efectuarVenta() {
        double totalVenta = listaVenta.stream().mapToDouble(ProductoVenta::getTotal).sum();

        if (listaVenta.isEmpty()) {
            mostrarAlerta("Agrega productos antes de efectuar la venta.");
            return;
        }

        try {
            double importeCliente = Double.parseDouble(ImporteCliente.getText());
            double cambio = importeCliente - totalVenta;

            try (Connection conn = conectar()) {
                conn.setAutoCommit(false);

                // Insertar venta con importe_cliente y cambio
                String sqlVenta = "INSERT INTO ventas (fecha, total, importe_cliente, cambio, id_empleado) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement ventaStmt = conn.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS);
                ventaStmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now())); // Usa DATETIME
                ventaStmt.setDouble(2, totalVenta);
                ventaStmt.setDouble(3, importeCliente);
                ventaStmt.setDouble(4, cambio);
                ventaStmt.setInt(5, 1); // ID de empleado

                ventaStmt.executeUpdate();

                ResultSet rs = ventaStmt.getGeneratedKeys();
                int idVenta = 0;
                if (rs.next()) {
                    idVenta = rs.getInt(1);
                }

                // Insertar detalle y actualizar stock
                for (ProductoVenta pv : listaVenta) {
                    PreparedStatement detStmt = conn.prepareStatement("INSERT INTO detalle_venta (id_venta, id_producto, cantidad, precio_unitario, total) VALUES (?, ?, ?, ?, ?)");
                    detStmt.setInt(1, idVenta);
                    detStmt.setInt(2, pv.getId());
                    detStmt.setInt(3, pv.getCantidadVendida());
                    detStmt.setDouble(4, pv.getPrecio());
                    detStmt.setDouble(5, pv.getTotal());
                    detStmt.executeUpdate();

                    PreparedStatement stockStmt = conn.prepareStatement("UPDATE producto SET cantidad = cantidad - ? WHERE id = ?");
                    stockStmt.setInt(1, pv.getCantidadVendida());
                    stockStmt.setInt(2, pv.getId());
                    stockStmt.executeUpdate();
                }

                conn.commit();
                mostrarAlerta("¡Venta realizada exitosamente!");
                limpiarTodo();

            } catch (SQLException e) {
                e.printStackTrace();
                mostrarAlerta("Error al registrar la venta.");
            }

        } catch (NumberFormatException e) {
            mostrarAlerta("Importe del cliente no válido.");
        }
    }


    @FXML
    private void cancelarVenta() {
        limpiarTodo();
    }

    private void limpiarTodo() {
        listaVenta.clear();
        tablaProductosVenta.getItems().clear();
        totalP.setText("");
        cambioV.setText("");
        ImporteCliente.clear();
        CantidadPVE.clear();
        NomPVE.clear();
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mensaje");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    private void mostrarPanelBusqueda() {
        busquedaPV.setVisible(true);
        listaBusqueda.clear();
        try (Connection conn = conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM producto WHERE estado = 1")) {
            while (rs.next()) {

                ProductoObjeto p = new ProductoObjeto(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getDouble("precioVenta"),
                        rs.getInt("cantidad")
                );
                System.out.println("Precio del producto: " + p.getPrecioVenta());

                listaBusqueda.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void cancelarBusqueda() {
        busquedaPV.setVisible(false);
        busquedaAgregarVenta.clear();
        listaBusqueda.clear();
    }

}
