# Diseño — Lava-Melo

## Decisión de stack
El prompt pide backend en Java (el resto de la plantilla menciona FastAPI/uvicorn). Se usa **Java 17+ con Spring Boot 3.5** (un solo proceso, JAR ejecutable) y **springdoc** para `/docs`. Frontend estático (HTML + JS vanilla, Tailwind/Chart.js por CDN) servido por el mismo proceso desde `src/main/resources/static`.

Equivalencias con la estructura sugerida:

| Sugerido (Python)       | Implementado (Java)                              |
|-------------------------|--------------------------------------------------|
| `app/main.py`           | `LavaMeloApplication.java` + `WebConfig.java`    |
| `app/auth.py`           | `auth/` (sesión, `@RequiereRol`, interceptor, PBKDF2) |
| `app/models.py`         | `model/` (records)                               |
| `app/services/`         | `service/`                                       |
| `app/routers/`          | `web/` (un controlador por módulo)               |
| `app/repositories/`     | `repository/` (`Repositorio`, `MemoryRepositorio`, `SqlRepositorio`) |
| `requirements.txt`      | `pom.xml` + Maven Wrapper (`mvnw`)               |
| `pytest`                | JUnit 5 (`src/test`)                             |

## Capas
```
Navegador ──► web/*Controller ──► service/*Service ──► Repositorio (interfaz)
                 ▲ RolInterceptor                          ├─ MemoryRepositorio (default, semilla)
                                                           └─ SqlRepositorio (esqueleto)
```
- **Controladores**: HTTP, DTOs de entrada, anotación `@RequiereRol`.
- **Servicios**: validaciones, transiciones de estado, cálculo de totales, stock, historial y armado de reportes.
- **Repositorio**: solo persistencia y consultas (CRUD + consultas agregadas de reportes). Sin reglas de negocio.
- `RepositorioConfig` crea el bean según `REPO` (default `memory`).

## Autenticación y permisos
- `POST /api/auth/login` guarda `empleadoId` y `rol` en la `HttpSession` (cookie `JSESSIONID`, HttpOnly, SameSite=Lax).
- `RolInterceptor` protege `/api/**` (excepto login): 401 sin sesión, 403 si el rol no está en `@RequiereRol`.
- El filtro "operario solo ve lo suyo" se aplica en `OrdenService`.
- Contraseñas: PBKDF2-HMAC-SHA256, 120.000 iteraciones, sal aleatoria (`javax.crypto`).

## Modelo (resumen)
CLIENTE, VEHICULO(cliente), SERVICIO(precios por categoría), EMPLEADO(usuario, hash, rol, activo), ORDEN_SERVICIO(cliente, vehículo, empleado, estado, total), DETALLE_ORDEN(orden, servicio, precio), PAGO(orden único, método, monto), PROVEEDOR, COMPRA(proveedor), DETALLE_COMPRA(compra, producto), PRODUCTO(stock, stock_minimo), INVENTARIO(movimiento: ENTRADA/SALIDA/AJUSTE con motivo). Detalle completo en `docs/CONTRATO_BD.md`.

## Frontend
- `login.html`, `index.html` (shell con barra lateral y router por hash), `recibo.html` (estilo impresión).
- `js/api.js` (fetch + formato COP/fecha), `js/app.js` (sesión, menú por rol, router), `js/views/*.js` (una vista por módulo).
- Colores de estado: En espera ámbar, En proceso azul, Listo verde, Finalizado gris.

## Errores
`ApiException(status, mensaje)` y validaciones Jakarta se traducen a `{"detail": "..."}` con el código HTTP adecuado.
