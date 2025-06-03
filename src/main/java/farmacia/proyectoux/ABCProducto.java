package farmacia.proyectoux;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.Optional;

public class ABCProducto {

    @FXML
    private TableView<ProductoObjeto> tablaProductos;
    @FXML
    private TableColumn<ProductoObjeto, Integer> colId;
    @FXML
    private TableColumn<ProductoObjeto, Integer> colCodigo;
    @FXML
    private TableColumn<ProductoObjeto, String> colNombre;
    @FXML
    private TableColumn<ProductoObjeto, String> colDescripcion;
    @FXML
    private TableColumn<ProductoObjeto, Double> colPrecioVenta;
    @FXML
    private TableColumn<ProductoObjeto, Integer> colCantidad;
    @FXML
    private TableColumn<ProductoObjeto, String> colUbicacion;

    @FXML
    private TextField idProducto;
    @FXML
    private TextField codigoProducto;
    @FXML
    private TextField nombreProducto;
    @FXML
    private TextArea descripcionProducto;
    @FXML
    private TextField precioCompra;
    @FXML
    private TextField precioVenta;
    @FXML
    private TextField cantidadProducto;
    @FXML
    private ComboBox<String> categoriaProducto;
    @FXML
    private TextField ubicacionProducto;
    @FXML
    private ComboBox<String> estadoProducto;
    @FXML
    private TextField BuscarProducto;

    @FXML
    private Button btnAgregar;
    @FXML
    private Button btnEditar;
    @FXML
    private Button btnEliminar;
    @FXML
    private Button btnLimpiar;
    @FXML
    private Button btnBuscar;
    @FXML
    private Button btnAtras;

    private ObservableList<ProductoObjeto> listaProductos = FXCollections.observableArrayList();
    private ObservableList<String> listaCategorias = FXCollections.observableArrayList();
    private Connection conectarBD() {
        try {
            return conexionBD.getConexion();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al conectar con la base de datos");
            return null;
        }
    }

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarCategorias();
        cargarEstados();
        mostrarProductos();

