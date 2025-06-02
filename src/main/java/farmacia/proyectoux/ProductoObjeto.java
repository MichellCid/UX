package farmacia.proyectoux;

public class ProductoObjeto {
    private int id;
    private int codigo;
    private String nombre;
    private String descripcion;
    private double precioCompra;
    private double PrecioVenta;
    private int cantidad;
    private int categoria;
    private String ubicacion;
    private boolean estado;

    public ProductoObjeto(int id, int codigo, String nombre, String descripcion, double precioCompra, double precioVenta, int cantidad, int categoria, String ubicacion, boolean estado){
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioCompra = precioCompra;
        this.PrecioVenta = precioVenta;
        this.cantidad = cantidad;
        this.categoria = categoria;
        this.ubicacion = ubicacion;
        this.estado = estado;

    }

    public ProductoObjeto(int id, String nombre, String descripcion, double precioVenta, int cantidad){
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;

        this.PrecioVenta = precioVenta;
        this.cantidad = cantidad;


    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(double precioCompra) {
        this.precioCompra = precioCompra;
    }

    public double getPrecioVenta() {
        return PrecioVenta;
    }

    public void setPrecioVenta(double precioVenta) {
        PrecioVenta = precioVenta;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getCategoria() {
        return categoria;
    }

    public void setCategoria(int categoria) {
        this.categoria = categoria;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }
}

