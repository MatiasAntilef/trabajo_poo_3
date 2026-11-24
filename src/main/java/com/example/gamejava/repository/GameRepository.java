package com.example.gamejava.repository;

import com.example.gamejava.model.GameModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class GameRepository {

    private static final String URL = "jdbc:mysql://localhost:3306/tienda_juegos";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // VERIFICAR SI EL NOMBRE YA EXISTE (EXCLUYENDO UN ID AL EDITAR)
    public boolean existeNombre(String nombre, int idExcluir) {
        String sql = "SELECT COUNT(*) FROM videojuegos WHERE LOWER(nombre) = LOWER(?) AND id != ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombre.trim());
            pstmt.setInt(2, idExcluir);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar nombre duplicado: " + e.getMessage());
        }
        return false;
    }


    // LEER TODOS LOS VIDEOJUEGOS
    public ObservableList<GameModel> obtenerTodos() {
        ObservableList<GameModel> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM videojuegos";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new GameModel(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("empresa"),
                        rs.getString("genero"),
                        rs.getDouble("precio_salida"),
                        rs.getString("plataformas"),
                        rs.getString("imagen_url")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar videojuegos: " + e.getMessage());
        }
        return lista;
    }

    // GUARDAR NUEVO VIDEOJUEGO
    public boolean insertar(GameModel juego) {
        String sql = "INSERT INTO videojuegos (nombre, empresa, genero, precio_salida, plataformas, imagen_url) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, juego.getNombre());
            pstmt.setString(2, juego.getEmpresa());
            pstmt.setString(3, juego.getGenero());
            pstmt.setDouble(4, juego.getPrecioSalida());
            pstmt.setString(5, juego.getPlataformas());
            pstmt.setString(6, juego.getImagenUrl());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar videojuego: " + e.getMessage());
            return false;
        }
    }

    // ACTUALIZAR VIDEOJUEGO
    public boolean actualizar(GameModel juego) {
        String sql = "UPDATE videojuegos SET nombre = ?, empresa = ?, genero = ?, precio_salida = ?, plataformas = ?, imagen_url = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, juego.getNombre());
            pstmt.setString(2, juego.getEmpresa());
            pstmt.setString(3, juego.getGenero());
            pstmt.setDouble(4, juego.getPrecioSalida());
            pstmt.setString(5, juego.getPlataformas());
            pstmt.setString(6, juego.getImagenUrl());
            pstmt.setInt(7, juego.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar videojuego: " + e.getMessage());
            return false;
        }
    }

    // ELIMINAR VIDEOJUEGO
    public boolean eliminar(int id) {
        String sql = "DELETE FROM videojuegos WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar videojuego: " + e.getMessage());
            return false;
        }
    }
}