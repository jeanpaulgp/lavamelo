# Lava-Melo

Aplicación web para administrar un lavadero de vehículos. Cubre la operación diaria (clientes,
vehículos, órdenes de servicio, cobros e inventario) y entrega los reportes que el negocio necesita
para tomar decisiones. Todo corre en un único proceso: la misma aplicación expone la API, sirve la
interfaz web y publica su documentación.

El proyecto está pensado para funcionar de inmediato con datos de ejemplo en memoria, y para conectarse
más adelante a una base de datos real sin reescribir la lógica de negocio.

---

## Tecnologías

**Backend**
- Java 17 (compatible con versiones superiores; probado con JDK 23)
- Spring Boot 3.5 (Spring MVC para la API REST y el servido de archivos estáticos)
- Bean Validation (Jakarta) para validar los datos de entrada
- springdoc-openapi para la documentación interactiva
- Maven como gestor de dependencias, con Maven Wrapper incluido

**Frontend**
- HTML y JavaScript estándar (sin framework SPA)
- Tailwind CSS, Chart.js y Google Fonts servidos por CDN
- Enrutamiento por hash y renderizado por módulos

**Seguridad**
- Sesión basada en cookie (`HttpSession`)
- Contraseñas cifradas con PBKDF2-HMAC-SHA256 usando la librería estándar de Java

**Pruebas**
- JUnit 5

---

## Requisitos

- JDK 17 o superior instalado y disponible en el `PATH`.
- No es necesario instalar Maven: el repositorio incluye el wrapper (`mvnw` / `mvnw.cmd`).

---

## Puesta en marcha

```bash
# Windows (PowerShell)
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

La aplicación queda disponible en `http://localhost:8000`. La documentación de la API se publica en
`http://localhost:8000/docs`.

Para generar un ejecutable independiente:

```bash
./mvnw clean package
java -jar target/lava-melo-1.0.0.jar
```

El puerto puede cambiarse con la variable de entorno `PORT` (por defecto, 8000).

---

## Usuarios de prueba

Los datos de ejemplo incluyen un usuario por cada rol:

| Rol                   | Usuario      | Contraseña      |
|-----------------------|--------------|-----------------|
| Administrador         | `admin`      | `admin123`      |
| Recepcionista / Cajero| `recepcion`  | `recepcion123`  |
| Operario              | `operario`   | `operario123`   |

Existe además un segundo operario (`operario2` / `operario123`) para que los reportes muestren el
trabajo repartido entre varias personas.

---

## Roles y permisos

Los permisos se aplican en el backend; el frontend simplemente oculta lo que el rol no puede usar.

- **Administrador.** Acceso completo: clientes, vehículos, servicios, empleados, proveedores,
  inventario, compras, pagos y reportes.
- **Recepcionista / Cajero.** Registra clientes y vehículos, consulta historiales, crea las órdenes,
  cobra y emite el recibo. No accede a empleados, inventario ni reportes.
- **Operario.** Ve únicamente las órdenes que tiene asignadas y actualiza su estado (En proceso / Listo).

---

## Cómo funciona la operación

El recorrido habitual es:

1. Se registra el cliente y su vehículo (placa única, con categoría: moto, automóvil o camioneta).
2. Recepción crea la orden, elige los servicios y asigna un operario.
3. La orden avanza por los estados **En espera → En proceso → Listo → Finalizado**. Solo se avanza de
   a un paso; no se puede retroceder.
4. El total se calcula con el precio de cada servicio según la categoría del vehículo. Ese precio queda
   "congelado" en la orden, de modo que un cambio de tarifa posterior no altera órdenes ya creadas.
5. Cuando la orden está *Lista*, se cobra (Efectivo, Nequi o Tarjeta). El pago la marca como
   *Finalizada* de forma automática. Una orden se cobra una sola vez.
6. Se puede imprimir el recibo desde el navegador y la venta aparece en los reportes.

El inventario se mueve por compras a proveedores (entradas) y por salidas o ajustes con motivo. Cuando
un producto llega a su stock mínimo, se muestra una alerta en el tablero y en la lista.

---

## Reportes

Disponibles para el Administrador, responden las preguntas clave del negocio:

- Vehículos atendidos hoy.
- Ingresos de la semana.
- Servicio más solicitado.
- Clientes más frecuentes.
- Servicios realizados por cada operario.
- Insumos próximos a agotarse.
- Clientes que no regresan hace más de 60 días.
- Día del mes con mayores ventas.

