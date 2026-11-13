# Sistema de soporte técnico en línea

Proyecto de **Programación IV** · Universidad Latina, Sede Heredia. Aplicación web en capas (Servlets/JSP, JDBC/MySQL) para gestionar tickets de soporte con tres roles (cliente, técnico y administrador), historial de cambios, notificaciones por correo y un servicio web SOAP.

## Documentación

Toda la documentación técnica está en el **[manual técnico](docs/Manual_Tecnico.pdf)**: instalación y despliegue (Linux y Windows), arquitectura en capas con su diagrama, estructura de paquetes, páginas y endpoints, servicio SOAP, modelo de datos, seguridad y usuarios de prueba.

## Inicio rápido

Requisitos: JDK 17, Maven y MySQL 8.

```bash
mysql -u root -p < schema.sql
mvn clean package tomcat7:run
```

Luego se abre http://localhost:8080/sistema-tickets/login. Los usuarios de prueba (contraseña `123456`), la configuración opcional del correo y la solución de problemas están en el manual.
