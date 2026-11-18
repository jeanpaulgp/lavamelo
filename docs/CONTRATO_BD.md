# Contrato de Base de Datos — Lava-Melo

Documento para quien implemente la persistencia real (`REPO=sql`). La lógica de negocio vive en la
capa de servicios, así que la base de datos solo debe **guardar y consultar**. Basta con implementar
todos los métodos de `com.lavamelo.repository.Repositorio` en `SqlRepositorio.java`; no hay que tocar
servicios, controladores ni frontend.

La referencia de comportamiento exacto (orden de resultados, filtros, agrupaciones) está en
`MemoryRepositorio.java`. Los campos marcados **(a confirmar con BD)** son extensiones al modelo de
la presentación necesarias para el alcance.

## Convenciones
- `id`: entero autoincremental (BIGINT). Al **crear**, la entidad llega con `id = null` y se devuelve con el id asignado.
- `actualizar*`: reemplaza la fila con el mismo `id` y la devuelve.
- Rangos `desde`/`hasta`: **inclusivos**, comparando solo la parte de fecha (`DATE(fecha)`).
- Montos en pesos colombianos como enteros (sin decimales).
- Enumeraciones guardadas como texto (o su equivalente en la BD): ver más abajo.

## Enumeraciones
| Enum            | Valores |
|-----------------|---------|
| `Categoria`     | MOTO, AUTOMOVIL, CAMIONETA |
| `Rol`           | ADMIN, RECEPCION, OPERARIO |
| `EstadoOrden`   | EN_ESPERA, EN_PROCESO, LISTO, FINALIZADO |
| `MetodoPago`    | EFECTIVO, NEQUI, TARJETA |
| `TipoMovimiento`| ENTRADA, SALIDA, AJUSTE |

---

## Entidades y campos mínimos

### CLIENTE
`id`, `documento` (único), `nombre`, `telefono`, `email`, `direccion`, `fechaRegistro` (DATE)

### VEHICULO
`id`, `placa` (**única**), `marca`, `modelo`, `color`, `categoria` (Categoria), `clienteId` → CLIENTE
- Relación: N vehículos por cliente.

### SERVICIO
`id`, `nombre`, `descripcion`, `duracionMin` (int, **a confirmar con BD**), `precios` (**a confirmar con BD**), `activo` (bool)
- `precios`: un precio por cada `Categoria`. Sugerencia SQL: tabla `SERVICIO_PRECIO(servicioId, categoria, precio)` con PK `(servicioId, categoria)`.

### EMPLEADO  *(reemplaza a una tabla USUARIO)*
`id`, `documento`, `nombre`, `telefono`, `usuario` (**único, a confirmar con BD**), `passwordHash` (**a confirmar con BD**), `rol` (Rol), `activo` (bool)
- `passwordHash`: cadena PBKDF2 con formato `pbkdf2_sha256$iteraciones$sal$hash`. **Nunca** guardar contraseñas en texto plano.

### ORDEN_SERVICIO
`id`, `clienteId` → CLIENTE, `vehiculoId` → VEHICULO, `empleadoId` → EMPLEADO (operario), `fecha` (TIMESTAMP), `estado` (EstadoOrden), `total` (entero), `observaciones`

### DETALLE_ORDEN
`id`, `ordenId` → ORDEN_SERVICIO, `servicioId` → SERVICIO, `precio` (entero, congelado al crear la orden)
- Relación: N detalles por orden.

### PAGO
`id`, `ordenId` → ORDEN_SERVICIO (**UNIQUE**: una orden se paga una sola vez), `fecha` (TIMESTAMP), `monto` (entero), `metodo` (MetodoPago)

### PROVEEDOR
`id`, `nit`, `nombre`, `telefono`, `email`, `direccion`

### COMPRA
`id`, `proveedorId` → PROVEEDOR, `fecha` (TIMESTAMP), `total` (entero)

### DETALLE_COMPRA
`id`, `compraId` → COMPRA, `productoId` → PRODUCTO, `cantidad` (int), `costoUnitario` (entero)

### PRODUCTO
`id`, `nombre`, `unidad`, `stock` (int), `stockMinimo` (int, **a confirmar con BD**, para alertas)

### INVENTARIO  *(movimientos / kardex)*
`id`, `productoId` → PRODUCTO, `fecha` (TIMESTAMP), `tipo` (TipoMovimiento), `cantidad` (int **con signo**: + entra, − sale), `motivo`, `compraId` → COMPRA (solo en ENTRADA, si aplica)
- Entradas: provienen de COMPRA/DETALLE_COMPRA (`tipo = ENTRADA`).
- Salidas/consumo y correcciones: `tipo = SALIDA` o `AJUSTE`, con `motivo`.

---

## Métodos del repositorio (firmas)

> Tipos: `Optional<T>` = puede no existir; `List<T>` = colección; `long` = agregados.

### Cliente
- `List<Cliente> listarClientes()`
- `List<Cliente> buscarClientes(String texto)` — LIKE en nombre, documento o teléfono (sin distinguir mayúsculas)
- `Optional<Cliente> obtenerCliente(long id)`
- `Optional<Cliente> buscarClientePorDocumento(String documento)`
- `Cliente crearCliente(Cliente)` · `Cliente actualizarCliente(Cliente)` · `void eliminarCliente(long id)`