Se suma un desglose de ventas por método de pago y la posibilidad de filtrar por rango de fechas.

---

## Estructura del proyecto

```
src/main/java/com/lavamelo/
  LavaMeloApplication.java     Punto de arranque
  auth/                        Sesión, control de roles y cifrado de contraseñas
  model/                       Entidades del dominio
  service/                     Lógica de negocio (validaciones, estados, totales, reportes)
  web/                         Controladores REST, uno por módulo
  repository/                  Contrato de datos y sus implementaciones (memoria y SQL)
src/main/resources/
  application.properties
  static/                      index.html, login.html, recibo.html, css/, js/, img/
src/test/java/com/lavamelo/    Pruebas
docs/CONTRATO_BD.md            Contrato de base de datos para la capa de persistencia
```

La regla que sostiene el diseño: **toda la lógica de negocio vive en los servicios y todo el acceso a
datos pasa por una única interfaz de repositorio.** Así, cambiar el origen de datos no obliga a tocar
la lógica ni la interfaz.

---

## Base de datos

La persistencia se resuelve a través de la interfaz `repository/Repositorio.java`. La implementación se
elige con la variable de entorno `REPO`:

- `REPO=memory` (valor por defecto): datos de ejemplo en memoria. Ideal para desarrollar y demostrar.
  Los datos se reinician cada vez que se reinicia la aplicación.
- `REPO=sql`: implementación sobre base de datos.

```bash
# Windows
$env:REPO="sql"; .\mvnw.cmd spring-boot:run

# Linux / macOS
REPO=sql ./mvnw spring-boot:run
```

El detalle de entidades, campos y la firma de cada método está en
[`docs/CONTRATO_BD.md`](docs/CONTRATO_BD.md), que sirve como especificación para quien implemente la
capa de datos.

---

## Pruebas

```bash
./mvnw test
```

La batería cubre los puntos más sensibles del sistema: las transiciones de estado de las órdenes, el
cálculo del total según la categoría del vehículo, los permisos por rol (incluido el filtro que limita
al operario a sus propias órdenes) y la restricción de un único cobro por orden.

---

## Estado actual y pendientes

Lo que ya está terminado y funcionando:

- Operación completa: clientes, vehículos, servicios, órdenes, pagos, recibo e inventario.
- Autenticación por sesión y control de acceso por rol, verificado también a nivel de API.
- Los ocho reportes del negocio y los tableros por rol.
- Datos de ejemplo que alimentan todas las vistas y reportes desde el primer arranque.

Lo que queda por hacer:

- **Implementar `repository/sql.java`.** Hoy es un esqueleto que declara los mismos métodos que la
  versión en memoria pero aún no habla con ninguna base de datos. Es el trabajo que corresponde a la
  capa de persistencia y está documentado en el contrato.
- **Definir el motor y el esquema de base de datos** (por ejemplo PostgreSQL o MySQL), agregar el
  driver correspondiente y configurar la conexión.
- **Persistir las sesiones** si se despliega en más de una instancia (hoy viven en memoria del proceso).

Fuera del alcance de esta versión, de forma deliberada: contenedores, microservicios, ORM, migraciones,
colas, notificaciones, pagos en línea, facturación electrónica y multi-sucursal.

---

## Decisiones y supuestos

- La consigna pedía backend en Java; se eligió Spring Boot por ser el camino natural para servir en un
  solo proceso la API, los archivos estáticos y la documentación. El puerto por defecto (8000) se
  mantiene por comodidad.
- La entidad `Empleado` cumple también el papel de usuario del sistema (guarda usuario, contraseña
  cifrada, rol y estado activo), lo que evita una tabla adicional.
- "Eliminar" un servicio es en realidad una baja lógica: queda inactivo para no romper el historial de
  órdenes que lo usaron.
- No se permite eliminar registros con historial asociado (un cliente con vehículos, un vehículo con
  órdenes, un empleado con órdenes o un proveedor con compras). En esos casos se recomienda desactivar.
- El stock de un producto no se edita a mano: cambia únicamente a partir de compras, salidas o ajustes,
  para que siempre exista una trazabilidad del movimiento.
- El recibo es una página HTML preparada para impresión; no se genera un PDF.
