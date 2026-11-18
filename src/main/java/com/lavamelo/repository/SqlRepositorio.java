package com.lavamelo.repository;

import com.lavamelo.model.Modelos.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ESQUELETO para la persona de base de datos. Se activa con REPO=sql.
 * <p>
 * Pasos sugeridos:
 * <ol>
 *   <li>Agregar al pom.xml: spring-boot-starter-jdbc y el driver (ej. org.postgresql:postgresql o com.mysql:mysql-connector-j).</li>
 *   <li>Configurar spring.datasource.url / username / password (variables de entorno o application.properties).</li>
 *   <li>Recibir un JdbcTemplate en el constructor (ver RepositorioConfig) e implementar cada método.</li>
 * </ol>
 * No hace falta tocar servicios, controladores ni frontend. El contrato está en docs/CONTRATO_BD.md
 * y la referencia de comportamiento en MemoryRepositorio.
 */
public class SqlRepositorio implements Repositorio {

    private static UnsupportedOperationException pendiente() {
        return new UnsupportedOperationException("SqlRepositorio: método pendiente de implementar");
    }

    // ------------------------------------------------------------ CLIENTE
    @Override public List<Cliente> listarClientes() { throw pendiente(); }
    // SELECT * FROM cliente WHERE LOWER(nombre) LIKE ? OR documento LIKE ? OR telefono LIKE ?
    @Override public List<Cliente> buscarClientes(String texto) { throw pendiente(); }
    @Override public Optional<Cliente> obtenerCliente(long id) { throw pendiente(); }
    @Override public Optional<Cliente> buscarClientePorDocumento(String documento) { throw pendiente(); }
    @Override public Cliente crearCliente(Cliente cliente) { throw pendiente(); }
    @Override public Cliente actualizarCliente(Cliente cliente) { throw pendiente(); }
    @Override public void eliminarCliente(long id) { throw pendiente(); }

    // ------------------------------------------------------------ VEHICULO
    @Override public List<Vehiculo> listarVehiculos() { throw pendiente(); }
    @Override public List<Vehiculo> listarVehiculosPorCliente(long clienteId) { throw pendiente(); }
    @Override public Optional<Vehiculo> obtenerVehiculo(long id) { throw pendiente(); }
    @Override public Optional<Vehiculo> buscarVehiculoPorPlaca(String placa) { throw pendiente(); }
    @Override public Vehiculo crearVehiculo(Vehiculo vehiculo) { throw pendiente(); }
    @Override public Vehiculo actualizarVehiculo(Vehiculo vehiculo) { throw pendiente(); }
    @Override public void eliminarVehiculo(long id) { throw pendiente(); }

    // ------------------------------------------------------------ SERVICIO (+ SERVICIO_PRECIO por categoría)
    @Override public List<Servicio> listarServicios() { throw pendiente(); }
    @Override public Optional<Servicio> obtenerServicio(long id) { throw pendiente(); }
    @Override public Servicio crearServicio(Servicio servicio) { throw pendiente(); }
    @Override public Servicio actualizarServicio(Servicio servicio) { throw pendiente(); }
    @Override public void eliminarServicio(long id) { throw pendiente(); }

    // ------------------------------------------------------------ EMPLEADO
    @Override public List<Empleado> listarEmpleados() { throw pendiente(); }
    @Override public Optional<Empleado> obtenerEmpleado(long id) { throw pendiente(); }
    @Override public Optional<Empleado> buscarEmpleadoPorUsuario(String usuario) { throw pendiente(); }
    @Override public Empleado crearEmpleado(Empleado empleado) { throw pendiente(); }
    @Override public Empleado actualizarEmpleado(Empleado empleado) { throw pendiente(); }
    @Override public void eliminarEmpleado(long id) { throw pendiente(); }

    // ------------------------------------------------------------ ORDEN_SERVICIO / DETALLE_ORDEN
    // Filtro: agregar cada condición solo si el campo no es null; ORDER BY fecha DESC, id DESC
    @Override public List<OrdenServicio> listarOrdenes(FiltroOrdenes filtro) { throw pendiente(); }
    @Override public Optional<OrdenServicio> obtenerOrden(long id) { throw pendiente(); }
    @Override public OrdenServicio crearOrden(OrdenServicio orden) { throw pendiente(); }
    @Override public OrdenServicio actualizarOrden(OrdenServicio orden) { throw pendiente(); }
    @Override public List<DetalleOrden> listarDetallesOrden(long ordenId) { throw pendiente(); }
    @Override public DetalleOrden crearDetalleOrden(DetalleOrden detalle) { throw pendiente(); }

