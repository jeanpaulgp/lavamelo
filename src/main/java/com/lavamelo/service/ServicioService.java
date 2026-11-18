package com.lavamelo.service;

import com.lavamelo.model.Modelos.*;
import com.lavamelo.repository.Repositorio;
import com.lavamelo.service.Dtos.ServicioEntrada;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Catálogo de servicios con precio por categoría de vehículo. */
@Service
public class ServicioService {

    private final Repositorio repo;

    public ServicioService(Repositorio repo) {
        this.repo = repo;
    }

    public List<Servicio> listar(boolean soloActivos) {
        return repo.listarServicios().stream().filter(s -> !soloActivos || s.activo()).toList();
    }

    public Servicio obtener(long id) {
        return repo.obtenerServicio(id).orElseThrow(() -> ApiException.noEncontrado("Servicio"));
    }

    public Servicio crear(ServicioEntrada e) {
        return repo.crearServicio(new Servicio(null, e.nombre().trim(), Util.limpio(e.descripcion()), e.duracionMin(),
                precios(e.precios()), e.activo() == null || e.activo()));
    }

    public Servicio actualizar(long id, ServicioEntrada e) {
        Servicio actual = obtener(id);
        return repo.actualizarServicio(new Servicio(id, e.nombre().trim(), Util.limpio(e.descripcion()), e.duracionMin(),
                precios(e.precios()), e.activo() == null ? actual.activo() : e.activo()));
    }

    /** Baja lógica: el servicio queda inactivo para conservar el historial de órdenes. */
    public Servicio desactivar(long id) {
        Servicio s = obtener(id);
        return repo.actualizarServicio(new Servicio(id, s.nombre(), s.descripcion(), s.duracionMin(), s.precios(), false));
    }

    /** Exige un precio mayor que 0 para cada categoría. */
    private static Map<Categoria, Long> precios(Map<Categoria, Long> entrada) {
        Map<Categoria, Long> precios = new EnumMap<>(Categoria.class);
        for (Categoria c : Categoria.values()) {
            Long p = entrada.get(c);
            if (p == null || p <= 0) throw ApiException.invalido("Indique un precio mayor que 0 para " + c);
            precios.put(c, p);
        }
        return precios;
    }
}
