package farmacia.proyectoux;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;

public class ProductosEmpleado {
    @FXML
    private TableView<ProductoObjeto> tablaProductos;
    @FXML private TableColumn<ProductoObjeto, Integer> idPE;
    @FXML private TableColumn<ProductoObjeto, String> codigoPE;
    @FXML private TableColumn<ProductoObjeto, String> NomPE;
    @FXML private TableColumn<ProductoObjeto, String> DescPE;
    @FXML private TableColumn<ProductoObjeto, Double> PrecioVPE;
    @FXML private TableColumn<ProductoObjeto, Integer> cantPE;
    @FXML private TableColumn<ProductoObjeto, String> ubicacionPE;
    @FXML private TextField buscarProductoE;
    @FXML private Button VerPE;

    private ObservableList<ProductoObjeto> listaProductos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarProductos();
        configurarBusqueda();
        configurarBotonVerInformacion();
    }

    private void configurarColumnas() {
        idPE.setCellValueFactory(new PropertyValueFactory<>("id"));
        codigoPE.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        NomPE.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        DescPE.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        PrecioVPE.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        cantPE.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        ubicacionPE.setCellValueFactory(new PropertyValueFactory<>("ubicacion"));
    }

    private void cargarProductos() {
        listaProductos.clear();

        String sql = "SELECT p.id, p.codigo, p.nombre, p.descripcion, p.precioVenta, p.cantidad, c.nombre AS categoria, p.ubicacion " +
                "FROM producto p " +
                "JOIN categoria c ON p.categoria = c.id " +
                "WHERE p.estado = true";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ux", "Billie", "1234");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ProductoObjeto p = new ProductoObjeto(
                        rs.getInt("id"),
                        rs.getInt("codigo"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getDouble("precioVenta"),
                        rs.getInt("cantidad"),
                        rs.getString("categoria"),
                        rs.getString("ubicacion")
                );
                listaProductos.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        tablaProductos.setItems(listaProductos);
    }

    private void configurarBusqueda() {
        buscarProductoE.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                tablaProductos.setItems(listaProductos);
                return;
            }
            String filtro = newValue.toLowerCase();

            ObservableList<ProductoObjeto> productosFiltrados = listaProductos.filtered(p ->
                    String.valueOf(p.getId()).contains(filtro) ||
                            String.valueOf(p.getCodigo()).contains(filtro) ||
                            p.getNombre().toLowerCase().contains(filtro)
            );

            tablaProductos.setItems(productosFiltrados);
        });
    }

    private void configurarBotonVerInformacion() {
        VerPE.setOnAction(e -> {
            ProductoObjeto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
            if (seleccionado == null) {
                Alert alerta = new Alert(Alert.AlertType.WARNING);
                alerta.setTitle("Atención");
                alerta.setHeaderText(null);
                alerta.setContentText("Por favor selecciona un producto para ver su información.");
                alerta.showAndWait();
                return;
            }
            // Mostrar todos los datos del producto en una alerta
            Alert alertaInfo = new Alert(Alert.AlertType.INFORMATION);
            alertaInfo.setTitle("Información del Producto");
            alertaInfo.setHeaderText(seleccionado.getNombre());
            alertaInfo.setContentText(
                    "ID: " + seleccionado.getId() + "\n" +
                            "Código: " + seleccionado.getCodigo() + "\n" +
                            "Descripción: " + seleccionado.getDescripcion() + "\n" +
                            "Precio Venta: $" + seleccionado.getPrecioVenta() + "\n" +
                            "Cantidad: " + seleccionado.getCantidad() + "\n" +
                            "Categoria: " + seleccionado.getCategoria() + "\n" +
                            "Ubicación: " + seleccionado.getUbicacion()
            );
            alertaInfo.showAndWait();
        });
    }
}

