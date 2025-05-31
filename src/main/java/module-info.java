module farmacia.proyectoux {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens farmacia.proyectoux to javafx.fxml;
    exports farmacia.proyectoux;
}