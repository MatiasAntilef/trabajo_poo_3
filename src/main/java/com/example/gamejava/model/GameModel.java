package com.example.gamejava.model;

import javafx.beans.property.*;

public class GameModel {

    private final IntegerProperty id;
    private final StringProperty nombre;
    private final StringProperty empresa;
    private final StringProperty genero;
    private final DoubleProperty precioSalida;
    private final StringProperty plataformas;
    private final StringProperty imagenUrl;

    public GameModel() {
        this(0, "", "", "", 0.0, "", "");
    }

    public GameModel(String nombre, String empresa, String genero, double precioSalida, String plataformas, String imagenUrl) {
        this(0, nombre, empresa, genero, precioSalida, plataformas, imagenUrl);
    }



    public GameModel(int id, String nombre, String empresa, String genero, double precioSalida, String plataformas, String imagenUrl) {
        this.id = new SimpleIntegerProperty(id);
        this.nombre = new SimpleStringProperty(nombre);
        this.empresa = new SimpleStringProperty(empresa);
        this.genero = new SimpleStringProperty(genero);
        this.precioSalida = new SimpleDoubleProperty(precioSalida);
        this.plataformas = new SimpleStringProperty(plataformas);
        this.imagenUrl = new SimpleStringProperty(imagenUrl != null ? imagenUrl : "");
    }

    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public String getNombre() { return nombre.get(); }
    public void setNombre(String nombre) { this.nombre.set(nombre); }
    public StringProperty nombreProperty() { return nombre; }

    public String getEmpresa() { return empresa.get(); }
    public void setEmpresa(String empresa) { this.empresa.set(empresa); }
    public StringProperty empresaProperty() { return empresa; }

    public String getGenero() { return genero.get(); }
    public void setGenero(String genero) { this.genero.set(genero); }
    public StringProperty generoProperty() { return genero; }

    public double getPrecioSalida() { return precioSalida.get(); }
    public void setPrecioSalida(double precioSalida) { this.precioSalida.set(precioSalida); }
    public DoubleProperty precioSalidaProperty() { return precioSalida; }

    public String getPlataformas() { return plataformas.get(); }
    public void setPlataformas(String plataformas) { this.plataformas.set(plataformas); }
    public StringProperty plataformasProperty() { return plataformas; }

    public String getImagenUrl() { return imagenUrl.get(); }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl.set(imagenUrl); }
    public StringProperty imagenUrlProperty() { return imagenUrl; }
}