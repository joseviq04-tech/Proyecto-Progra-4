package com.soporte.model;

public class HistorialCambio {
    private int id;
    private int ticketId;
    private String ticketTitulo;    // Título del ticket 
    private int usuarioId;
    private String usuarioNombre;   // Quién hizo el cambio 
    private String estadoAnterior;
    private String estadoNuevo;
    private String comentario;
    private String fechaCambio;

    public HistorialCambio() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTicketId() { return ticketId; }
    public void setTicketId(int ticketId) { this.ticketId = ticketId; }

    public String getTicketTitulo() { return ticketTitulo; }
    public void setTicketTitulo(String ticketTitulo) { this.ticketTitulo = ticketTitulo; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public String getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(String estadoAnterior) { this.estadoAnterior = estadoAnterior; }

    public String getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(String estadoNuevo) { this.estadoNuevo = estadoNuevo; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public String getFechaCambio() { return fechaCambio; }
    public void setFechaCambio(String fechaCambio) { this.fechaCambio = fechaCambio; }
}
