package com.soporte.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Crea las conexiones a MySQL.
 * Los datos se leen, en este orden de prioridad, de:
 *   1. variables de entorno DB_URL, DB_USER y DB_PASSWORD
 *   2. el archivo db.properties (en src/main/resources)
 *   3. valores por defecto (MySQL local, usuario soporte_user)
 */
public class Conexion {

    private static final String URL_DEFECTO =
            "jdbc:mysql://localhost:3306/sistema_soporte"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
    private static final String USUARIO_DEFECTO = "soporte_user";
    private static final String PASSWORD_DEFECTO = "123456";

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        Properties props = new Properties();
        try (InputStream in = Conexion.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            } else {
                System.err.println("[BD] No se encontró db.properties; se usan los valores por defecto.");
            }
        } catch (IOException e) {
            System.err.println("[BD] No se pudo leer db.properties; se usan los valores por defecto.");
        }

        URL = elegir(System.getenv("DB_URL"), props.getProperty("db.url"), URL_DEFECTO);
        USER = elegir(System.getenv("DB_USER"), props.getProperty("db.usuario"), USUARIO_DEFECTO);
        PASSWORD = elegir(System.getenv("DB_PASSWORD"), props.getProperty("db.password"), PASSWORD_DEFECTO);
    }

    // Devuelve el primer valor que no esté vacío
    private static String elegir(String entorno, String archivo, String defecto) {
        if (entorno != null && !entorno.trim().isEmpty()) {
            return entorno.trim();
        }
        if (archivo != null && !archivo.trim().isEmpty()) {
            return archivo.trim();
        }
        return defecto;
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver de MySQL no encontrado: " + e.getMessage());
        }
    }
}