        // Configurar selección de tabla para cargar datos en los campos
        tablaProductos.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> mostrarDetallesProducto(newValue));
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colPrecioVenta.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colUbicacion.setCellValueFactory(new PropertyValueFactory<>("ubicacion"));
    }

    private void cargarCategorias() {
        listaCategorias.clear();
        String consulta = "SELECT id, nombre FROM categoria";

        try (Connection con = conectarBD();
             PreparedStatement ps = con.prepareStatement(consulta);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                listaCategorias.add(rs.getString("nombre"));
            }
            categoriaProducto.setItems(listaCategorias);
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al cargar categorías");
        }
    }

    private void cargarEstados() {
        ObservableList<String> estados = FXCollections.observableArrayList("Activo", "Inactivo");
        estadoProducto.setItems(estados);
        estadoProducto.getSelectionModel().selectFirst();
    }

    public void mostrarProductos() {
        listaProductos.clear();
        String consulta = "SELECT p.*, c.nombre as nombre_categoria FROM producto p " +
                "LEFT JOIN categoria c ON p.categoria = c.id";

        try (Connection con = conectarBD();
             PreparedStatement ps = con.prepareStatement(consulta);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ProductoObjeto producto = new ProductoObjeto(
                        rs.getInt("id"),
                        rs.getInt("codigo"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getDouble("precioCompra"),
                        rs.getDouble("precioVenta"),
                        rs.getInt("cantidad"),
                        rs.getString("nombre_categoria"),
                        rs.getString("ubicacion"),
                        rs.getBoolean("estado")
                );
                listaProductos.add(producto);
            }

            tablaProductos.setItems(listaProductos);

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al obtener productos de la base de datos.");
        }
    }

    private void mostrarDetallesProducto(ProductoObjeto producto) {
        if (producto != null) {
            idProducto.setText(String.valueOf(producto.getId()));
            codigoProducto.setText(String.valueOf(producto.getCodigo()));
            nombreProducto.setText(producto.getNombre());
            descripcionProducto.setText(producto.getDescripcion());
            precioCompra.setText(String.valueOf(producto.getPrecioCompra()));
            precioVenta.setText(String.valueOf(producto.getPrecioVenta()));
            cantidadProducto.setText(String.valueOf(producto.getCantidad()));
            categoriaProducto.getSelectionModel().select(producto.getCategoria());
            ubicacionProducto.setText(producto.getUbicacion());
            estadoProducto.getSelectionModel().select(producto.isEstado() ? "Activo" : "Inactivo");
        }
    }

    @FXML
    private void agregarProducto() {
        if (validarCampos()) {
            String consulta = "INSERT INTO producto (codigo, nombre, descripcion, precioCompra, precioVenta, " +
                    "cantidad, categoria, ubicacion, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection con = conectarBD();
                 PreparedStatement ps = con.prepareStatement(consulta, Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, Integer.parseInt(codigoProducto.getText()));
                ps.setString(2, nombreProducto.getText());
                ps.setString(3, descripcionProducto.getText());
                ps.setDouble(4, Double.parseDouble(precioCompra.getText()));
                ps.setDouble(5, Double.parseDouble(precioVenta.getText()));
                ps.setInt(6, Integer.parseInt(cantidadProducto.getText()));

                // Obtener ID de la categoría seleccionada
                int idCategoria = obtenerIdCategoria(categoriaProducto.getValue());
                ps.setInt(7, idCategoria);

                ps.setString(8, ubicacionProducto.getText());
                ps.setBoolean(9, estadoProducto.getValue().equals("Activo"));

                int filasAfectadas = ps.executeUpdate();

                if (filasAfectadas > 0) {
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        idProducto.setText(String.valueOf(rs.getInt(1)));
                    }
                    mostrarAlerta("Producto agregado correctamente", Alert.AlertType.INFORMATION);
                    mostrarProductos();
                    limpiarCampos();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                mostrarAlerta("Error al agregar producto: " + e.getMessage());
            }
        }
    }

    @FXML
    private void editarProducto() {
        if (validarCampos() && !idProducto.getText().isEmpty()) {
            Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
            alerta.setTitle("Confirmar edición");
            alerta.setHeaderText(null);
            alerta.setContentText("¿Estás seguro de que deseas editar este producto?");

            Optional<ButtonType> resultado = alerta.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                String consulta = "UPDATE producto SET codigo=?, nombre=?, descripcion=?, precioCompra=?, " +
                        "precioVenta=?, cantidad=?, categoria=?, ubicacion=?, estado=? WHERE id=?";

                try (Connection con = conectarBD();
                     PreparedStatement ps = con.prepareStatement(consulta)) {

                    ps.setInt(1, Integer.parseInt(codigoProducto.getText()));
                    ps.setString(2, nombreProducto.getText());
                    ps.setString(3, descripcionProducto.getText());
                    ps.setDouble(4, Double.parseDouble(precioCompra.getText()));
                    ps.setDouble(5, Double.parseDouble(precioVenta.getText()));
                    ps.setInt(6, Integer.parseInt(cantidadProducto.getText()));

                    // Obtener ID de la categoría seleccionada
                    int idCategoria = obtenerIdCategoria(categoriaProducto.getValue());
                    ps.setInt(7, idCategoria);

                    ps.setString(8, ubicacionProducto.getText());
                    ps.setBoolean(9, estadoProducto.getValue().equals("Activo"));
                    ps.setInt(10, Integer.parseInt(idProducto.getText()));

                    int filasAfectadas = ps.executeUpdate();

                    if (filasAfectadas > 0) {
                        mostrarAlerta("Producto actualizado correctamente", Alert.AlertType.INFORMATION);
                        mostrarProductos();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    mostrarAlerta("Error al editar producto: " + e.getMessage());
                }
            }
        } else {
            mostrarAlerta("Seleccione un producto para editar o complete todos los campos");
        }
    }

    @FXML
    private void eliminarProducto() {
        if (!idProducto.getText().isEmpty()) {
            Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
            alerta.setTitle("Confirmar eliminación");
            alerta.setHeaderText(null);
            alerta.setContentText("¿Estás seguro de que deseas eliminar este producto?");

            Optional<ButtonType> resultado = alerta.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                String consulta = "DELETE FROM producto WHERE id=?";

                try (Connection con = conectarBD();
                     PreparedStatement ps = con.prepareStatement(consulta)) {

                    ps.setInt(1, Integer.parseInt(idProducto.getText()));

                    int filasAfectadas = ps.executeUpdate();

                    if (filasAfectadas > 0) {
                        mostrarAlerta("Producto eliminado correctamente", Alert.AlertType.INFORMATION);
                        mostrarProductos();
                        limpiarCampos();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    mostrarAlerta("Error al eliminar producto: " + e.getMessage());
                }
            }
        } else {
            mostrarAlerta("Seleccione un producto para eliminar");
        }
    }

    @FXML
    private void buscarProducto() {
        String busqueda = BuscarProducto.getText().trim();
        if (!busqueda.isEmpty()) {
            String consulta = "SELECT p.*, c.nombre as nombre_categoria FROM producto p " +
                    "LEFT JOIN categoria c ON p.categoria = c.id " +
                    "WHERE p.nombre LIKE ? OR p.codigo LIKE ? OR p.descripcion LIKE ?";

            try (Connection con = conectarBD();
                 PreparedStatement ps = con.prepareStatement(consulta)) {

                ps.setString(1, "%" + busqueda + "%");
                ps.setString(2, "%" + busqueda + "%");
                ps.setString(3, "%" + busqueda + "%");

                ResultSet rs = ps.executeQuery();
                listaProductos.clear();

                while (rs.next()) {
                    ProductoObjeto producto = new ProductoObjeto(
                            rs.getInt("id"),
                            rs.getInt("codigo"),
                            rs.getString("nombre"),
                            rs.getString("descripcion"),
                            rs.getDouble("precioCompra"),
                            rs.getDouble("precioVenta"),
                            rs.getInt("cantidad"),
                            rs.getString("nombre_categoria"),
                            rs.getString("ubicacion"),
                            rs.getBoolean("estado")
                    );
                    listaProductos.add(producto);
                }

                tablaProductos.setItems(listaProductos);

                if (listaProductos.isEmpty()) {
                    mostrarAlerta("No se encontraron productos con ese criterio de búsqueda", Alert.AlertType.INFORMATION);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                mostrarAlerta("Error al buscar productos");
            }
        } else {
            mostrarProductos();
        }
    }

    @FXML
    private void limpiarCampos() {
        idProducto.clear();
        codigoProducto.clear();
        nombreProducto.clear();
        descripcionProducto.clear();
        precioCompra.clear();
        precioVenta.clear();
        cantidadProducto.clear();
        categoriaProducto.getSelectionModel().clearSelection();
        ubicacionProducto.clear();
        estadoProducto.getSelectionModel().selectFirst();
        BuscarProducto.clear();
        tablaProductos.getSelectionModel().clearSelection();
    }

    private boolean validarCampos() {
        if (codigoProducto.getText().isEmpty() || nombreProducto.getText().isEmpty() ||
                descripcionProducto.getText().isEmpty() || precioCompra.getText().isEmpty() ||
                precioVenta.getText().isEmpty() || cantidadProducto.getText().isEmpty() ||
                categoriaProducto.getValue() == null || ubicacionProducto.getText().isEmpty()) {

            mostrarAlerta("Todos los campos son obligatorios");
            return false;
        }

        try {
            Integer.parseInt(codigoProducto.getText());
            Double.parseDouble(precioCompra.getText());
            Double.parseDouble(precioVenta.getText());
            Integer.parseInt(cantidadProducto.getText());
        } catch (NumberFormatException e) {
            mostrarAlerta("Los campos numéricos deben contener valores válidos");
            return false;
        }

        return true;
    }

    private int obtenerIdCategoria(String nombreCategoria) throws SQLException {
        String consulta = "SELECT id FROM categoria WHERE nombre = ?";
        try (Connection con = conectarBD();
             PreparedStatement ps = con.prepareStatement(consulta)) {

            ps.setString(1, nombreCategoria);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1; // Retorna -1 si no encuentra la categoría
    }

    private void mostrarAlerta(String mensaje) {
        mostrarAlerta(mensaje, Alert.AlertType.ERROR);
    }

    private void mostrarAlerta(String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(tipo == Alert.AlertType.ERROR ? "Error" : "Información");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
    @FXML
    private void regresarInicio() {
        try {
            // Cargar el FXML de InicioAdmin
            FXMLLoader loader = new FXMLLoader(getClass().getResource("InicioAdmin.fxml"));
            Parent root = loader.load();

            // Obtener la escena actual y la ventana
            Scene scene = btnAtras.getScene();
            Stage stage = (Stage) scene.getWindow();

            // Cambiar la escena a InicioAdmin
            scene.setRoot(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error al cargar la pantalla de inicio");
        }
    }
}
