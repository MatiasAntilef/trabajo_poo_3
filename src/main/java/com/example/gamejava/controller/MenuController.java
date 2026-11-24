package com.example.gamejava.controller;

import com.example.gamejava.model.GameModel;
import com.example.gamejava.repository.GameRepository;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.File;

public class MenuController {

    // Controles del Navbar de Filtros
    @FXML private TextField txtBuscarNombre;
    @FXML private TextField txtBuscarEmpresa;
    @FXML private ComboBox<String> cmbFiltroGenero;
    @FXML private ComboBox<String> cmbFiltroPlataforma;
    @FXML private TextField txtPrecioMax;

    private final ObservableList<GameModel> listaMaestra = FXCollections.observableArrayList();
    private FilteredList<GameModel> listaFiltrada;

    // CRUD VIDEOJUEGOS
    @FXML private TextField txtNombre;
    @FXML private TextField txtEmpresa;
    @FXML private TextField txtGenero;
    @FXML private TextField txtPrecio;
    @FXML private CheckBox chkPC;
    @FXML private CheckBox chkCelular;
    @FXML private CheckBox chkPlayStation;
    @FXML private CheckBox chkXbox;
    @FXML private CheckBox chkSwitch;

    // Tabla y columnas LISTA DE VIDEOJUEGOS
    @FXML private TextField txtImagenUrl;
    @FXML private TableView<GameModel> tablaJuegos;
    @FXML private TableColumn<GameModel, String> colPortada;
    @FXML private TableColumn<GameModel, Integer> colId;
    @FXML private TableColumn<GameModel, String> colNombre;
    @FXML private TableColumn<GameModel, String> colEmpresa;
    @FXML private TableColumn<GameModel, String> colGenero;
    @FXML private TableColumn<GameModel, Double> colPrecio;
    @FXML private TableColumn<GameModel, String> colPlataformas;

    private final GameRepository repository = new GameRepository();
    private GameModel juegoSeleccionado = null;

    @FXML
    public void initialize() {
        // 1. Enlazar propiedades básicas de las columnas
        colId.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        colNombre.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
        colEmpresa.setCellValueFactory(cellData -> cellData.getValue().empresaProperty());
        colGenero.setCellValueFactory(cellData -> cellData.getValue().generoProperty());
        colPrecio.setCellValueFactory(cellData -> cellData.getValue().precioSalidaProperty().asObject());
        colPlataformas.setCellValueFactory(cellData -> cellData.getValue().plataformasProperty());

        // 2. Renderizar imagen uniforme (mismo tamaño exacto, centrada y recortada sin estirarse)
        colPortada.setCellValueFactory(cellData -> cellData.getValue().imagenUrlProperty());
        colPortada.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setPreserveRatio(false);
                imageView.fitWidthProperty().bind(colPortada.widthProperty().subtract(16));
                imageView.fitHeightProperty().bind(tablaJuegos.fixedCellSizeProperty().subtract(10));
                setAlignment(Pos.CENTER);
                setStyle("-fx-border-color: transparent transparent #dcdcdc transparent; -fx-border-width: 0 0 1 0; -fx-padding: 4;");
            }

