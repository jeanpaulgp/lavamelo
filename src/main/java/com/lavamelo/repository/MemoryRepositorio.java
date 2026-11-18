package com.lavamelo.repository;

import com.lavamelo.auth.Contrasenas;
import com.lavamelo.model.Modelos.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementación en memoria (por defecto). Los datos se pierden al reiniciar.
 * Incluye datos semilla para que todos los módulos y reportes muestren información.
 */
public class MemoryRepositorio implements Repositorio {

    /** Tabla genérica en memoria con autoincremento. */
    private static final class Tabla<T extends ConId<T>> {
        private final Map<Long, T> filas = new LinkedHashMap<>();
        private final AtomicLong secuencia = new AtomicLong();

        List<T> todos() {
            return new ArrayList<>(filas.values());
        }

        Optional<T> obtener(long id) {
            return Optional.ofNullable(filas.get(id));
        }

        T insertar(T entidad) {
            T conId = entidad.conId(secuencia.incrementAndGet());
            filas.put(conId.id(), conId);
            return conId;
        }

        T reemplazar(T entidad) {
            if (entidad.id() == null || !filas.containsKey(entidad.id())) {
                throw new NoSuchElementException("No existe el registro con id " + entidad.id());
            }
            filas.put(entidad.id(), entidad);
            return entidad;
        }

        void eliminar(long id) {
            filas.remove(id);
        }
    }

    private final Tabla<Cliente> clientes = new Tabla<>();
    private final Tabla<Vehiculo> vehiculos = new Tabla<>();
    private final Tabla<Servicio> servicios = new Tabla<>();
    private final Tabla<Empleado> empleados = new Tabla<>();
    private final Tabla<OrdenServicio> ordenes = new Tabla<>();
    private final Tabla<DetalleOrden> detallesOrden = new Tabla<>();
    private final Tabla<Pago> pagos = new Tabla<>();
    private final Tabla<Proveedor> proveedores = new Tabla<>();
    private final Tabla<Compra> compras = new Tabla<>();
    private final Tabla<DetalleCompra> detallesCompra = new Tabla<>();
    private final Tabla<Producto> productos = new Tabla<>();
    private final Tabla<Inventario> movimientos = new Tabla<>();

    public MemoryRepositorio() {
        this(true);
    }

    public MemoryRepositorio(boolean conSemilla) {
        if (conSemilla) cargarSemilla();
    }

    private static boolean enRango(LocalDateTime fecha, LocalDate desde, LocalDate hasta) {
        LocalDate d = fecha.toLocalDate();
        return (desde == null || !d.isBefore(desde)) && (hasta == null || !d.isAfter(hasta));
    }

    private static boolean contiene(String campo, String texto) {
        return campo != null && campo.toLowerCase(Locale.ROOT).contains(texto);
    }

    // ============================================================ CLIENTE

    @Override
    public synchronized List<Cliente> listarClientes() {
        return clientes.todos();
    }

    @Override
    public synchronized List<Cliente> buscarClientes(String texto) {
        String t = texto.toLowerCase(Locale.ROOT).trim();
        return clientes.todos().stream()
                .filter(c -> contiene(c.nombre(), t) || contiene(c.documento(), t) || contiene(c.telefono(), t))
                .toList();
    }

    @Override
    public synchronized Optional<Cliente> obtenerCliente(long id) {
        return clientes.obtener(id);
    }

    @Override
    public synchronized Optional<Cliente> buscarClientePorDocumento(String documento) {
        return clientes.todos().stream().filter(c -> c.documento().equalsIgnoreCase(documento)).findFirst();
    }

    @Override
    public synchronized Cliente crearCliente(Cliente cliente) {
        return clientes.insertar(cliente);
    }

    @Override
    public synchronized Cliente actualizarCliente(Cliente cliente) {
        return clientes.reemplazar(cliente);
    }

    @Override
    public synchronized void eliminarCliente(long id) {
        clientes.eliminar(id);
    }

    // ============================================================ VEHICULO

    @Override
    public synchronized List<Vehiculo> listarVehiculos() {
        return vehiculos.todos();
    }

    @Override
    public synchronized List<Vehiculo> listarVehiculosPorCliente(long clienteId) {
        return vehiculos.todos().stream().filter(v -> v.clienteId() == clienteId).toList();
    }

