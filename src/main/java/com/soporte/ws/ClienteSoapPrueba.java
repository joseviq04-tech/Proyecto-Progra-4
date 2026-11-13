package com.soporte.ws;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Cliente de prueba del servicio SOAP.
 * Uso: java com.soporte.ws.ClienteSoapPrueba [idTicket]
 */
public class ClienteSoapPrueba {

    private static final String ENDPOINT = "http://localhost:8080/sistema-tickets/ws/ticketService";
    private static final String NAMESPACE = "http://ws.soporte.com/";

    public static void main(String[] args) throws Exception {
        int ticketId = (args.length > 0) ? Integer.parseInt(args[0]) : 1;

        // Mensaje SOAP de solicitud para la operación consultarEstadoTicket
        String solicitud =
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
              + "xmlns:ws=\"" + NAMESPACE + "\">"
              + "<soapenv:Body>"
              + "<ws:consultarEstadoTicket>"
              + "<ticketId>" + ticketId + "</ticketId>"
              + "</ws:consultarEstadoTicket>"
              + "</soapenv:Body>"
              + "</soapenv:Envelope>";

        System.out.println("=== Solicitud SOAP ===");
        System.out.println(solicitud);

        HttpURLConnection conexion = (HttpURLConnection) new URL(ENDPOINT).openConnection();
        conexion.setRequestMethod("POST");
        conexion.setDoOutput(true);
        conexion.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        conexion.setRequestProperty("SOAPAction", "\"\"");

        try (OutputStream salida = conexion.getOutputStream()) {
            salida.write(solicitud.getBytes(StandardCharsets.UTF_8));
        }

        int codigo = conexion.getResponseCode();
        InputStream entrada = (codigo >= 400) ? conexion.getErrorStream() : conexion.getInputStream();
        byte[] bytes = entrada.readAllBytes();

        System.out.println();
        System.out.println("=== Respuesta SOAP (HTTP " + codigo + ") ===");
        System.out.println(new String(bytes, StandardCharsets.UTF_8));

        if (codigo == 200) {
            Document documento = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(new ByteArrayInputStream(bytes));
            NodeList resultado = documento.getElementsByTagName("return");
            if (resultado.getLength() > 0) {
                System.out.println();
                System.out.println("=== Resultado ===");
                System.out.println(resultado.item(0).getTextContent());
            }
        }
    }
}
