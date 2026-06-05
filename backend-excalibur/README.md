# Backend Excalibur

Servicio Spring Boot para la operacion inicial de Excalibur.

## Stack

- Java 21
- Spring Boot 3.5.7
- Spring Web
- Spring Security
- Spring JDBC
- PostgreSQL
- JWT con JJWT
- BCrypt para passwords

## Configuracion

Por defecto usa:

- URL: `jdbc:postgresql://127.0.0.1:5432/excalibur`
- Usuario BD: `postgres`
- Password BD: `admin`
- Puerto HTTP: `8083`

Variables disponibles:

```bash
SERVER_PORT=8083
EXCALIBUR_DB_URL=jdbc:postgresql://127.0.0.1:5432/excalibur
EXCALIBUR_DB_USER=postgres
EXCALIBUR_DB_PASSWORD=admin
EXCALIBUR_JWT_SECRET=dev-excalibur-secret-change-me-dev-excalibur-secret
EXCALIBUR_JWT_EXPIRATION_MINUTES=480
```

## Ejecutar

```bash
mvn spring-boot:run
```

## Endpoints

Autenticacion:

- `POST /api/auth/login`
- `GET /api/auth/me`
- `PATCH /api/auth/me/password`
- `POST /api/auth/logout`

Usuarios:

- `GET /api/users`
- `GET /api/users/{id}`
- `POST /api/users`
- `PUT /api/users/{id}`
- `PATCH /api/users/{id}/password`
- `DELETE /api/users/{id}`

Roles:

- `GET /api/roles`

Todos los endpoints excepto `POST /api/auth/login` requieren:

```text
Authorization: Bearer <token>
```

## Usuario inicial

- Usuario: `Excalibur`
- Password temporal: `Excalibur2026!`
- Rol: `ADMIN`
