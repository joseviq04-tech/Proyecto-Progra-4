package com.soporte.service;

import com.soporte.dao.UsuarioDAO;
import com.soporte.model.Usuario;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Capa de lógica de negocio para la gestión de usuarios.
 * Valida los datos y aplica las reglas; el acceso a la base de datos lo hace UsuarioDAO.
 * Los métodos crear, actualizar y eliminar devuelven null si todo salió bien,
 * o el mensaje de error que se le mostrará al administrador.
 */
public class UsuarioService {

    private static final List<String> ROLES = Arrays.asList("CLIENTE", "TECNICO", "ADMINISTRADOR");
    private static final Pattern CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int PASSWORD_MINIMA = 6;

    private static final int MYSQL_DUPLICADO = 1062;
    private static final int MYSQL_LLAVE_FORANEA = 1451;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public List<Usuario> listar() throws SQLException {
        return usuarioDAO.listarTodos();
    }

    public Usuario buscarPorId(int id) throws SQLException {
        return usuarioDAO.buscarPorId(id);
    }

    public String crear(String nombre, String correo, String password, String rol) {
        String error = validarDatos(nombre, correo, rol);
        if (error != null) {
            return error;
        }
        if (password == null || password.length() < PASSWORD_MINIMA) {
            return "La contraseña debe tener al menos " + PASSWORD_MINIMA + " caracteres.";
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre.trim());
        usuario.setCorreo(correo.trim());
        usuario.setRol(rol);

        try {
            usuarioDAO.crear(usuario, PasswordEncoder.hashPassword(password));
            return null;
        } catch (SQLException e) {
            return traducirError(e);
        }
    }

    public String actualizar(int id, String nombre, String correo, String password, String rol, int idAdminActual) {
        String error = validarDatos(nombre, correo, rol);
        if (error != null) {
            return error;
        }

        boolean cambiaPassword = password != null && !password.isEmpty();
        if (cambiaPassword && password.length() < PASSWORD_MINIMA) {
            return "La contraseña debe tener al menos " + PASSWORD_MINIMA + " caracteres.";
        }
        if (id == idAdminActual && !"ADMINISTRADOR".equals(rol)) {
            return "No puedes quitarte a ti mismo el rol de administrador.";
        }

        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombre(nombre.trim());
        usuario.setCorreo(correo.trim());
        usuario.setRol(rol);

        try {
            String hash = cambiaPassword ? PasswordEncoder.hashPassword(password) : null;
            if (!usuarioDAO.actualizar(usuario, hash)) {
                return "El usuario no existe.";
            }
            return null;
        } catch (SQLException e) {
            return traducirError(e);
        }
    }

    public String eliminar(int id, int idAdminActual) {
        if (id == idAdminActual) {
            return "No puedes eliminar tu propia cuenta.";
        }
        try {
            if (!usuarioDAO.eliminar(id)) {
                return "El usuario no existe.";
            }
            return null;
        } catch (SQLException e) {
            return traducirError(e);
        }
    }


    private String validarDatos(String nombre, String correo, String rol) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "El nombre es obligatorio.";
        }
        if (nombre.trim().length() > 100) {
            return "El nombre no puede superar los 100 caracteres.";
        }
        if (correo == null || correo.trim().isEmpty()) {
            return "El correo es obligatorio.";
        }
        if (correo.trim().length() > 100 || !CORREO.matcher(correo.trim()).matches()) {
            return "El correo no tiene un formato válido.";
        }
        if (rol == null || !ROLES.contains(rol)) {
            return "El rol seleccionado no es válido.";
        }
        return null;
    }

    private String traducirError(SQLException e) {
        if (e.getErrorCode() == MYSQL_DUPLICADO) {
            return "Ya existe un usuario con ese correo.";
        }
        if (e.getErrorCode() == MYSQL_LLAVE_FORANEA) {
            return "No se puede eliminar: el usuario tiene tickets asociados.";
        }
        e.printStackTrace();
        return "Error de base de datos. Intenta de nuevo.";
    }
}
