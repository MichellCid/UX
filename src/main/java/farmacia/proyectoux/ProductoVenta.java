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


    // Getters y setters para JavaFX (si usas Property puedes hacerlo también)
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public int getCantidadVendida() { return cantidadVendida; }
    public double getPrecio() { return precio; }
    public double getTotal() { return total; }
}

