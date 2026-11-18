package com.lavamelo.service;

import com.lavamelo.model.Modelos.*;
import com.lavamelo.repository.Repositorio;
import com.lavamelo.service.Dtos.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClienteService {

    private final Repositorio repo;
    private final OrdenService ordenes;
    private final VehiculoService vehiculos;

    public ClienteService(Repositorio repo, OrdenService ordenes, VehiculoService vehiculos) {
        this.repo = repo;
        this.ordenes = ordenes;
        this.vehiculos = vehiculos;
    }

    public List<Cliente> listar(String q) {
        String texto = Util.limpio(q);
        return texto == null ? repo.listarClientes() : repo.buscarClientes(texto);
    }

    public Cliente obtener(long id) {
        return repo.obtenerCliente(id).orElseThrow(() -> ApiException.noEncontrado("Cliente"));
    }

    public synchronized Cliente crear(ClienteEntrada e) {
        String documento = e.documento().trim();
        if (repo.buscarClientePorDocumento(documento).isPresent()) {
            throw ApiException.conflicto("Ya existe un cliente con el documento " + documento);
        }
        return repo.crearCliente(new Cliente(null, documento, e.nombre().trim(), e.telefono().trim(),
                Util.limpio(e.email()), Util.limpio(e.direccion()), LocalDate.now()));
    }

    public synchronized Cliente actualizar(long id, ClienteEntrada e) {
        Cliente actual = obtener(id);
        String documento = e.documento().trim();
        repo.buscarClientePorDocumento(documento).filter(c -> !c.id().equals(id)).ifPresent(c -> {
            throw ApiException.conflicto("Ya existe otro cliente con el documento " + documento);
        });
        return repo.actualizarCliente(new Cliente(id, documento, e.nombre().trim(), e.telefono().trim(),
                Util.limpio(e.email()), Util.limpio(e.direccion()), actual.fechaRegistro()));
    }

    public void eliminar(long id) {
        obtener(id);
        if (!repo.listarVehiculosPorCliente(id).isEmpty()) {
            throw ApiException.conflicto("No se puede eliminar: el cliente tiene vehículos registrados");
        }
        repo.eliminarCliente(id);
    }

    /** Historial de visitas: todas las órdenes del cliente y total pagado. */
    public HistorialCliente historial(long id) {
        Cliente c = obtener(id);
        List<OrdenVista> lista = repo.listarOrdenes(new FiltroOrdenes(null, null, id, null, null, null))
                .stream().map(ordenes::vista).toList();
        long total = lista.stream().filter(o -> o.pago() != null).mapToLong(o -> o.pago().monto()).sum();
        LocalDateTime ultima = lista.isEmpty() ? null : lista.get(0).fecha();
        return new HistorialCliente(c, vehiculos.listar(null, id), lista.size(), total, ultima, lista);
    }
}
