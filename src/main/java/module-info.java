module rae.wc.wcontroller {
    requires javafx.controls;
    requires javafx.fxml;


    opens rae.wc.wcontroller to javafx.fxml;
    exports rae.wc.wcontroller;
}