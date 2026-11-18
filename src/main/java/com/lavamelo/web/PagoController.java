package com.lavamelo.web;

import com.lavamelo.auth.RequiereRol;
import com.lavamelo.service.Dtos.*;
import com.lavamelo.service.PagoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static com.lavamelo.model.Modelos.Rol.ADMIN;
import static com.lavamelo.model.Modelos.Rol.RECEPCION;

@RestController
@RequestMapping("/api/pagos")
@RequiereRol({ADMIN, RECEPCION})
public class PagoController {

    private final PagoService servicio;

    public PagoController(PagoService servicio) {
        this.servicio = servicio;
    }

    /** Default: pagos de hoy. */
    @GetMapping
    public List<PagoVista> listar(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDate hoy = LocalDate.now();
        return servicio.listar(desde == null ? hoy : desde, hasta == null ? hoy : hasta);
    }

    /** Registra el pago de una orden Lista y la finaliza. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrdenVista registrar(@Valid @RequestBody PagoEntrada e) {
        return servicio.registrar(e);
    }

    @GetMapping("/ingresos-dia")
    public IngresosDia ingresosDia(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return servicio.ingresosDelDia(fecha == null ? LocalDate.now() : fecha);
    }

    @GetMapping("/recibo/{ordenId}")
    public OrdenVista recibo(@PathVariable long ordenId) {
        return servicio.recibo(ordenId);
    }
}
