package com.soporte.controller;

import com.soporte.dao.UsuarioDAO;
import com.soporte.model.Usuario;
import com.soporte.service.PasswordEncoder;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String correo = request.getParameter("correo");
        String password = request.getParameter("password");

        if (correo == null || password == null || correo.trim().isEmpty() || password.isEmpty()) {
            mostrarError(request, response, "Por favor, completa todos los campos.");
            return;
        }

        String passwordHash = PasswordEncoder.hashPassword(password);

        Usuario usuario;
        try {
            usuario = usuarioDAO.validarLogin(correo.trim(), passwordHash);
        } catch (SQLException e) {
            // Diferencia entre problema con la contraseña y la base de datos
            log("Error de base de datos durante el inicio de sesión", e);
            mostrarError(request, response,
                    "No se pudo iniciar sesión por un problema del sistema. Intenta de nuevo más tarde.");
            return;
        }
        
        if (usuario == null) {
            mostrarError(request, response, "Correo o contraseña incorrectos.");
            return;
        }

        // Se descarta cualquier sesión previa y creamos una nueva
        HttpSession anterior = request.getSession(false);
        if (anterior != null) {
            anterior.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(30 * 60); // 30 minutos
        // Único atributo de sesión: lo leen todos los servlets y JSP
        session.setAttribute("usuarioLogueado", usuario);

        String ctx = request.getContextPath();
        switch (usuario.getRol()) {
            case "ADMINISTRADOR":
                response.sendRedirect(ctx + "/admin");
                break;
            case "TECNICO":
                response.sendRedirect(ctx + "/tecnico/dashboard");
                break;
            case "CLIENTE":
                response.sendRedirect(ctx + "/cliente/dashboard");
                break;
            default:
                session.invalidate();
                response.sendRedirect(ctx + "/login");
        }
    }

    private void mostrarError(HttpServletRequest request, HttpServletResponse response, String mensaje)
            throws ServletException, IOException {
        request.setAttribute("error", mensaje);
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }
}
