package com.soporte.dao;

import com.soporte.model.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de los usuarios (JDBC con consultas parametrizadas).
 * Todos los métodos lanzan SQLException: la capa que los llama decide qué mensaje mostrar.
 */
public class UsuarioDAO {

    // Devuelve el usuario si el correo y el hash de la contraseña coinciden; si no, null
    public Usuario validarLogin(String correo, String passwordHash) throws SQLException {
        String sql = "SELECT id, nombre, correo, rol FROM usuarios WHERE correo = ? AND password_hash = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, correo.trim());
            stmt.setString(2, passwordHash.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    // Lista los usuarios de un rol
    public List<Usuario> listarPorRol(String rol) throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, correo, rol FROM usuarios WHERE rol = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, rol.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    // Gestión de usuarios (administrador)
    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, correo, rol FROM usuarios ORDER BY id";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Usuario buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre, correo, rol FROM usuarios WHERE id = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    public void crear(Usuario usuario, String passwordHash) throws SQLException {
        String sql = "INSERT INTO usuarios (nombre, correo, password_hash, rol) VALUES (?, ?, ?, ?)";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getCorreo());
            stmt.setString(3, passwordHash);
            stmt.setString(4, usuario.getRol());
            stmt.executeUpdate();
        }
    }

    // Si passwordHash es null, la contraseña actual no se modifica
    public boolean actualizar(Usuario usuario, String passwordHash) throws SQLException {
        String sql = (passwordHash == null)
                ? "UPDATE usuarios SET nombre = ?, correo = ?, rol = ? WHERE id = ?"
                : "UPDATE usuarios SET nombre = ?, correo = ?, rol = ?, password_hash = ? WHERE id = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getCorreo());
            stmt.setString(3, usuario.getRol());
            if (passwordHash == null) {
                stmt.setInt(4, usuario.getId());
            } else {
                stmt.setString(4, passwordHash);
                stmt.setInt(5, usuario.getId());
            }
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // Convierte una fila de la base de datos en un objeto Usuario
    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setNombre(rs.getString("nombre"));
        u.setCorreo(rs.getString("correo"));
        u.setRol(rs.getString("rol"));
        return u;
    }
}
