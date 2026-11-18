package com.lavamelo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entidades del dominio (nombres del modelo de la presentación).
 * Son inmutables: para "actualizar" se crea una copia con los cambios.
 * Todas implementan {@link ConId} para que el repositorio pueda asignar el id.
 */
public final class Modelos {

    private Modelos() {
    }

    /** Contrato mínimo para que un repositorio asigne identificadores. */
    public interface ConId<T> {
        Long id();

        T conId(Long id);
    }

    // ---------------------------------------------------------------- Enumeraciones

    public enum Categoria { MOTO, AUTOMOVIL, CAMIONETA }

    public enum Rol { ADMIN, RECEPCION, OPERARIO }

    public enum MetodoPago { EFECTIVO, NEQUI, TARJETA }

    public enum TipoMovimiento { ENTRADA, SALIDA, AJUSTE }

    /** Estados de la orden. Solo se avanza al siguiente, nunca hacia atrás. */
    public enum EstadoOrden {
        EN_ESPERA, EN_PROCESO, LISTO, FINALIZADO;

        /** Estado siguiente permitido, o null si ya es el último. */
        public EstadoOrden siguiente() {
            int i = ordinal() + 1;
            return i < values().length ? values()[i] : null;
        }
    }

    // ---------------------------------------------------------------- Módulo operativo

    public record Cliente(Long id, String documento, String nombre, String telefono,
                          String email, String direccion, LocalDate fechaRegistro) implements ConId<Cliente> {
        public Cliente conId(Long id) {
            return new Cliente(id, documento, nombre, telefono, email, direccion, fechaRegistro);
        }
    }

    public record Vehiculo(Long id, String placa, String marca, String modelo, String color,
                           Categoria categoria, Long clienteId) implements ConId<Vehiculo> {
        public Vehiculo conId(Long id) {
            return new Vehiculo(id, placa, marca, modelo, color, categoria, clienteId);
        }
    }

    /** Extensión (a confirmar con BD): duración y precio por categoría de vehículo. */
    public record Servicio(Long id, String nombre, String descripcion, int duracionMin,
                           Map<Categoria, Long> precios, boolean activo) implements ConId<Servicio> {
        public Servicio conId(Long id) {
            return new Servicio(id, nombre, descripcion, duracionMin, precios, activo);
        }
    }

    /** Extensión (a confirmar con BD): usuario, hash de contraseña, rol y activo. */
    public record Empleado(Long id, String documento, String nombre, String telefono, String usuario,
                           @JsonIgnore String passwordHash, Rol rol, boolean activo) implements ConId<Empleado> {
        public Empleado conId(Long id) {
            return new Empleado(id, documento, nombre, telefono, usuario, passwordHash, rol, activo);
        }
    }

    public record OrdenServicio(Long id, Long clienteId, Long vehiculoId, Long empleadoId, LocalDateTime fecha,
                                EstadoOrden estado, long total, String observaciones) implements ConId<OrdenServicio> {
        public OrdenServicio conId(Long id) {
            return new OrdenServicio(id, clienteId, vehiculoId, empleadoId, fecha, estado, total, observaciones);
        }

        public OrdenServicio conEstado(EstadoOrden nuevo) {
            return new OrdenServicio(id, clienteId, vehiculoId, empleadoId, fecha, nuevo, total, observaciones);
        }

        public OrdenServicio conEmpleado(Long nuevoEmpleadoId) {
            return new OrdenServicio(id, clienteId, vehiculoId, nuevoEmpleadoId, fecha, estado, total, observaciones);
        }
    }

    /** Precio congelado al momento de crear la orden. */
    public record DetalleOrden(Long id, Long ordenId, Long servicioId, long precio) implements ConId<DetalleOrden> {
        public DetalleOrden conId(Long id) {
            return new DetalleOrden(id, ordenId, servicioId, precio);
        }
    }

    public record Pago(Long id, Long ordenId, LocalDateTime fecha, long monto, MetodoPago metodo) implements ConId<Pago> {
        public Pago conId(Long id) {
            return new Pago(id, ordenId, fecha, monto, metodo);
        }
    }

    // ---------------------------------------------------------------- Módulo inventario

    public record Proveedor(Long id, String nit, String nombre, String telefono, String email,
                            String direccion) implements ConId<Proveedor> {
        public Proveedor conId(Long id) {
            return new Proveedor(id, nit, nombre, telefono, email, direccion);
        }
    }

    public record Compra(Long id, Long proveedorId, LocalDateTime fecha, long total) implements ConId<Compra> {
        public Compra conId(Long id) {
            return new Compra(id, proveedorId, fecha, total);
        }
    }

    public record DetalleCompra(Long id, Long compraId, Long productoId, int cantidad,
                                long costoUnitario) implements ConId<DetalleCompra> {
        public DetalleCompra conId(Long id) {
            return new DetalleCompra(id, compraId, productoId, cantidad, costoUnitario);
        }
    }

    /** stock = existencias actuales; stockMinimo es extensión (a confirmar con BD). */
    public record Producto(Long id, String nombre, String unidad, int stock, int stockMinimo) implements ConId<Producto> {
        public Producto conId(Long id) {
            return new Producto(id, nombre, unidad, stock, stockMinimo);
        }

        public Producto conStock(int nuevoStock) {
            return new Producto(id, nombre, unidad, nuevoStock, stockMinimo);
        }

        public boolean stockBajo() {
            return stock <= stockMinimo;
        }
    }

    /**
     * INVENTARIO = movimiento de existencias (kardex).
     * cantidad con signo: positiva entra, negativa sale. compraId solo para ENTRADA.
     */
    public record Inventario(Long id, Long productoId, LocalDateTime fecha, TipoMovimiento tipo, int cantidad,
                             String motivo, Long compraId) implements ConId<Inventario> {
        public Inventario conId(Long id) {
            return new Inventario(id, productoId, fecha, tipo, cantidad, motivo, compraId);
        }
    }

    // ---------------------------------------------------------------- Resultados de consultas

    /** Filtro de órdenes; cualquier campo null significa "sin filtrar". Fechas inclusivas. */
    public record FiltroOrdenes(EstadoOrden estado, Long empleadoId, Long clienteId, Long vehiculoId,
                                LocalDate desde, LocalDate hasta) {
        public static FiltroOrdenes todas() {
            return new FiltroOrdenes(null, null, null, null, null, null);
        }
    }

    /** Fila genérica de agrupación: etiqueta, cantidad y total en COP. */
    public record Conteo(String etiqueta, long cantidad, long total) {
    }

    public record ClienteInactivo(Long clienteId, String nombre, String telefono, LocalDateTime ultimaVisita) {
    }

    public record VentaDia(LocalDate fecha, long total, long pagos) {
    }
}
