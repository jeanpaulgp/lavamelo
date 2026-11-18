package com.lavamelo.web;

import com.lavamelo.auth.RequiereRol;
import com.lavamelo.model.Modelos.Proveedor;
import com.lavamelo.service.Dtos.ProveedorEntrada;
import com.lavamelo.service.ProveedorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.lavamelo.model.Modelos.Rol.ADMIN;

@RestController
@RequestMapping("/api/proveedores")
@RequiereRol(ADMIN)
public class ProveedorController {

    private final ProveedorService servicio;

    public ProveedorController(ProveedorService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Proveedor> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public Proveedor obtener(@PathVariable long id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Proveedor crear(@Valid @RequestBody ProveedorEntrada e) {
        return servicio.crear(e);
    }

    @PutMapping("/{id}")
    public Proveedor actualizar(@PathVariable long id, @Valid @RequestBody ProveedorEntrada e) {
        return servicio.actualizar(id, e);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable long id) {
        servicio.eliminar(id);
    }
}
