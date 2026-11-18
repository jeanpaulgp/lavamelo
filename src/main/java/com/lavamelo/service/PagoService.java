package com.lavamelo.service;

import com.lavamelo.model.Modelos.*;
import com.lavamelo.repository.Repositorio;
import com.lavamelo.service.Dtos.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PagoService {

    private final Repositorio repo;
    private final OrdenService ordenes;

    public PagoService(Repositorio repo, OrdenService ordenes) {
        this.repo = repo;
        this.ordenes = ordenes;
    }

    /**
     * Registra el pago de una orden en estado Listo por su total y la pasa a Finalizado.
     * synchronized evita dobles cobros simultáneos (con BD, reforzar con UNIQUE(orden_id)).
     */
    public synchronized OrdenVista registrar(PagoEntrada e) {
        OrdenServicio orden = repo.obtenerOrden(e.ordenId()).orElseThrow(() -> ApiException.noEncontrado("Orden"));
        if (repo.obtenerPagoPorOrden(orden.id()).isPresent()) {
            throw ApiException.conflicto("La orden #" + orden.id() + " ya fue pagada");
        }
        if (orden.estado() != EstadoOrden.LISTO) {
            throw ApiException.invalido("Solo se pueden cobrar órdenes en estado Listo (estado actual: " + orden.estado() + ")");
        }
        OrdenService.validarTransicion(orden.estado(), EstadoOrden.FINALIZADO);
        repo.crearPago(new Pago(null, orden.id(), LocalDateTime.now(), orden.total(), e.metodo()));
        return ordenes.vista(repo.actualizarOrden(orden.conEstado(EstadoOrden.FINALIZADO)));
    }

    public List<PagoVista> listar(LocalDate desde, LocalDate hasta) {
        return repo.listarPagos(desde, hasta).stream().map(p -> {
            OrdenServicio o = repo.obtenerOrden(p.ordenId()).orElse(null);
            String placa = o == null ? null : repo.obtenerVehiculo(o.vehiculoId()).map(Vehiculo::placa).orElse(null);
            String cliente = o == null ? null : repo.obtenerCliente(o.clienteId()).map(Cliente::nombre).orElse(null);
            return new PagoVista(p.id(), p.fecha(), p.monto(), p.metodo(), p.ordenId(), placa, cliente);
        }).toList();
    }

    public IngresosDia ingresosDelDia(LocalDate fecha) {
        return new IngresosDia(fecha, repo.sumarIngresos(fecha, fecha), repo.listarPagos(fecha, fecha).size(),
                completarMetodos(repo.ventasPorMetodo(fecha, fecha)));
    }

    /** Recibo: solo existe para órdenes pagadas. */
    public OrdenVista recibo(long ordenId) {
        OrdenServicio o = repo.obtenerOrden(ordenId).orElseThrow(() -> ApiException.noEncontrado("Orden"));
        if (repo.obtenerPagoPorOrden(ordenId).isEmpty()) {
            throw ApiException.invalido("La orden #" + ordenId + " aún no tiene pago registrado");
        }
        return ordenes.vista(o);
    }

    /** Devuelve los tres métodos en orden fijo, con 0 si no hubo ventas. */
    static List<Conteo> completarMetodos(List<Conteo> filas) {
        Map<String, Conteo> porMetodo = filas.stream().collect(Collectors.toMap(Conteo::etiqueta, Function.identity()));
        return Arrays.stream(MetodoPago.values())
                .map(m -> porMetodo.getOrDefault(m.name(), new Conteo(m.name(), 0, 0))).toList();
    }
}
