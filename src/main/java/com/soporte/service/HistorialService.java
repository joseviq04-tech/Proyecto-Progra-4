package com.soporte.service;

import com.soporte.dao.HistorialDAO;
import com.soporte.model.HistorialCambio;
import java.sql.SQLException;
import java.util.List;

/**
 * Capa de lógica de negocio para consultar el historial general de cambios de estado.
 * Solo el administrador puede llegar aquí 
 */
public class HistorialService {

    private final HistorialDAO historialDAO = new HistorialDAO();

    public List<HistorialCambio> listarTodo() throws SQLException {
        return historialDAO.listarTodos();
    }
}
