package com.lavamelo.web;

import com.lavamelo.auth.RequiereRol;
import com.lavamelo.auth.Sesion;
import com.lavamelo.model.Modelos.Empleado;
import com.lavamelo.service.Dtos.EmpleadoEntrada;
import com.lavamelo.service.EmpleadoService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.lavamelo.model.Modelos.Rol.ADMIN;
import static com.lavamelo.model.Modelos.Rol.RECEPCION;

@RestController
@RequestMapping("/api/empleados")
@RequiereRol(ADMIN)
public class EmpleadoController {

    private final EmpleadoService servicio;

    public EmpleadoController(EmpleadoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Empleado> listar() {
        return servicio.listar();
    }

    /** Operarios activos, para asignar órdenes. */
    @GetMapping("/operarios")
    @RequiereRol({ADMIN, RECEPCION})
    public List<Empleado> operarios() {
        return servicio.operariosActivos();
    }

    @GetMapping("/{id}")
    public Empleado obtener(@PathVariable long id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Empleado crear(@Valid @RequestBody EmpleadoEntrada e) {
        return servicio.crear(e);
    }

    @PutMapping("/{id}")
    public Empleado actualizar(@Parameter(hidden = true) @RequestAttribute(Sesion.ATRIBUTO) Sesion s,
                               @PathVariable long id, @Valid @RequestBody EmpleadoEntrada e) {
        return servicio.actualizar(s, id, e);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@Parameter(hidden = true) @RequestAttribute(Sesion.ATRIBUTO) Sesion s, @PathVariable long id) {
        servicio.eliminar(s, id);
    }
}
