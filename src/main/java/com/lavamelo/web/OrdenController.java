package com.lavamelo.web;

import com.lavamelo.auth.RequiereRol;
import com.lavamelo.auth.Sesion;
import com.lavamelo.model.Modelos.EstadoOrden;
import com.lavamelo.service.Dtos.*;
import com.lavamelo.service.OrdenService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static com.lavamelo.model.Modelos.Rol.*;

/** El operario solo ve y actualiza sus propias órdenes (filtrado en OrdenService). */
@RestController
@RequestMapping("/api/ordenes")
@RequiereRol({ADMIN, RECEPCION, OPERARIO})
public class OrdenController {

    private final OrdenService servicio;

    public OrdenController(OrdenService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<OrdenVista> listar(@Parameter(hidden = true) @RequestAttribute(Sesion.ATRIBUTO) Sesion s,
                                   @RequestParam(required = false) EstadoOrden estado,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                   @RequestParam(defaultValue = "false") boolean activas) {
        return servicio.listar(s, estado, desde, hasta, activas);
    }

    @GetMapping("/{id}")
    public OrdenVista obtener(@Parameter(hidden = true) @RequestAttribute(Sesion.ATRIBUTO) Sesion s, @PathVariable long id) {
        return servicio.obtener(s, id);
    }

    @PostMapping
    @RequiereRol({ADMIN, RECEPCION})
    @ResponseStatus(HttpStatus.CREATED)
    public OrdenVista crear(@Valid @RequestBody OrdenEntrada e) {
        return servicio.crear(e);
    }

    /** En proceso / Listo. Recepción no cambia estados. */
    @PatchMapping("/{id}/estado")
    @RequiereRol({ADMIN, OPERARIO})
    public OrdenVista cambiarEstado(@Parameter(hidden = true) @RequestAttribute(Sesion.ATRIBUTO) Sesion s,
                                    @PathVariable long id, @Valid @RequestBody EstadoEntrada e) {
        return servicio.cambiarEstado(s, id, e.estado());
    }

    @PatchMapping("/{id}/operario")
    @RequiereRol({ADMIN, RECEPCION})
    public OrdenVista reasignar(@PathVariable long id, @Valid @RequestBody OperarioEntrada e) {
        return servicio.reasignar(id, e.empleadoId());
    }
}
