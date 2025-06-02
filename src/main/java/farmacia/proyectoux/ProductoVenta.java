package farmacia.proyectoux;

public class ProductoVenta {

    private int id;
    private String nombre;
    private String descripcion;
    private int cantidadVendida;
    private double precio;
    private double total;

    public ProductoVenta(int id, String nombre, String descripcion, int cantidadVendida, double precio, double total) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.cantidadVendida = cantidadVendida;
        this.precio = precio;
        this.total = total;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() { return id; }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() { return nombre; }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() { return descripcion; }

    public void setCantidadVendida(int cantidadVendida) {
        this.cantidadVendida = cantidadVendida;
    }

    public int getCantidadVendida() { return cantidadVendida; }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public double getPrecio() { return precio; }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getTotal() { return total; }



}

