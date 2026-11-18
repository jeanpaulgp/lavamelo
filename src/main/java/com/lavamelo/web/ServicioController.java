package com.lavamelo.web;

import com.lavamelo.auth.RequiereRol;
import com.lavamelo.model.Modelos.Servicio;
import com.lavamelo.service.Dtos.ServicioEntrada;
import com.lavamelo.service.ServicioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.lavamelo.model.Modelos.Rol.ADMIN;
import static com.lavamelo.model.Modelos.Rol.RECEPCION;

@RestController
@RequestMapping("/api/servicios")
@RequiereRol(ADMIN)
public class ServicioController {

    private final ServicioService servicio;

    public ServicioController(ServicioService servicio) {
        this.servicio = servicio;
    }

    /** Recepción lo necesita para armar órdenes. */
    @GetMapping
    @RequiereRol({ADMIN, RECEPCION})
    public List<Servicio> listar(@RequestParam(defaultValue = "false") boolean activos) {
        return servicio.listar(activos);
    }

    @GetMapping("/{id}")
    @RequiereRol({ADMIN, RECEPCION})
    public Servicio obtener(@PathVariable long id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Servicio crear(@Valid @RequestBody ServicioEntrada e) {
        return servicio.crear(e);
    }

    @PutMapping("/{id}")
    public Servicio actualizar(@PathVariable long id, @Valid @RequestBody ServicioEntrada e) {
        return servicio.actualizar(id, e);
    }

    /** Baja lógica (queda inactivo). */
    @DeleteMapping("/{id}")
    public Servicio desactivar(@PathVariable long id) {
        return servicio.desactivar(id);
    }
}
