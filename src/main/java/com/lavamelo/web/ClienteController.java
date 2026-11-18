package com.lavamelo.web;

import com.lavamelo.auth.RequiereRol;
import com.lavamelo.model.Modelos.Cliente;
import com.lavamelo.service.ClienteService;
import com.lavamelo.service.Dtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.lavamelo.model.Modelos.Rol.ADMIN;
import static com.lavamelo.model.Modelos.Rol.RECEPCION;

@RestController
@RequestMapping("/api/clientes")
@RequiereRol({ADMIN, RECEPCION})
public class ClienteController {

    private final ClienteService servicio;

    public ClienteController(ClienteService servicio) {
        this.servicio = servicio;
    }

    /** q busca por nombre, documento o teléfono. */
    @GetMapping
    public List<Cliente> listar(@RequestParam(required = false) String q) {
        return servicio.listar(q);
    }

    @GetMapping("/{id}")
    public Cliente obtener(@PathVariable long id) {
        return servicio.obtener(id);
    }

    @GetMapping("/{id}/historial")
    public HistorialCliente historial(@PathVariable long id) {
        return servicio.historial(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Cliente crear(@Valid @RequestBody ClienteEntrada e) {
        return servicio.crear(e);
    }

    @PutMapping("/{id}")
    public Cliente actualizar(@PathVariable long id, @Valid @RequestBody ClienteEntrada e) {
        return servicio.actualizar(id, e);
    }

    @DeleteMapping("/{id}")
    @RequiereRol(ADMIN)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable long id) {
        servicio.eliminar(id);
    }
}