    @Override
    public synchronized Optional<Vehiculo> obtenerVehiculo(long id) {
        return vehiculos.obtener(id);
    }

    @Override
    public synchronized Optional<Vehiculo> buscarVehiculoPorPlaca(String placa) {
        return vehiculos.todos().stream().filter(v -> v.placa().equalsIgnoreCase(placa)).findFirst();
    }

    @Override
    public synchronized Vehiculo crearVehiculo(Vehiculo vehiculo) {
        return vehiculos.insertar(vehiculo);
    }

    @Override
    public synchronized Vehiculo actualizarVehiculo(Vehiculo vehiculo) {
        return vehiculos.reemplazar(vehiculo);
    }

    @Override
    public synchronized void eliminarVehiculo(long id) {
        vehiculos.eliminar(id);
    }

    // ============================================================ SERVICIO

    @Override
    public synchronized List<Servicio> listarServicios() {
        return servicios.todos();
    }

    @Override
    public synchronized Optional<Servicio> obtenerServicio(long id) {
        return servicios.obtener(id);
    }

    @Override
    public synchronized Servicio crearServicio(Servicio servicio) {
        return servicios.insertar(servicio);
    }

    @Override
    public synchronized Servicio actualizarServicio(Servicio servicio) {
        return servicios.reemplazar(servicio);
    }

    @Override
    public synchronized void eliminarServicio(long id) {
        servicios.eliminar(id);
    }

    // ============================================================ EMPLEADO

    @Override
    public synchronized List<Empleado> listarEmpleados() {
        return empleados.todos();
    }

    @Override
    public synchronized Optional<Empleado> obtenerEmpleado(long id) {
        return empleados.obtener(id);
    }

    @Override
    public synchronized Optional<Empleado> buscarEmpleadoPorUsuario(String usuario) {
        return empleados.todos().stream().filter(e -> e.usuario().equalsIgnoreCase(usuario)).findFirst();
    }

    @Override
    public synchronized Empleado crearEmpleado(Empleado empleado) {
        return empleados.insertar(empleado);
    }

    @Override
    public synchronized Empleado actualizarEmpleado(Empleado empleado) {
        return empleados.reemplazar(empleado);
    }

    @Override
    public synchronized void eliminarEmpleado(long id) {
        empleados.eliminar(id);
    }

    // ============================================================ ORDEN / DETALLE

    @Override
    public synchronized List<OrdenServicio> listarOrdenes(FiltroOrdenes f) {
        return ordenes.todos().stream()
                .filter(o -> f.estado() == null || o.estado() == f.estado())
                .filter(o -> f.empleadoId() == null || f.empleadoId().equals(o.empleadoId()))
                .filter(o -> f.clienteId() == null || f.clienteId().equals(o.clienteId()))
                .filter(o -> f.vehiculoId() == null || f.vehiculoId().equals(o.vehiculoId()))
                .filter(o -> enRango(o.fecha(), f.desde(), f.hasta()))
                .sorted(Comparator.comparing(OrdenServicio::fecha).thenComparing(OrdenServicio::id).reversed())
                .toList();
    }

    @Override
    public synchronized Optional<OrdenServicio> obtenerOrden(long id) {
        return ordenes.obtener(id);
    }

    @Override
    public synchronized OrdenServicio crearOrden(OrdenServicio orden) {
        return ordenes.insertar(orden);
    }

    @Override
    public synchronized OrdenServicio actualizarOrden(OrdenServicio orden) {
        return ordenes.reemplazar(orden);
    }

    @Override
    public synchronized List<DetalleOrden> listarDetallesOrden(long ordenId) {
        return detallesOrden.todos().stream().filter(d -> d.ordenId() == ordenId).toList();
    }

    @Override
    public synchronized DetalleOrden crearDetalleOrden(DetalleOrden detalle) {
        return detallesOrden.insertar(detalle);
    }

    // ============================================================ PAGO

    @Override
    public synchronized List<Pago> listarPagos(LocalDate desde, LocalDate hasta) {
        return pagos.todos().stream()
                .filter(p -> enRango(p.fecha(), desde, hasta))
                .sorted(Comparator.comparing(Pago::fecha).reversed())
                .toList();
    }

    @Override
    public synchronized Optional<Pago> obtenerPagoPorOrden(long ordenId) {
        return pagos.todos().stream().filter(p -> p.ordenId() == ordenId).findFirst();
    }

