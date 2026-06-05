# Excalibur Operacion

Sistema operativo Excalibur para administracion de usuarios, roles, configuracion operativa y flujo de jornada.

## Modulos

- `backend-excalibur`: API Spring Boot con seguridad JWT, usuarios, roles y configuracion operativa.
- `frontend-excalibur`: Frontend Vue para panel administrativo y supervisor.
- `database`: Scripts SQL para base de datos PostgreSQL.

## Servicios locales

- Backend: `http://192.168.1.94:8083`
- Frontend: `http://192.168.1.94:8084/`

## Validacion

```bash
cd backend-excalibur && mvn -q -DskipTests package
cd frontend-excalibur && npm run build
```
