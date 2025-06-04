package farmacia.proyectoux;

import javafx.beans.property.*;

public class VentaObjeto {
    private IntegerProperty id;
    private StringProperty fecha;
    private StringProperty productos;
    private IntegerProperty totalProductos;
    private DoubleProperty totalVenta;

    public VentaObjeto(int id, String fecha, String productos, int totalProductos, double totalVenta) {
        this.id = new SimpleIntegerProperty(id);
        this.fecha = new SimpleStringProperty(fecha);
        this.productos = new SimpleStringProperty(productos);
        this.totalProductos = new SimpleIntegerProperty(totalProductos);
        this.totalVenta = new SimpleDoubleProperty(totalVenta);
    }

    public int getId() { return id.get(); }
    public String getFecha() { return fecha.get(); }
    public String getProductos() { return productos.get(); }
    public int getTotalProductos() { return totalProductos.get(); }
    public double getTotalVenta() { return totalVenta.get(); }

    public IntegerProperty idProperty() { return id; }
    public StringProperty fechaProperty() { return fecha; }
    public StringProperty productosProperty() { return productos; }
    public IntegerProperty totalProductosProperty() { return totalProductos; }
    public DoubleProperty totalVentaProperty() { return totalVenta; }
}