    @Override
    public synchronized Pago crearPago(Pago pago) {
        return pagos.insertar(pago);
    }

    // ============================================================ PROVEEDOR

    @Override
    public synchronized List<Proveedor> listarProveedores() {
        return proveedores.todos();
    }

    @Override
    public synchronized Optional<Proveedor> obtenerProveedor(long id) {
        return proveedores.obtener(id);
    }

    @Override
    public synchronized Proveedor crearProveedor(Proveedor proveedor) {
        return proveedores.insertar(proveedor);
    }

    @Override
    public synchronized Proveedor actualizarProveedor(Proveedor proveedor) {
        return proveedores.reemplazar(proveedor);
    }

    @Override
    public synchronized void eliminarProveedor(long id) {
        proveedores.eliminar(id);
    }

    // ============================================================ COMPRA / DETALLE

    @Override
    public synchronized List<Compra> listarCompras() {
        return compras.todos().stream().sorted(Comparator.comparing(Compra::fecha).reversed()).toList();
    }

    @Override
    public synchronized List<Compra> listarComprasPorProveedor(long proveedorId) {
        return listarCompras().stream().filter(c -> c.proveedorId() == proveedorId).toList();
    }

    @Override
    public synchronized Optional<Compra> obtenerCompra(long id) {
        return compras.obtener(id);
    }

    @Override
    public synchronized Compra crearCompra(Compra compra) {
        return compras.insertar(compra);
    }

    @Override
    public synchronized List<DetalleCompra> listarDetallesCompra(long compraId) {
        return detallesCompra.todos().stream().filter(d -> d.compraId() == compraId).toList();
    }

    @Override
    public synchronized DetalleCompra crearDetalleCompra(DetalleCompra detalle) {
        return detallesCompra.insertar(detalle);
    }

    // ============================================================ PRODUCTO

    @Override
    public synchronized List<Producto> listarProductos() {
        return productos.todos();
    }

    @Override
    public synchronized Optional<Producto> obtenerProducto(long id) {
        return productos.obtener(id);
    }

    @Override
    public synchronized Producto crearProducto(Producto producto) {
        return productos.insertar(producto);
    }

    @Override
    public synchronized Producto actualizarProducto(Producto producto) {
        return productos.reemplazar(producto);
    }

    @Override
    public synchronized void eliminarProducto(long id) {
        productos.eliminar(id);
    }

    // ============================================================ INVENTARIO

    @Override
    public synchronized List<Inventario> listarMovimientos(Long productoId) {
        return movimientos.todos().stream()
                .filter(m -> productoId == null || productoId.equals(m.productoId()))
                .sorted(Comparator.comparing(Inventario::fecha).thenComparing(Inventario::id).reversed())
                .toList();
    }

    @Override
    public synchronized Inventario crearMovimiento(Inventario movimiento) {
        return movimientos.insertar(movimiento);
    }

    // ============================================================ REPORTES

    private List<OrdenServicio> ordenesEnRango(LocalDate desde, LocalDate hasta) {
        return ordenes.todos().stream().filter(o -> enRango(o.fecha(), desde, hasta)).toList();
    }

    /** Agrupa y ordena de mayor a menor cantidad (y total como desempate). */
    private static <T> List<Conteo> agrupar(Collection<T> items, Function<T, String> etiqueta, Function<T, Long> monto) {
        Map<String, long[]> acum = new LinkedHashMap<>();
        for (T item : items) {
            long[] a = acum.computeIfAbsent(etiqueta.apply(item), k -> new long[2]);
            a[0]++;
            a[1] += monto.apply(item);
        }
        return acum.entrySet().stream()
                .map(e -> new Conteo(e.getKey(), e.getValue()[0], e.getValue()[1]))
                .sorted(Comparator.comparingLong(Conteo::cantidad).thenComparingLong(Conteo::total).reversed())
                .toList();
    }

    @Override
    public synchronized long contarVehiculosAtendidos(LocalDate desde, LocalDate hasta) {
        return ordenesEnRango(desde, hasta).stream().map(OrdenServicio::vehiculoId).distinct().count();
    }

    @Override
    public synchronized long sumarIngresos(LocalDate desde, LocalDate hasta) {
        return listarPagos(desde, hasta).stream().mapToLong(Pago::monto).sum();
    }

