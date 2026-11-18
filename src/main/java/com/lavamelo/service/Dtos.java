package com.lavamelo.service;

import com.lavamelo.model.Modelos.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Datos de entrada (con validación) y vistas de salida de la API. */
public final class Dtos {

    private Dtos() {
    }

    // ================================================================ ENTRADAS

    public record LoginEntrada(@NotBlank(message = "El usuario es obligatorio") String usuario,
                               @NotBlank(message = "La contraseña es obligatoria") String password) {
    }

    public record ClienteEntrada(
            @NotBlank(message = "El documento es obligatorio") @Size(max = 20) String documento,
            @NotBlank(message = "El nombre es obligatorio") @Size(max = 100) String nombre,
            @NotBlank(message = "El teléfono es obligatorio") @Pattern(regexp = "[0-9 +()-]{7,20}", message = "Teléfono inválido") String telefono,
            @Email(message = "Correo inválido") String email,
            @Size(max = 150) String direccion) {
    }

    public record VehiculoEntrada(
            @NotBlank(message = "La placa es obligatoria") @Pattern(regexp = "[A-Za-z0-9 -]{5,8}", message = "Placa inválida (5 a 8 letras/números)") String placa,
            @NotBlank(message = "La marca es obligatoria") String marca,
            @NotBlank(message = "El modelo es obligatorio") String modelo,
            String color,
            @NotNull(message = "La categoría es obligatoria") Categoria categoria,
            @NotNull(message = "El cliente es obligatorio") Long clienteId) {
    }

    public record ServicioEntrada(
            @NotBlank(message = "El nombre es obligatorio") String nombre,
            String descripcion,
            @Min(value = 5, message = "La duración mínima es 5 minutos") int duracionMin,
            @NotNull(message = "Los precios son obligatorios") Map<Categoria, Long> precios,
            Boolean activo) {
    }

    /** password obligatoria al crear; al editar, vacía = no cambiar. */
    public record EmpleadoEntrada(
            @NotBlank(message = "El documento es obligatorio") String documento,
            @NotBlank(message = "El nombre es obligatorio") String nombre,
            String telefono,
            @NotBlank(message = "El usuario es obligatorio") @Pattern(regexp = "[a-zA-Z0-9._-]{3,30}", message = "Usuario inválido (3 a 30 caracteres, sin espacios)") String usuario,
            String password,
            @NotNull(message = "El rol es obligatorio") Rol rol,
            Boolean activo) {
    }

    public record OrdenEntrada(
            @NotNull(message = "El vehículo es obligatorio") Long vehiculoId,
            @NotEmpty(message = "Seleccione al menos un servicio") List<Long> servicioIds,
            @NotNull(message = "Debe asignar un operario") Long empleadoId,
            @Size(max = 300) String observaciones) {
    }

    public record EstadoEntrada(@NotNull(message = "El estado es obligatorio") EstadoOrden estado) {
    }

    public record OperarioEntrada(@NotNull(message = "El operario es obligatorio") Long empleadoId) {
    }

    public record PagoEntrada(@NotNull(message = "La orden es obligatoria") Long ordenId,
                              @NotNull(message = "El método de pago es obligatorio") MetodoPago metodo) {
    }

    public record ProveedorEntrada(
            @NotBlank(message = "El NIT es obligatorio") String nit,
            @NotBlank(message = "El nombre es obligatorio") String nombre,
            String telefono,
            @Email(message = "Correo inválido") String email,
            String direccion) {
    }

    /** stockInicial solo se usa al crear. */
    public record ProductoEntrada(
            @NotBlank(message = "El nombre es obligatorio") String nombre,
            @NotBlank(message = "La unidad es obligatoria") String unidad,
            @Min(value = 0, message = "El stock mínimo no puede ser negativo") int stockMinimo,
            @Min(value = 0, message = "El stock inicial no puede ser negativo") Integer stockInicial) {
    }