### Vehículo
- `List<Vehiculo> listarVehiculos()`
- `List<Vehiculo> listarVehiculosPorCliente(long clienteId)`
- `Optional<Vehiculo> obtenerVehiculo(long id)`
- `Optional<Vehiculo> buscarVehiculoPorPlaca(String placa)` — placa ya normalizada en mayúsculas
- `Vehiculo crearVehiculo(Vehiculo)` · `Vehiculo actualizarVehiculo(Vehiculo)` · `void eliminarVehiculo(long id)`

### Servicio
- `List<Servicio> listarServicios()` · `Optional<Servicio> obtenerServicio(long id)`
- `Servicio crearServicio(Servicio)` · `Servicio actualizarServicio(Servicio)` · `void eliminarServicio(long id)`

### Empleado
- `List<Empleado> listarEmpleados()` · `Optional<Empleado> obtenerEmpleado(long id)`
- `Optional<Empleado> buscarEmpleadoPorUsuario(String usuario)`
- `Empleado crearEmpleado(Empleado)` · `Empleado actualizarEmpleado(Empleado)` · `void eliminarEmpleado(long id)`

### Orden y detalle
- `List<OrdenServicio> listarOrdenes(FiltroOrdenes filtro)` — aplica cada campo del filtro solo si no es null; **ORDER BY fecha DESC, id DESC**
- `Optional<OrdenServicio> obtenerOrden(long id)`
- `OrdenServicio crearOrden(OrdenServicio)` · `OrdenServicio actualizarOrden(OrdenServicio)`
- `List<DetalleOrden> listarDetallesOrden(long ordenId)` · `DetalleOrden crearDetalleOrden(DetalleOrden)`
- `FiltroOrdenes(estado, empleadoId, clienteId, vehiculoId, desde, hasta)` — todos opcionales.

### Pago
- `List<Pago> listarPagos(LocalDate desde, LocalDate hasta)` — ORDER BY fecha DESC
- `Optional<Pago> obtenerPagoPorOrden(long ordenId)`
- `Pago crearPago(Pago)` — respetar UNIQUE(ordenId)

### Proveedor
- `List<Proveedor> listarProveedores()` · `Optional<Proveedor> obtenerProveedor(long id)`
- `Proveedor crearProveedor(Proveedor)` · `Proveedor actualizarProveedor(Proveedor)` · `void eliminarProveedor(long id)`

### Compra y detalle
- `List<Compra> listarCompras()` — ORDER BY fecha DESC
- `List<Compra> listarComprasPorProveedor(long proveedorId)`
- `Optional<Compra> obtenerCompra(long id)` · `Compra crearCompra(Compra)`
- `List<DetalleCompra> listarDetallesCompra(long compraId)` · `DetalleCompra crearDetalleCompra(DetalleCompra)`

### Producto
- `List<Producto> listarProductos()` · `Optional<Producto> obtenerProducto(long id)`
- `Producto crearProducto(Producto)` · `Producto actualizarProducto(Producto)` · `void eliminarProducto(long id)`

### Inventario (movimientos)
- `List<Inventario> listarMovimientos(Long productoId)` — `null` = todos; ORDER BY fecha DESC, id DESC
- `Inventario crearMovimiento(Inventario)`

### Consultas de reportes
| Método | Devuelve | Consulta esperada |
|--------|----------|-------------------|
| `contarVehiculosAtendidos(desde, hasta)` | `long` | `COUNT(DISTINCT vehiculoId)` de órdenes en el rango |
| `sumarIngresos(desde, hasta)` | `long` | `SUM(monto)` de pagos en el rango |
| `serviciosMasSolicitados(desde, hasta)` | `List<Conteo>` | por servicio: nº de detalles y `SUM(precio)`, mayor a menor |
| `clientesFrecuentes(desde, hasta, limite)` | `List<Conteo>` | por cliente: nº de órdenes y `SUM(total)`, top `limite` |
| `serviciosPorOperario(desde, hasta)` | `List<Conteo>` | por operario, órdenes en estado LISTO o FINALIZADO |
| `productosStockBajo()` | `List<Producto>` | `stock <= stockMinimo`, menor stock primero |
| `clientesSinVisitasDesde(fechaCorte)` | `List<ClienteInactivo>` | clientes con última orden anterior a `fechaCorte` |
| `ventasPorDia(desde, hasta)` | `List<VentaDia>` | `SUM(monto)` y nº de pagos por día, orden cronológico |
| `ventasPorMetodo(desde, hasta)` | `List<Conteo>` | por método: nº de pagos y `SUM(monto)` |

Tipos de resultado:
- `Conteo(String etiqueta, long cantidad, long total)`
- `ClienteInactivo(Long clienteId, String nombre, String telefono, LocalDateTime ultimaVisita)`
- `VentaDia(LocalDate fecha, long total, long pagos)`

---

## Pasos para conectar la BD
1. Agregar al `pom.xml`: `spring-boot-starter-jdbc` y el driver (PostgreSQL, MySQL, etc.).
2. Configurar `spring.datasource.*` (URL, usuario, contraseña) por variables de entorno o `application.properties`.
3. Inyectar un `JdbcTemplate`/`DataSource` en `SqlRepositorio` (ajustar `RepositorioConfig.repositorio`).
4. Implementar cada método usando las firmas de arriba.
5. Arrancar con `REPO=sql`.