    @Override
    public synchronized List<Conteo> serviciosMasSolicitados(LocalDate desde, LocalDate hasta) {
        List<DetalleOrden> detalles = ordenesEnRango(desde, hasta).stream()
                .flatMap(o -> listarDetallesOrden(o.id()).stream()).toList();
        return agrupar(detalles,
                d -> servicios.obtener(d.servicioId()).map(Servicio::nombre).orElse("Servicio #" + d.servicioId()),
                DetalleOrden::precio);
    }

    @Override
    public synchronized List<Conteo> clientesFrecuentes(LocalDate desde, LocalDate hasta, int limite) {
        return agrupar(ordenesEnRango(desde, hasta),
                o -> clientes.obtener(o.clienteId()).map(Cliente::nombre).orElse("Cliente #" + o.clienteId()),
                OrdenServicio::total).stream().limit(limite).toList();
    }

    @Override
    public synchronized List<Conteo> serviciosPorOperario(LocalDate desde, LocalDate hasta) {
        List<OrdenServicio> realizadas = ordenesEnRango(desde, hasta).stream()
                .filter(o -> o.estado() == EstadoOrden.LISTO || o.estado() == EstadoOrden.FINALIZADO)
                .toList();
        return agrupar(realizadas,
                o -> empleados.obtener(o.empleadoId()).map(Empleado::nombre).orElse("Empleado #" + o.empleadoId()),
                OrdenServicio::total);
    }

    @Override
    public synchronized List<Producto> productosStockBajo() {
        return productos.todos().stream().filter(Producto::stockBajo)
                .sorted(Comparator.comparingInt(Producto::stock)).toList();
    }

    @Override
    public synchronized List<ClienteInactivo> clientesSinVisitasDesde(LocalDate fechaCorte) {
        Map<Long, LocalDateTime> ultima = new HashMap<>();
        for (OrdenServicio o : ordenes.todos()) {
            ultima.merge(o.clienteId(), o.fecha(), (a, b) -> a.isAfter(b) ? a : b);
        }
        return clientes.todos().stream()
                .filter(c -> ultima.containsKey(c.id()) && ultima.get(c.id()).toLocalDate().isBefore(fechaCorte))
                .map(c -> new ClienteInactivo(c.id(), c.nombre(), c.telefono(), ultima.get(c.id())))
                .sorted(Comparator.comparing(ClienteInactivo::ultimaVisita))
                .toList();
    }

    @Override
    public synchronized List<VentaDia> ventasPorDia(LocalDate desde, LocalDate hasta) {
        Map<LocalDate, long[]> porDia = new TreeMap<>();
        for (Pago p : listarPagos(desde, hasta)) {
            long[] a = porDia.computeIfAbsent(p.fecha().toLocalDate(), k -> new long[2]);
            a[0] += p.monto();
            a[1]++;
        }
        return porDia.entrySet().stream()
                .map(e -> new VentaDia(e.getKey(), e.getValue()[0], e.getValue()[1])).toList();
    }

    @Override
    public synchronized List<Conteo> ventasPorMetodo(LocalDate desde, LocalDate hasta) {
        return agrupar(listarPagos(desde, hasta), p -> p.metodo().name(), Pago::monto);
    }

    // ============================================================ DATOS SEMILLA

