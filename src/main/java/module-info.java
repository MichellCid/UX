module farmacia.proyectoux {
    requires javafx.controls;
    requires javafx.fxml;


    opens farmacia.proyectoux to javafx.fxml;
    exports farmacia.proyectoux;
}