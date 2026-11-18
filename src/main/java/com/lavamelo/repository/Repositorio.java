package com.lavamelo.repository;

import com.lavamelo.model.Modelos.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * PUNTO DE CONEXIÓN CON LA BASE DE DATOS.
 * <p>
 * Toda lectura/escritura pasa por aquí. Las reglas de negocio (validaciones, estados,
 * totales, stock) viven en los servicios; este contrato solo guarda y consulta.
 * <ul>
 *   <li>crear*: recibe la entidad con id null y devuelve la entidad con el id asignado.</li>
 *   <li>actualizar*: reemplaza la fila con el mismo id y la devuelve.</li>
 *   <li>Rangos de fechas (desde, hasta): inclusivos, comparando solo la parte de fecha.</li>
 * </ul>
 * Ver docs/CONTRATO_BD.md.
 */
public interface Repositorio {

    // ------------------------------------------------------------ CLIENTE
    List<Cliente> listarClientes();

    /** Coincidencia parcial, sin distinguir mayúsculas, en nombre, documento o teléfono. */
    List<Cliente> buscarClientes(String texto);

    Optional<Cliente> obtenerCliente(long id);

    Optional<Cliente> buscarClientePorDocumento(String documento);

    Cliente crearCliente(Cliente cliente);

    Cliente actualizarCliente(Cliente cliente);

    void eliminarCliente(long id);

    // ------------------------------------------------------------ VEHICULO
    List<Vehiculo> listarVehiculos();

    List<Vehiculo> listarVehiculosPorCliente(long clienteId);

    Optional<Vehiculo> obtenerVehiculo(long id);

    /** Placa exacta (ya normalizada en mayúsculas por el servicio). */
    Optional<Vehiculo> buscarVehiculoPorPlaca(String placa);

    Vehiculo crearVehiculo(Vehiculo vehiculo);

    Vehiculo actualizarVehiculo(Vehiculo vehiculo);

    void eliminarVehiculo(long id);

    // ------------------------------------------------------------ SERVICIO
    List<Servicio> listarServicios();

    Optional<Servicio> obtenerServicio(long id);

    Servicio crearServicio(Servicio servicio);

    Servicio actualizarServicio(Servicio servicio);

    void eliminarServicio(long id);

    // ------------------------------------------------------------ EMPLEADO
    List<Empleado> listarEmpleados();

    Optional<Empleado> obtenerEmpleado(long id);

    Optional<Empleado> buscarEmpleadoPorUsuario(String usuario);

    Empleado crearEmpleado(Empleado empleado);

    Empleado actualizarEmpleado(Empleado empleado);

    void eliminarEmpleado(long id);

    // ------------------------------------------------------------ ORDEN_SERVICIO / DETALLE_ORDEN
    /** Ordenadas de la más reciente a la más antigua. */
    List<OrdenServicio> listarOrdenes(FiltroOrdenes filtro);

    Optional<OrdenServicio> obtenerOrden(long id);

    OrdenServicio crearOrden(OrdenServicio orden);

    OrdenServicio actualizarOrden(OrdenServicio orden);

    List<DetalleOrden> listarDetallesOrden(long ordenId);

    DetalleOrden crearDetalleOrden(DetalleOrden detalle);

    // ------------------------------------------------------------ PAGO
    /** Pagos cuya fecha está en el rango, del más reciente al más antiguo. */
    List<Pago> listarPagos(LocalDate desde, LocalDate hasta);

    Optional<Pago> obtenerPagoPorOrden(long ordenId);

    Pago crearPago(Pago pago);

    // ------------------------------------------------------------ PROVEEDOR
    List<Proveedor> listarProveedores();

    Optional<Proveedor> obtenerProveedor(long id);

    Proveedor crearProveedor(Proveedor proveedor);

    Proveedor actualizarProveedor(Proveedor proveedor);

    void eliminarProveedor(long id);

    // ------------------------------------------------------------ COMPRA / DETALLE_COMPRA
    /** Ordenadas de la más reciente a la más antigua. */
    List<Compra> listarCompras();

    List<Compra> listarComprasPorProveedor(long proveedorId);

    Optional<Compra> obtenerCompra(long id);

    Compra crearCompra(Compra compra);

    List<DetalleCompra> listarDetallesCompra(long compraId);

    DetalleCompra crearDetalleCompra(DetalleCompra detalle);

    // ------------------------------------------------------------ PRODUCTO
    List<Producto> listarProductos();

    Optional<Producto> obtenerProducto(long id);

    Producto crearProducto(Producto producto);

    Producto actualizarProducto(Producto producto);

    void eliminarProducto(long id);

    // ------------------------------------------------------------ INVENTARIO (movimientos)
    /** productoId null = todos. Del más reciente al más antiguo. */
    List<Inventario> listarMovimientos(Long productoId);

    Inventario crearMovimiento(Inventario movimiento);

    // ------------------------------------------------------------ CONSULTAS DE REPORTES
    /** Vehículos distintos con órdenes creadas en el rango (cualquier estado). */
    long contarVehiculosAtendidos(LocalDate desde, LocalDate hasta);

    /** Suma de PAGO.monto con fecha de pago en el rango. */
    long sumarIngresos(LocalDate desde, LocalDate hasta);

    /** Por servicio: veces en DETALLE_ORDEN (órdenes del rango) y suma de precio. Mayor a menor. */
    List<Conteo> serviciosMasSolicitados(LocalDate desde, LocalDate hasta);

    /** Por cliente: número de órdenes en el rango y suma de total. Mayor a menor, máximo 'limite'. */
    List<Conteo> clientesFrecuentes(LocalDate desde, LocalDate hasta, int limite);

    /** Por operario: órdenes en estado LISTO o FINALIZADO del rango y suma de total. Mayor a menor. */
    List<Conteo> serviciosPorOperario(LocalDate desde, LocalDate hasta);

    /** Productos con stock &lt;= stockMinimo, del menor stock al mayor. */
    List<Producto> productosStockBajo();

    /** Clientes con al menos una orden cuya última orden es anterior a fechaCorte. Más antiguos primero. */
    List<ClienteInactivo> clientesSinVisitasDesde(LocalDate fechaCorte);

    /** Suma de pagos agrupada por día (solo días con ventas), en orden cronológico. */
    List<VentaDia> ventasPorDia(LocalDate desde, LocalDate hasta);

    /** Por método de pago: cantidad de pagos y suma de monto en el rango. */
    List<Conteo> ventasPorMetodo(LocalDate desde, LocalDate hasta);
}