    private void cargarSemilla() {
        LocalDate hoy = LocalDate.now();

        // --- Empleados (un usuario por rol + un segundo operario)
        empleados.insertar(new Empleado(null, "52111222", "Martha Melo", "3105550101", "admin",
                Contrasenas.hash("admin123"), Rol.ADMIN, true));                                   // 1
        empleados.insertar(new Empleado(null, "52333444", "Ana Ruiz", "3105550102", "recepcion",
                Contrasenas.hash("recepcion123"), Rol.RECEPCION, true));                           // 2
        empleados.insertar(new Empleado(null, "80555666", "Carlos M.", "3105550103", "operario",
                Contrasenas.hash("operario123"), Rol.OPERARIO, true));                             // 3
        empleados.insertar(new Empleado(null, "80777888", "Pedro R.", "3105550104", "operario2",
                Contrasenas.hash("operario123"), Rol.OPERARIO, true));                             // 4

        // --- Servicios (precio moto / automóvil / camioneta)
        servicio("Lavado Básico", "Lavado exterior con champú y secado", 30, 10_000, 15_000, 20_000);        // 1
        servicio("Lavado Completo + Cera", "Lavado exterior e interior con encerado", 60, 20_000, 30_000, 38_000); // 2
        servicio("Lavado de Motor", "Desengrase y limpieza del motor", 45, 15_000, 25_000, 30_000);           // 3
        servicio("Aspirado Interior", "Aspirado de sillas, tapetes y baúl", 20, 8_000, 12_000, 15_000);       // 4
        servicio("Polichado", "Pulido de pintura y protección", 120, 40_000, 70_000, 90_000);                // 5
        servicio("Lavado de Tapicería", "Limpieza profunda de sillas y techo", 180, 30_000, 80_000, 100_000); // 6

        // --- Clientes
        String[][] cs = {
                {"1012345678", "Juan Pérez", "3001234567", "juan.perez@correo.com", "Calle 45 # 12-30"},
                {"1023456789", "María Rodríguez", "3012345678", "maria.rodriguez@correo.com", "Carrera 7 # 80-15"},
                {"1034567890", "Andrés López", "3023456789", "andres.lopez@correo.com", "Calle 100 # 15-20"},
                {"1045678901", "Camila Torres", "3034567890", "camila.torres@correo.com", "Avenida 68 # 22-10"},
                {"1056789012", "Diego Ramírez", "3045678901", "diego.ramirez@correo.com", "Calle 26 # 50-40"},
                {"1067890123", "Valentina Castro", "3056789012", "valentina.castro@correo.com", "Carrera 15 # 93-60"},
                {"1078901234", "Santiago Moreno", "3067890123", "santiago.moreno@correo.com", "Calle 53 # 24-11"},
                {"1089012345", "Laura Herrera", "3078901234", "laura.herrera@correo.com", "Carrera 50 # 10-05"},
                {"1090123456", "Felipe Vargas", "3089012345", "felipe.vargas@correo.com", "Calle 13 # 68-90"},
                {"1101234567", "Sofía Jiménez", "3090123456", "sofia.jimenez@correo.com", "Carrera 30 # 45-12"},
        };
        for (int i = 0; i < cs.length; i++) {
            clientes.insertar(new Cliente(null, cs[i][0], cs[i][1], cs[i][2], cs[i][3], cs[i][4],
                    hoy.minusDays(150 - i * 5L)));
        }

        // --- Vehículos: placa, marca, modelo, color, categoría, clienteId
        Object[][] vs = {
                {"ABC123", "Renault", "Logan", "Gris", Categoria.AUTOMOVIL, 1L},    // 1 (caso práctico)
                {"DEF456", "Chevrolet", "Spark", "Rojo", Categoria.AUTOMOVIL, 2L},  // 2
                {"GHI789", "Mazda", "CX-5", "Blanco", Categoria.CAMIONETA, 2L},     // 3
                {"JKL012", "Kia", "Picanto", "Azul", Categoria.AUTOMOVIL, 3L},      // 4
                {"MNO34E", "Honda", "CB190", "Negro", Categoria.MOTO, 3L},          // 5
                {"PQR567", "Toyota", "Hilux", "Plata", Categoria.CAMIONETA, 4L},    // 6
                {"STU890", "Renault", "Duster", "Naranja", Categoria.CAMIONETA, 5L},// 7
                {"VWX12F", "Bajaj", "Pulsar NS", "Azul", Categoria.MOTO, 5L},       // 8
                {"YZA345", "Chevrolet", "Onix", "Blanco", Categoria.AUTOMOVIL, 6L}, // 9
                {"BCD678", "Nissan", "Versa", "Gris", Categoria.AUTOMOVIL, 7L},     // 10
                {"EFG90H", "AKT", "NKD 125", "Rojo", Categoria.MOTO, 7L},           // 11
                {"HIJ234", "Ford", "Ranger", "Negro", Categoria.CAMIONETA, 8L},     // 12
                {"KLM567", "Hyundai", "Accent", "Vinotinto", Categoria.AUTOMOVIL, 9L}, // 13
                {"NOP890", "Suzuki", "Swift", "Amarillo", Categoria.AUTOMOVIL, 10L},// 14
                {"QRS123", "Volkswagen", "Tiguan", "Azul", Categoria.CAMIONETA, 6L},// 15
        };
        for (Object[] v : vs) {
            vehiculos.insertar(new Vehiculo(null, (String) v[0], (String) v[1], (String) v[2], (String) v[3],
                    (Categoria) v[4], (Long) v[5]));
        }

        // --- Caso práctico: Juan Pérez / ABC123, 17 visitas de "Lavado Completo + Cera" ($30.000) con Carlos M.
        MetodoPago[] rotacion = {MetodoPago.EFECTIVO, MetodoPago.TARJETA, MetodoPago.NEQUI};
        for (int k = 0; k < 17; k++) {
            LocalDateTime fecha = hoy.minusDays(3 + 7L * (16 - k)).atTime(9 + k % 3, 0);
            MetodoPago metodo = (k == 16) ? MetodoPago.NEQUI : rotacion[k % 3];
            ordenSemilla(1, 3, fecha, EstadoOrden.FINALIZADO, metodo, 2);
        }

        // --- Órdenes de las últimas 4 semanas (determinísticas)
        Random rnd = new Random(7);
        long[] vehiculosActivos = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 15};
        long[] serviciosPeso = {1, 1, 1, 2, 2, 2, 3, 4, 5, 6};
        for (int i = 0; i < 26; i++) {
            long vehiculoId = vehiculosActivos[rnd.nextInt(vehiculosActivos.length)];
            long operario = rnd.nextBoolean() ? 3 : 4;
            LocalDateTime fecha = hoy.minusDays(1 + rnd.nextInt(27)).atTime(8 + rnd.nextInt(9), rnd.nextInt(4) * 15);
            long principal = serviciosPeso[rnd.nextInt(serviciosPeso.length)];
            MetodoPago metodo = MetodoPago.values()[rnd.nextInt(3)];
            if (principal != 4 && rnd.nextInt(10) < 3) {
                ordenSemilla(vehiculoId, operario, fecha, EstadoOrden.FINALIZADO, metodo, principal, 4);
            } else {
                ordenSemilla(vehiculoId, operario, fecha, EstadoOrden.FINALIZADO, metodo, principal);
            }
        }

