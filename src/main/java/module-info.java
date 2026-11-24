module com.example.gamejava {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens com.example.gamejava to javafx.fxml;
    exports com.example.gamejava;

    exports com.example.gamejava.controller;
    opens com.example.gamejava.controller to javafx.fxml;
    opens com.example.gamejava.model to javafx.base;
}