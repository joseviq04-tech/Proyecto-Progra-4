package com.soporte.service;

import com.soporte.model.Ticket;
import com.soporte.model.Usuario;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Envía notificaciones por correo electrónico (SMTP).
 * La configuración del servidor se lee de src/main/resources/correo.properties.
 * Si el envío falla, solo se registra el error: un problema con el correo
 * no debe impedir que el cambio de estado del ticket se guarde.
 */
public class NotificacionService {

    private static final String ARCHIVO_CONFIG = "correo.properties";

    /**
     * Avisa al cliente dueño del ticket de que su estado cambió.
     */
    public void notificarCambioEstado(Ticket ticket, Usuario cliente, String estadoAnterior, String estadoNuevo) {
        Properties config = cargarConfiguracion();
        if (config == null) {
            return;
        }

        String host = config.getProperty("smtp.host");
        String puerto = config.getProperty("smtp.port");
        final String usuario = config.getProperty("smtp.usuario");
        final String password = config.getProperty("smtp.password");
        String remitente = config.getProperty("correo.remitente");

        if (host == null || puerto == null || usuario == null || password == null || remitente == null) {
            System.err.println("[Correo] Falta algún dato en " + ARCHIVO_CONFIG);
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", puerto);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");

        Session sesion = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(usuario, password);
            }
        });

        try {
            MimeMessage mensaje = new MimeMessage(sesion);
            mensaje.setFrom(new InternetAddress(remitente, "Sistema de Soporte Técnico", "UTF-8"));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(cliente.getCorreo()));
            mensaje.setSubject("[Soporte] Ticket #" + ticket.getId() + ": ahora está " + estadoNuevo, "UTF-8");
            mensaje.setText(construirCuerpo(ticket, cliente, estadoAnterior, estadoNuevo), "UTF-8");

            Transport.send(mensaje);
            System.out.println("[Correo] Notificación enviada a " + cliente.getCorreo()
                    + " (ticket #" + ticket.getId() + ": " + estadoAnterior + " -> " + estadoNuevo + ")");

        } catch (Exception e) {
            System.err.println("[Correo] No se pudo enviar la notificación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String construirCuerpo(Ticket ticket, Usuario cliente, String estadoAnterior, String estadoNuevo) {
        return "Hola " + cliente.getNombre() + ",\n\n"
             + "El estado de tu ticket ha cambiado.\n\n"
             + "Ticket #" + ticket.getId() + ": " + ticket.getTitulo() + "\n"
             + "Estado anterior: " + estadoAnterior + "\n"
             + "Estado nuevo: " + estadoNuevo + "\n\n"
             + "Puedes consultar el detalle ingresando al portal de soporte.\n\n"
             + "Sistema de Soporte Técnico";
    }

    private Properties cargarConfiguracion() {
        try (InputStream entrada = NotificacionService.class.getClassLoader().getResourceAsStream(ARCHIVO_CONFIG)) {
            if (entrada == null) {
                System.err.println("[Correo] No se encontró el archivo " + ARCHIVO_CONFIG);
                return null;
            }
            Properties config = new Properties();
            config.load(entrada);
            return config;
        } catch (IOException e) {
            System.err.println("[Correo] No se pudo leer " + ARCHIVO_CONFIG + ": " + e.getMessage());
            return null;
        }
    }
}
