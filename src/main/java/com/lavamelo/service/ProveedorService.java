package com.lavamelo.service;

import com.lavamelo.model.Modelos.Proveedor;
import com.lavamelo.repository.Repositorio;
import com.lavamelo.service.Dtos.ProveedorEntrada;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProveedorService {

    private final Repositorio repo;

    public ProveedorService(Repositorio repo) {
        this.repo = repo;
    }

    public List<Proveedor> listar() {
        return repo.listarProveedores();
    }

    public Proveedor obtener(long id) {
        return repo.obtenerProveedor(id).orElseThrow(() -> ApiException.noEncontrado("Proveedor"));
    }

    public Proveedor crear(ProveedorEntrada e) {
        return repo.crearProveedor(new Proveedor(null, e.nit().trim(), e.nombre().trim(), Util.limpio(e.telefono()),
                Util.limpio(e.email()), Util.limpio(e.direccion())));
    }

    public Proveedor actualizar(long id, ProveedorEntrada e) {
        obtener(id);
        return repo.actualizarProveedor(new Proveedor(id, e.nit().trim(), e.nombre().trim(), Util.limpio(e.telefono()),
                Util.limpio(e.email()), Util.limpio(e.direccion())));
    }

    public void eliminar(long id) {
        obtener(id);
        if (!repo.listarComprasPorProveedor(id).isEmpty()) {
            throw ApiException.conflicto("No se puede eliminar: el proveedor tiene compras registradas");
        }
        repo.eliminarProveedor(id);
    }
}
