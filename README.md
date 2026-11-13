# Sistema de soporte técnico en línea

Proyecto de **Programación IV** · Universidad Latina, Sede Heredia. Aplicación web en capas (Servlets/JSP, JDBC/MySQL) para gestionar tickets de soporte con tres roles (cliente, técnico y administrador), historial de cambios, notificaciones por correo y un servicio web SOAP.

## Documentación

- **[Manual técnico](docs/Manual_Tecnico.pdf)**: instalación y despliegue (Linux y Windows), arquitectura en capas con su diagrama, estructura de paquetes, páginas y endpoints, servicio SOAP, notificaciones por correo, modelo de datos, seguridad y usuarios de prueba.
- **[Esquema de la base de datos](docs/esquema.pdf)**: diagrama entidad-relación, tablas con sus columnas y restricciones, y relaciones.

## Inicio rápido

Requisitos: JDK 17, Maven y MySQL 8.

```bash
mysql -u root -p < schema.sql
mvn clean package tomcat7:run
```

Luego se abre http://localhost:8080/sistema-tickets/login. Los usuarios de prueba (contraseña `123456`), la solución de problemas y el detalle de cada paso están en el manual.

## Configuración opcional

Los archivos de configuración con datos reales son locales y no se suben al repositorio (están en el `.gitignore`). Se crean copiando su plantilla:

| Archivo | Para qué sirve | Plantilla |
|---|---|---|
| `src/main/resources/db.properties` | Conexión a MySQL. Si no existe, se usan valores por defecto (`localhost:3306`, usuario `soporte_user`) que coinciden con `schema.sql`. | `db.properties.example` |
| `src/main/resources/correo.properties` | Envío de notificaciones por correo (se usa Mailtrap como SMTP de pruebas). Si no existe, el sistema funciona igual pero no envía correos. | `correo.properties.example` |

```bash
cp src/main/resources/db.properties.example src/main/resources/db.properties
cp src/main/resources/correo.properties.example src/main/resources/correo.properties
```

Después se editan los valores (por ejemplo, las credenciales de la bandeja de Mailtrap en `correo.properties`).
