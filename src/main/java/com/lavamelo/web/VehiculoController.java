package com.lavamelo.web;

import com.lavamelo.auth.RequiereRol;
import com.lavamelo.service.Dtos.*;
import com.lavamelo.service.VehiculoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.lavamelo.model.Modelos.Rol.ADMIN;
import static com.lavamelo.model.Modelos.Rol.RECEPCION;

@RestController
@RequestMapping("/api/vehiculos")
@RequiereRol({ADMIN, RECEPCION})
public class VehiculoController {

    private final VehiculoService servicio;

    public VehiculoController(VehiculoService servicio) {
        this.servicio = servicio;
    }

    /** q busca por placa, marca, modelo o dueño. */
    @GetMapping
    public List<VehiculoVista> listar(@RequestParam(required = false) String q,
                                      @RequestParam(required = false) Long clienteId) {
        return servicio.listar(q, clienteId);
    }

    @GetMapping("/{id}")
    public VehiculoVista obtener(@PathVariable long id) {
        return servicio.obtener(id);
    }

    @GetMapping("/{id}/historial")
    public HistorialVehiculo historial(@PathVariable long id) {
        return servicio.historial(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehiculoVista crear(@Valid @RequestBody VehiculoEntrada e) {
        return servicio.crear(e);
    }

    @PutMapping("/{id}")
    public VehiculoVista actualizar(@PathVariable long id, @Valid @RequestBody VehiculoEntrada e) {
        return servicio.actualizar(id, e);
    }

    @DeleteMapping("/{id}")
    @RequiereRol(ADMIN)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable long id) {
        servicio.eliminar(id);
    }
}