    // ------------------------------------------------------------ PAGO (UNIQUE(orden_id))
    @Override public List<Pago> listarPagos(LocalDate desde, LocalDate hasta) { throw pendiente(); }
    @Override public Optional<Pago> obtenerPagoPorOrden(long ordenId) { throw pendiente(); }
    @Override public Pago crearPago(Pago pago) { throw pendiente(); }

    // ------------------------------------------------------------ PROVEEDOR
    @Override public List<Proveedor> listarProveedores() { throw pendiente(); }
    @Override public Optional<Proveedor> obtenerProveedor(long id) { throw pendiente(); }
    @Override public Proveedor crearProveedor(Proveedor proveedor) { throw pendiente(); }
    @Override public Proveedor actualizarProveedor(Proveedor proveedor) { throw pendiente(); }
    @Override public void eliminarProveedor(long id) { throw pendiente(); }

    // ------------------------------------------------------------ COMPRA / DETALLE_COMPRA
    @Override public List<Compra> listarCompras() { throw pendiente(); }
    @Override public List<Compra> listarComprasPorProveedor(long proveedorId) { throw pendiente(); }
    @Override public Optional<Compra> obtenerCompra(long id) { throw pendiente(); }
    @Override public Compra crearCompra(Compra compra) { throw pendiente(); }
    @Override public List<DetalleCompra> listarDetallesCompra(long compraId) { throw pendiente(); }
    @Override public DetalleCompra crearDetalleCompra(DetalleCompra detalle) { throw pendiente(); }

    // ------------------------------------------------------------ PRODUCTO
    @Override public List<Producto> listarProductos() { throw pendiente(); }
    @Override public Optional<Producto> obtenerProducto(long id) { throw pendiente(); }
    @Override public Producto crearProducto(Producto producto) { throw pendiente(); }
    @Override public Producto actualizarProducto(Producto producto) { throw pendiente(); }
    @Override public void eliminarProducto(long id) { throw pendiente(); }

    // ------------------------------------------------------------ INVENTARIO
    @Override public List<Inventario> listarMovimientos(Long productoId) { throw pendiente(); }
    @Override public Inventario crearMovimiento(Inventario movimiento) { throw pendiente(); }

    // ------------------------------------------------------------ REPORTES
    // SELECT COUNT(DISTINCT vehiculo_id) FROM orden_servicio WHERE DATE(fecha) BETWEEN ? AND ?
    @Override public long contarVehiculosAtendidos(LocalDate desde, LocalDate hasta) { throw pendiente(); }
    // SELECT COALESCE(SUM(monto),0) FROM pago WHERE DATE(fecha) BETWEEN ? AND ?
    @Override public long sumarIngresos(LocalDate desde, LocalDate hasta) { throw pendiente(); }
    // SELECT s.nombre, COUNT(*), SUM(d.precio) FROM detalle_orden d JOIN orden_servicio o ... JOIN servicio s ... GROUP BY s.nombre ORDER BY 2 DESC, 3 DESC
    @Override public List<Conteo> serviciosMasSolicitados(LocalDate desde, LocalDate hasta) { throw pendiente(); }
    // SELECT c.nombre, COUNT(*), SUM(o.total) FROM orden_servicio o JOIN cliente c ... GROUP BY c.id, c.nombre ORDER BY 2 DESC, 3 DESC LIMIT ?
    @Override public List<Conteo> clientesFrecuentes(LocalDate desde, LocalDate hasta, int limite) { throw pendiente(); }
    // ... WHERE o.estado IN ('LISTO','FINALIZADO') GROUP BY empleado
    @Override public List<Conteo> serviciosPorOperario(LocalDate desde, LocalDate hasta) { throw pendiente(); }
    // SELECT * FROM producto WHERE stock <= stock_minimo ORDER BY stock
    @Override public List<Producto> productosStockBajo() { throw pendiente(); }
    // SELECT c.id, c.nombre, c.telefono, MAX(o.fecha) FROM cliente c JOIN orden_servicio o ... GROUP BY ... HAVING MAX(o.fecha) < ? ORDER BY 4
    @Override public List<ClienteInactivo> clientesSinVisitasDesde(LocalDate fechaCorte) { throw pendiente(); }
    // SELECT DATE(fecha), SUM(monto), COUNT(*) FROM pago WHERE ... GROUP BY DATE(fecha) ORDER BY 1
    @Override public List<VentaDia> ventasPorDia(LocalDate desde, LocalDate hasta) { throw pendiente(); }
    // SELECT metodo, COUNT(*), SUM(monto) FROM pago WHERE ... GROUP BY metodo
    @Override public List<Conteo> ventasPorMetodo(LocalDate desde, LocalDate hasta) { throw pendiente(); }
}