        // --- Órdenes de hoy en distintos estados (para dashboards y para probar el flujo)
        ordenSemilla(9, 4, hoy.atTime(8, 0), EstadoOrden.FINALIZADO, MetodoPago.EFECTIVO, 1, 4);
        ordenSemilla(7, 3, hoy.atTime(8, 30), EstadoOrden.LISTO, null, 2);
        ordenSemilla(5, 3, hoy.atTime(9, 0), EstadoOrden.EN_PROCESO, null, 1);
        ordenSemilla(10, 4, hoy.atTime(9, 30), EstadoOrden.EN_ESPERA, null, 2);

        // --- Clientes que no han regresado en más de 60 días
        ordenSemilla(12, 4, hoy.minusDays(72).atTime(11, 0), EstadoOrden.FINALIZADO, MetodoPago.TARJETA, 5);
        ordenSemilla(13, 3, hoy.minusDays(100).atTime(10, 0), EstadoOrden.FINALIZADO, MetodoPago.EFECTIVO, 1);
        ordenSemilla(13, 3, hoy.minusDays(85).atTime(15, 0), EstadoOrden.FINALIZADO, MetodoPago.NEQUI, 2);
        ordenSemilla(14, 4, hoy.minusDays(95).atTime(14, 30), EstadoOrden.FINALIZADO, MetodoPago.EFECTIVO, 3);

        // --- Inventario: productos (stock final deseado, stock mínimo). Cera y Desengrasante quedan en stock bajo.
        productos.insertar(new Producto(null, "Champú para autos", "galón", 0, 5));          // 1
        productos.insertar(new Producto(null, "Cera líquida", "litro", 0, 4));               // 2
        productos.insertar(new Producto(null, "Silicona para llantas", "litro", 0, 3));      // 3
        productos.insertar(new Producto(null, "Desengrasante de motor", "galón", 0, 3));     // 4
        productos.insertar(new Producto(null, "Paños de microfibra", "unidad", 0, 10));      // 5
        productos.insertar(new Producto(null, "Ambientador", "unidad", 0, 5));               // 6
        productos.insertar(new Producto(null, "Limpiador de tapicería", "litro", 0, 2));     // 7
        productos.insertar(new Producto(null, "Abrillantador de vidrios", "litro", 0, 3));   // 8
        int[] stockFinal = {0, 12, 3, 10, 2, 25, 15, 6, 8}; // índice = productoId