            @Override
            protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);
                if (empty || url == null || url.isBlank()) {
                    setGraphic(null);
                } else {
                    Image img = new Image(url, true);
                    img.progressProperty().addListener((obs, oldP, progress) -> {
                        if (progress.doubleValue() >= 1.0 && img.getWidth() > 0 && img.getHeight() > 0) {
                            aplicarRecorteCentrado(imageView, img);
                        }
                    });
                    if (img.getProgress() >= 1.0 && img.getWidth() > 0) {
                        aplicarRecorteCentrado(imageView, img);
                    }
                    imageView.setImage(img);
                    setGraphic(imageView);
                }
            }
        });

        // 3. Mejorar tipografía, centrado vertical y línea separadora en las columnas de texto
        estilizarColumnaTexto(colId, "-fx-font-size: 14px; -fx-text-fill: #666666;", Pos.CENTER);
        estilizarColumnaTexto(colNombre, "-fx-font-size: 16px; -fx-font-weight: bold;", Pos.CENTER_LEFT);
        estilizarColumnaTexto(colEmpresa, "-fx-font-size: 14px;", Pos.CENTER_LEFT);
        estilizarColumnaTexto(colGenero, "-fx-font-size: 14px;", Pos.CENTER_LEFT);
        estilizarColumnaTexto(colPlataformas, "-fx-font-size: 14px;", Pos.CENTER_LEFT);

        colPrecio.setCellFactory(col -> new TableCell<>() {
            {
                setAlignment(Pos.CENTER);
                setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e7e34; -fx-border-color: transparent transparent #dcdcdc transparent; -fx-border-width: 0 0 1 0;");
            }

            @Override
            protected void updateItem(Double precio, boolean empty) {
                super.updateItem(precio, empty);
                setText(empty || precio == null ? null : String.format("$%.2f", precio));
            }
        });

        // 4. Calcular altura de fila dinámica para que quepan exactamente 7 juegos antes de scrollear
        tablaJuegos.fixedCellSizeProperty().bind(
                tablaJuegos.heightProperty().subtract(32).divide(7)
        );

        // 5. Cargar datos desde MySQL
        cargarDatos();

        // 6. Configurar opciones del Navbar de Filtros
        cmbFiltroGenero.getItems().addAll(
                "Todos", "Acción", "Aventura", "RPG", "Estrategia",
                "Deportes", "Simulación", "Terror", "Metroidvania", "Indie"
        );
        cmbFiltroGenero.setValue("Todos");

        cmbFiltroPlataforma.getItems().addAll(
                "Todas", "PC", "Celular", "PlayStation", "Xbox", "Nintendo Switch"
        );
        cmbFiltroPlataforma.setValue("Todas");

        // 7. Envolver listaMaestra en FilteredList + SortedList para la tabla
        listaFiltrada = new FilteredList<>(listaMaestra, p -> true);
        SortedList<GameModel> listaOrdenada = new SortedList<>(listaFiltrada);
        listaOrdenada.comparatorProperty().bind(tablaJuegos.comparatorProperty());
        tablaJuegos.setItems(listaOrdenada);

        // 8. Listeners en tiempo real para los 5 filtros
        txtBuscarNombre.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        txtBuscarEmpresa.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        cmbFiltroGenero.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        cmbFiltroPlataforma.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        txtPrecioMax.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());

        // 9. Listener al hacer clic en una fila de la tabla
        tablaJuegos.getSelectionModel().selectedItemProperty().addListener((obs, anterior, actual) -> {
            if (actual != null) {
                seleccionarJuegoEnFormulario(actual);
            }
        });
    }

    private void aplicarRecorteCentrado(ImageView iv, Image img) {
        double targetW = iv.getFitWidth();
        double targetH = iv.getFitHeight();
        if (targetW <= 0 || targetH <= 0) return;

        double targetRatio = targetW / targetH;
        double imgW = img.getWidth();
        double imgH = img.getHeight();
        double imgRatio = imgW / imgH;

        if (imgRatio > targetRatio) {
            double newW = imgH * targetRatio;
            iv.setViewport(new Rectangle2D((imgW - newW) / 2.0, 0, newW, imgH));
        } else {
            double newH = imgW / targetRatio;
            iv.setViewport(new Rectangle2D(0, (imgH - newH) / 2.0, imgW, newH));
        }
    }

    private <T> void estilizarColumnaTexto(TableColumn<GameModel, T> columna, String estiloTexto, Pos alineacion) {
        columna.setCellFactory(col -> new TableCell<>() {
            {
                setAlignment(alineacion);
                setWrapText(true);
                setStyle(estiloTexto + " -fx-border-color: transparent transparent #dcdcdc transparent; -fx-border-width: 0 0 1 0; -fx-padding: 0 8 0 8;");
            }

            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
            }
        });
    }


    private void seleccionarJuegoEnFormulario(GameModel juego) {
        juegoSeleccionado = juego;
        txtNombre.setText(juego.getNombre());
        txtEmpresa.setText(juego.getEmpresa());
        txtGenero.setText(juego.getGenero());
        txtPrecio.setText(String.valueOf(juego.getPrecioSalida()));
        txtImagenUrl.setText(juego.getImagenUrl());
        marcarPlataformas(juego.getPlataformas());
    }



    @FXML
    private void onSeleccionarImagenClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar portada del videojuego");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.webp")
        );
        File archivo = fileChooser.showOpenDialog(txtNombre.getScene().getWindow());
        if (archivo != null) {
            txtImagenUrl.setText(archivo.toURI().toString());
        }
    }

    private void cargarDatos() {
        listaMaestra.setAll(repository.obtenerTodos());
    }

    private void aplicarFiltros() {
        String filtroNombre = txtBuscarNombre.getText() != null ? txtBuscarNombre.getText().toLowerCase().trim() : "";
        String filtroEmpresa = txtBuscarEmpresa.getText() != null ? txtBuscarEmpresa.getText().toLowerCase().trim() : "";
        String filtroGenero = cmbFiltroGenero.getValue();
        String filtroPlataforma = cmbFiltroPlataforma.getValue();
        String textoPrecio = txtPrecioMax.getText() != null ? txtPrecioMax.getText().trim() : "";

        listaFiltrada.setPredicate(juego -> {
            // 1. Nombre
            if (!filtroNombre.isEmpty() && !juego.getNombre().toLowerCase().contains(filtroNombre)) {
                return false;
            }
            // 2. Empresa
            if (!filtroEmpresa.isEmpty() && !juego.getEmpresa().toLowerCase().contains(filtroEmpresa)) {
                return false;
            }
            // 3. Género
            if (filtroGenero != null && !filtroGenero.equals("Todos")) {
                if (juego.getGenero() == null || !juego.getGenero().toLowerCase().contains(filtroGenero.toLowerCase())) {
                    return false;
                }
            }
            // 4. Plataforma
            if (filtroPlataforma != null && !filtroPlataforma.equals("Todas")) {
                if (juego.getPlataformas() == null || !juego.getPlataformas().toLowerCase().contains(filtroPlataforma.toLowerCase())) {
                    return false;
                }
            }
            // 5. Precio máximo
            if (!textoPrecio.isEmpty()) {
                try {
                    double max = Double.parseDouble(textoPrecio);
                    if (juego.getPrecioSalida() > max) {
                        return false;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            return true;
        });
    }

    @FXML
    private void onLimpiarFiltrosClick() {
        txtBuscarNombre.clear();
        txtBuscarEmpresa.clear();
        cmbFiltroGenero.setValue("Todos");
        cmbFiltroPlataforma.setValue("Todas");
        txtPrecioMax.clear();
    }

    private String obtenerPlataformasSeleccionadas() {
        List<String> seleccionadas = new ArrayList<>();
        if (chkPC.isSelected()) seleccionadas.add("PC");
        if (chkCelular.isSelected()) seleccionadas.add("Celular");
        if (chkPlayStation.isSelected()) seleccionadas.add("PlayStation");
        if (chkXbox.isSelected()) seleccionadas.add("Xbox");
        if (chkSwitch.isSelected()) seleccionadas.add("Nintendo Switch");
        return String.join(", ", seleccionadas);
    }

    private void marcarPlataformas(String plataformasTexto) {
        String texto = plataformasTexto != null ? plataformasTexto.toLowerCase() : "";
        chkPC.setSelected(texto.contains("pc"));
        chkCelular.setSelected(texto.contains("celular"));
        chkPlayStation.setSelected(texto.contains("playstation") || texto.contains("ps4") || texto.contains("ps5"));
        chkXbox.setSelected(texto.contains("xbox"));
        chkSwitch.setSelected(texto.contains("switch"));
    }

    @FXML
    private void onGuardarClick() {
        if (!validarCampos()) return;
        if (repository.existeNombre(txtNombre.getText().trim(), 0)) {
            mostrarAlerta(Alert.AlertType.WARNING, "Nombre duplicado", "Ya existe un videojuego registrado con ese nombre.");
            return;
        }

        GameModel nuevo = new GameModel(
                txtNombre.getText().trim(),
                txtEmpresa.getText().trim(),
                txtGenero.getText().trim(),
                Double.parseDouble(txtPrecio.getText().trim()),
                obtenerPlataformasSeleccionadas(),
                txtImagenUrl.getText().trim()
        );

        if (repository.insertar(nuevo)) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Videojuego agregado correctamente.");
            limpiarFormulario();
            cargarDatos();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo guardar en la base de datos.");
        }
    }

    @FXML
    private void onActualizarClick() {
        if (juegoSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atención", "Haz clic en una carta para editar.");
            return;
        }
        if (!validarCampos()) return;
        if (repository.existeNombre(txtNombre.getText().trim(), juegoSeleccionado.getId())) {
            mostrarAlerta(Alert.AlertType.WARNING, "Nombre duplicado", "Otro videojuego ya está registrado con ese nombre.");
            return;
        }

        juegoSeleccionado.setNombre(txtNombre.getText().trim());
        juegoSeleccionado.setEmpresa(txtEmpresa.getText().trim());
        juegoSeleccionado.setGenero(txtGenero.getText().trim());
        juegoSeleccionado.setPrecioSalida(Double.parseDouble(txtPrecio.getText().trim()));
        juegoSeleccionado.setPlataformas(obtenerPlataformasSeleccionadas());
        juegoSeleccionado.setImagenUrl(txtImagenUrl.getText().trim());

        if (repository.actualizar(juegoSeleccionado)) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Actualizado", "Videojuego actualizado correctamente.");
            limpiarFormulario();
            cargarDatos();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo actualizar el registro.");
        }
    }

    @FXML
    private void onEliminarClick() {
        if (juegoSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atención", "Selecciona un videojuego de la tabla para eliminar.");
            return;
        }

        if (repository.eliminar(juegoSeleccionado.getId())) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Eliminado", "Videojuego eliminado de la tienda.");
            limpiarFormulario();
            cargarDatos();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar el registro.");
        }
    }

    @FXML
    private void onLimpiarClick() {
        limpiarFormulario();
    }

    private void limpiarFormulario() {
        juegoSeleccionado = null;
        txtNombre.clear();
        txtEmpresa.clear();
        txtGenero.clear();
        txtPrecio.clear();
        chkPC.setSelected(false);
        chkCelular.setSelected(false);
        chkPlayStation.setSelected(false);
        chkXbox.setSelected(false);
        chkSwitch.setSelected(false);
        txtImagenUrl.clear();
        tablaJuegos.getSelectionModel().clearSelection();
    }

    private boolean validarCampos() {
        if (txtNombre.getText().isBlank() || txtEmpresa.getText().isBlank() ||
                txtGenero.getText().isBlank() || txtPrecio.getText().isBlank() ) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos vacíos", "Por favor completa todos los campos.");
            return false;
        }
        if (obtenerPlataformasSeleccionadas().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin plataformas", "Selecciona al menos una plataforma.");
            return false;
        }

        try {
            double precio = Double.parseDouble(txtPrecio.getText().trim());
            if (precio < 0) {
                mostrarAlerta(Alert.AlertType.WARNING, "Precio inválido", "El precio no puede ser negativo.");
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Formato incorrecto", "El precio debe ser un número válido (ej. 59.99).");
            return false;
        }

        return true;
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}