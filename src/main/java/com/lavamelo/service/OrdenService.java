package com.lavamelo.service;

import com.lavamelo.auth.Sesion;
import com.lavamelo.model.Modelos.*;
import com.lavamelo.repository.Repositorio;
import com.lavamelo.service.Dtos.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/** Órdenes de servicio: creación, cálculo del total, transiciones de estado y vistas por rol. */
@Service
public class OrdenService {

    private final Repositorio repo;

    public OrdenService(Repositorio repo) {
        this.repo = repo;
    }

    // ------------------------------------------------------------ Reglas de negocio (puras)

    /** Precio de un servicio para la categoría del vehículo. */
    public static long precioPara(Servicio servicio, Categoria categoria) {
        Long precio = servicio.precios() == null ? null : servicio.precios().get(categoria);
        if (precio == null || precio <= 0) {
            throw ApiException.invalido("El servicio \"" + servicio.nombre() + "\" no tiene precio para " + categoria);
        }
        return precio;
    }

    /** Total = suma de los precios de cada servicio según la categoría del vehículo. */
    public static long calcularTotal(Categoria categoria, List<Servicio> servicios) {
        return servicios.stream().mapToLong(s -> precioPara(s, categoria)).sum();
    }

    /** Solo se permite avanzar exactamente un paso: En espera → En proceso → Listo → Finalizado. */
    public static void validarTransicion(EstadoOrden actual, EstadoOrden nuevo) {
        if (actual.siguiente() != nuevo) {
            throw ApiException.invalido("Transición no permitida: " + actual + " → " + nuevo
                    + (actual.siguiente() == null ? " (la orden ya está finalizada)" : ". El siguiente estado es " + actual.siguiente()));
        }
    }

    // ------------------------------------------------------------ Casos de uso

    public OrdenVista crear(OrdenEntrada e) {
        Vehiculo vehiculo = repo.obtenerVehiculo(e.vehiculoId()).orElseThrow(() -> ApiException.noEncontrado("Vehículo"));
        Empleado operario = operarioValido(e.empleadoId());

        List<Servicio> servicios = new ArrayList<>();
        for (Long id : new LinkedHashSet<>(e.servicioIds())) {
            Servicio s = repo.obtenerServicio(id).orElseThrow(() -> ApiException.noEncontrado("Servicio"));
            if (!s.activo()) throw ApiException.invalido("El servicio \"" + s.nombre() + "\" está inactivo");
            servicios.add(s);
        }
        long total = calcularTotal(vehiculo.categoria(), servicios);

        OrdenServicio orden = repo.crearOrden(new OrdenServicio(null, vehiculo.clienteId(), vehiculo.id(), operario.id(),
                LocalDateTime.now(), EstadoOrden.EN_ESPERA, total, Util.limpio(e.observaciones())));
        for (Servicio s : servicios) {
            repo.crearDetalleOrden(new DetalleOrden(null, orden.id(), s.id(), precioPara(s, vehiculo.categoria())));
        }
        return vista(orden);
    }

    /** El operario solo ve sus órdenes. soloActivas excluye las finalizadas. */
    public List<OrdenVista> listar(Sesion sesion, EstadoOrden estado, LocalDate desde, LocalDate hasta, boolean soloActivas) {
        Long empleadoId = sesion.es(Rol.OPERARIO) ? sesion.empleadoId() : null;
        return repo.listarOrdenes(new FiltroOrdenes(estado, empleadoId, null, null, desde, hasta)).stream()
                .filter(o -> !soloActivas || o.estado() != EstadoOrden.FINALIZADO)
                .map(this::vista)
                .toList();
    }

    public OrdenVista obtener(Sesion sesion, long id) {
        return vista(ordenVisible(sesion, id));
    }

    /** Cambio manual de estado (En proceso / Listo). Finalizado solo se asigna al registrar el pago. */
    public synchronized OrdenVista cambiarEstado(Sesion sesion, long id, EstadoOrden nuevo) {
        OrdenServicio orden = ordenVisible(sesion, id);
        if (nuevo == EstadoOrden.FINALIZADO) {
            throw ApiException.invalido("El estado Finalizado se asigna automáticamente al registrar el pago");
        }
        validarTransicion(orden.estado(), nuevo);
        return vista(repo.actualizarOrden(orden.conEstado(nuevo)));
    }

    public OrdenVista reasignar(long id, long empleadoId) {
        OrdenServicio orden = repo.obtenerOrden(id).orElseThrow(() -> ApiException.noEncontrado("Orden"));
        if (orden.estado() != EstadoOrden.EN_ESPERA && orden.estado() != EstadoOrden.EN_PROCESO) {
            throw ApiException.invalido("Solo se puede reasignar una orden En espera o En proceso");
        }
        Empleado operario = operarioValido(empleadoId);
        return vista(repo.actualizarOrden(orden.conEmpleado(operario.id())));
    }

    // ------------------------------------------------------------ Apoyo

    private Empleado operarioValido(long empleadoId) {
        Empleado e = repo.obtenerEmpleado(empleadoId).orElseThrow(() -> ApiException.noEncontrado("Operario"));
        if (e.rol() != Rol.OPERARIO || !e.activo()) {
            throw ApiException.invalido("El empleado asignado debe ser un operario activo");
        }
        return e;
    }

    private OrdenServicio ordenVisible(Sesion sesion, long id) {
        OrdenServicio orden = repo.obtenerOrden(id).orElseThrow(() -> ApiException.noEncontrado("Orden"));
        if (sesion.es(Rol.OPERARIO) && orden.empleadoId() != sesion.empleadoId()) {
            throw ApiException.prohibido("Esta orden no está asignada a usted");
        }
        return orden;
    }

    /** Arma la vista con nombres de cliente, vehículo, operario, servicios y pago. */
    public OrdenVista vista(OrdenServicio o) {
        Cliente c = repo.obtenerCliente(o.clienteId()).orElse(null);
        Vehiculo v = repo.obtenerVehiculo(o.vehiculoId()).orElse(null);
        Empleado e = repo.obtenerEmpleado(o.empleadoId()).orElse(null);
        List<ItemOrden> items = repo.listarDetallesOrden(o.id()).stream().map(d -> {
            Servicio s = repo.obtenerServicio(d.servicioId()).orElse(null);
            return new ItemOrden(d.servicioId(), s == null ? "Servicio #" + d.servicioId() : s.nombre(), d.precio(),
                    s == null ? 0 : s.duracionMin());
        }).toList();
        int duracion = items.stream().mapToInt(ItemOrden::duracionMin).sum();
        return new OrdenVista(o.id(), o.fecha(), o.estado(), o.total(), o.observaciones(),
                o.clienteId(), c == null ? null : c.nombre(), c == null ? null : c.documento(), c == null ? null : c.telefono(),
                o.vehiculoId(), v == null ? null : v.placa(), v == null ? null : v.marca() + " " + v.modelo(),
                v == null ? null : v.categoria(),
                o.empleadoId(), e == null ? null : e.nombre(),
                items, duracion, repo.obtenerPagoPorOrden(o.id()).orElse(null));
    }
}