    /** SALIDA: cantidad positiva que se descuenta. AJUSTE: cantidad con signo (+ suma, - resta). */
    public record AjusteEntrada(
            @NotNull(message = "El producto es obligatorio") Long productoId,
            @NotNull(message = "El tipo es obligatorio") TipoMovimiento tipo,
            @NotNull(message = "La cantidad es obligatoria") Integer cantidad,
            @NotBlank(message = "El motivo es obligatorio") String motivo) {
    }

    public record ItemCompraEntrada(
            @NotNull(message = "El producto es obligatorio") Long productoId,
            @Min(value = 1, message = "La cantidad debe ser mayor que 0") int cantidad,
            @Min(value = 0, message = "El costo no puede ser negativo") long costoUnitario) {
    }

    public record CompraEntrada(
            @NotNull(message = "El proveedor es obligatorio") Long proveedorId,
            @NotEmpty(message = "Agregue al menos un producto") List<@Valid ItemCompraEntrada> items) {
    }

    // ================================================================ VISTAS

    public record ItemOrden(Long servicioId, String nombre, long precio, int duracionMin) {
    }

    public record OrdenVista(Long id, LocalDateTime fecha, EstadoOrden estado, long total, String observaciones,
                             Long clienteId, String clienteNombre, String clienteDocumento, String clienteTelefono,
                             Long vehiculoId, String placa, String vehiculo, Categoria categoria,
                             Long empleadoId, String operarioNombre,
                             List<ItemOrden> servicios, int duracionTotalMin, Pago pago) {
    }

    public record VehiculoVista(Long id, String placa, String marca, String modelo, String color,
                                Categoria categoria, Long clienteId, String clienteNombre) {
    }

    public record HistorialVehiculo(VehiculoVista vehiculo, Cliente cliente, long visitas, String ultimoServicio,
                                    LocalDateTime ultimaVisita, long totalAcumulado, List<OrdenVista> ordenes) {
    }

    public record HistorialCliente(Cliente cliente, List<VehiculoVista> vehiculos, long visitas, long totalAcumulado,
                                   LocalDateTime ultimaVisita, List<OrdenVista> ordenes) {
    }

    public record PagoVista(Long id, LocalDateTime fecha, long monto, MetodoPago metodo, Long ordenId,
                            String placa, String clienteNombre) {
    }

    public record IngresosDia(LocalDate fecha, long total, long pagos, List<Conteo> porMetodo) {
    }

    public record MovimientoVista(Long id, LocalDateTime fecha, Long productoId, String producto,
                                  TipoMovimiento tipo, int cantidad, String motivo, Long compraId) {
    }

    public record ItemCompraVista(Long productoId, String producto, int cantidad, long costoUnitario, long subtotal) {
    }

    public record CompraVista(Long id, LocalDateTime fecha, Long proveedorId, String proveedorNombre, long total,
                              List<ItemCompraVista> items) {
    }

    public record Reportes(
            LocalDate desde, LocalDate hasta,
            long vehiculosHoy,                              // 1
            LocalDate semanaDesde, long ingresosSemana,     // 2
            Conteo servicioMasSolicitado,                   // 3
            List<Conteo> servicios,
            List<Conteo> clientesFrecuentes,                // 4
            List<Conteo> serviciosPorOperario,              // 5
            List<Producto> insumosPorAgotarse,              // 6
            List<ClienteInactivo> clientesInactivos,        // 7
            String mes, VentaDia mejorDiaMes, List<VentaDia> ventasMes, // 8
            List<Conteo> ventasPorMetodo,
            long ingresosRango) {
    }

    /** Campos null = no aplica para el rol. */
    public record DashboardVista(Rol rol, String nombre, Long vehiculosHoy, Long ingresosHoy, Long pagosHoy,
                                 Map<EstadoOrden, Long> porEstado, List<OrdenVista> ordenes,
                                 List<Producto> stockBajo, Long terminadasHoy) {
    }
}