        proveedores.insertar(new Proveedor(null, "900123456-1", "Químicos del Norte S.A.S.", "6015551234",
                "ventas@quimicosnorte.com", "Calle 170 # 20-15"));
        proveedores.insertar(new Proveedor(null, "901654321-7", "Distribuidora AutoBrillo", "6015555678",
                "pedidos@autobrillo.com", "Autopista Sur # 60-40"));

        Map<Long, Integer> entradas = new HashMap<>();
        compraSemilla(1, hoy.minusDays(40).atTime(10, 0), entradas, new long[][]{{1, 6, 45_000}, {2, 4, 38_000}, {4, 3, 52_000}});
        compraSemilla(2, hoy.minusDays(20).atTime(11, 0), entradas, new long[][]{{5, 20, 4_500}, {6, 12, 6_000}, {8, 6, 15_000}});
        compraSemilla(1, hoy.minusDays(8).atTime(9, 30), entradas, new long[][]{{1, 4, 45_000}, {3, 5, 28_000}, {7, 4, 35_000}});

        // Inventario inicial + salidas por uso, cuadrando con el stock final deseado
        for (long pid = 1; pid <= 8; pid++) {
            int entro = entradas.getOrDefault(pid, 0);
            int salida = Math.max(0, 5 + entro - stockFinal[(int) pid]);
            int inicial = stockFinal[(int) pid] - entro + salida;
            movimientos.insertar(new Inventario(null, pid, hoy.minusDays(60).atTime(8, 0), TipoMovimiento.AJUSTE,
                    inicial, "Inventario inicial", null));
            if (salida > 0) {
                movimientos.insertar(new Inventario(null, pid, hoy.minusDays(2).atTime(17, 0), TipoMovimiento.SALIDA,
                        -salida, "Uso en servicios", null));
            }
            Producto p = productos.obtener(pid).orElseThrow();
            productos.reemplazar(p.conStock(stockFinal[(int) pid]));
        }
    }

    private void servicio(String nombre, String descripcion, int minutos, long moto, long auto, long camioneta) {
        Map<Categoria, Long> precios = new EnumMap<>(Categoria.class);
        precios.put(Categoria.MOTO, moto);
        precios.put(Categoria.AUTOMOVIL, auto);
        precios.put(Categoria.CAMIONETA, camioneta);
        servicios.insertar(new Servicio(null, nombre, descripcion, minutos, precios, true));
    }

    /** Crea orden + detalles (+ pago si está FINALIZADO) con precios según la categoría del vehículo. */
    private void ordenSemilla(long vehiculoId, long empleadoId, LocalDateTime fecha, EstadoOrden estado,
                              MetodoPago metodo, long... servicioIds) {
        Vehiculo v = vehiculos.obtener(vehiculoId).orElseThrow();
        long total = 0;
        for (long sid : servicioIds) total += servicios.obtener(sid).orElseThrow().precios().get(v.categoria());
        OrdenServicio o = ordenes.insertar(new OrdenServicio(null, v.clienteId(), vehiculoId, empleadoId, fecha,
                estado, total, null));
        for (long sid : servicioIds) {
            detallesOrden.insertar(new DetalleOrden(null, o.id(), sid,
                    servicios.obtener(sid).orElseThrow().precios().get(v.categoria())));
        }
        if (estado == EstadoOrden.FINALIZADO) {
            pagos.insertar(new Pago(null, o.id(), fecha.plusMinutes(70), total, metodo));
        }
    }

    /** items: {productoId, cantidad, costoUnitario} */
    private void compraSemilla(long proveedorId, LocalDateTime fecha, Map<Long, Integer> entradas, long[][] items) {
        long total = 0;
        for (long[] it : items) total += it[1] * it[2];
        Compra c = compras.insertar(new Compra(null, proveedorId, fecha, total));
        for (long[] it : items) {
            detallesCompra.insertar(new DetalleCompra(null, c.id(), it[0], (int) it[1], it[2]));
            movimientos.insertar(new Inventario(null, it[0], fecha, TipoMovimiento.ENTRADA, (int) it[1],
                    "Compra #" + c.id(), c.id()));
            entradas.merge(it[0], (int) it[1], Integer::sum);
        }
    }
}
