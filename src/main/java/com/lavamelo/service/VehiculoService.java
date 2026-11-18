package com.lavamelo.service;

import com.lavamelo.model.Modelos.*;
import com.lavamelo.repository.Repositorio;
import com.lavamelo.service.Dtos.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class VehiculoService {

    private final Repositorio repo;
    private final OrdenService ordenes;

    public VehiculoService(Repositorio repo, OrdenService ordenes) {
        this.repo = repo;
        this.ordenes = ordenes;
    }

    /** q filtra por placa, marca, modelo o nombre del dueño. */
    public List<VehiculoVista> listar(String q, Long clienteId) {
        List<Vehiculo> base = clienteId == null ? repo.listarVehiculos() : repo.listarVehiculosPorCliente(clienteId);
        String t = Util.limpio(q) == null ? null : q.trim().toLowerCase(Locale.ROOT);
        return base.stream().map(this::vista)
                .filter(v -> t == null || (v.placa() + " " + v.marca() + " " + v.modelo() + " " + v.clienteNombre())
                        .toLowerCase(Locale.ROOT).contains(t))
                .toList();
    }

    public Vehiculo obtenerEntidad(long id) {
        return repo.obtenerVehiculo(id).orElseThrow(() -> ApiException.noEncontrado("Vehículo"));
    }

    public VehiculoVista obtener(long id) {
        return vista(obtenerEntidad(id));
    }

    public synchronized VehiculoVista crear(VehiculoEntrada e) {
        String placa = validar(null, e);
        return vista(repo.crearVehiculo(new Vehiculo(null, placa, e.marca().trim(), e.modelo().trim(),
                Util.limpio(e.color()), e.categoria(), e.clienteId())));
    }

    public synchronized VehiculoVista actualizar(long id, VehiculoEntrada e) {
        obtenerEntidad(id);
        String placa = validar(id, e);
        return vista(repo.actualizarVehiculo(new Vehiculo(id, placa, e.marca().trim(), e.modelo().trim(),
                Util.limpio(e.color()), e.categoria(), e.clienteId())));
    }

    public void eliminar(long id) {
        obtenerEntidad(id);
        if (!repo.listarOrdenes(new FiltroOrdenes(null, null, null, id, null, null)).isEmpty()) {
            throw ApiException.conflicto("No se puede eliminar: el vehículo tiene órdenes registradas");
        }
        repo.eliminarVehiculo(id);
    }

    /** Historial automático: número de visitas, último servicio y total acumulado (pagado). */
    public HistorialVehiculo historial(long id) {
        Vehiculo v = obtenerEntidad(id);
        List<OrdenVista> lista = repo.listarOrdenes(new FiltroOrdenes(null, null, null, id, null, null))
                .stream().map(ordenes::vista).toList();
        long total = lista.stream().filter(o -> o.pago() != null).mapToLong(o -> o.pago().monto()).sum();
        OrdenVista ultima = lista.isEmpty() ? null : lista.get(0);
        String ultimoServicio = ultima == null ? null
                : ultima.servicios().stream().map(ItemOrden::nombre).collect(Collectors.joining(", "));
        return new HistorialVehiculo(vista(v), repo.obtenerCliente(v.clienteId()).orElse(null), lista.size(),
                ultimoServicio, ultima == null ? null : ultima.fecha(), total, lista);
    }

    /** Normaliza la placa y valida unicidad y cliente. */
    private String validar(Long id, VehiculoEntrada e) {
        String placa = Util.placa(e.placa());
        repo.buscarVehiculoPorPlaca(placa).filter(v -> !v.id().equals(id)).ifPresent(v -> {
            throw ApiException.conflicto("La placa " + placa + " ya está registrada");
        });
        repo.obtenerCliente(e.clienteId()).orElseThrow(() -> ApiException.noEncontrado("Cliente"));
        return placa;
    }

    private VehiculoVista vista(Vehiculo v) {
        String dueno = repo.obtenerCliente(v.clienteId()).map(Cliente::nombre).orElse(null);
        return new VehiculoVista(v.id(), v.placa(), v.marca(), v.modelo(), v.color(), v.categoria(), v.clienteId(), dueno);
    }
}
