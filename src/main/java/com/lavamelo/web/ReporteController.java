package com.lavamelo.web;

import com.lavamelo.auth.RequiereRol;
import com.lavamelo.auth.Sesion;
import com.lavamelo.service.Dtos.*;
import com.lavamelo.service.ReporteService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;

import static com.lavamelo.model.Modelos.Rol.ADMIN;

@RestController
@RequestMapping("/api")
public class ReporteController {

    private final ReporteService servicio;

    public ReporteController(ReporteService servicio) {
        this.servicio = servicio;
    }

    /** Las 8 preguntas del negocio + ventas por método. mes con formato AAAA-MM. */
    @GetMapping("/reportes")
    @RequiereRol(ADMIN)
    public Reportes reportes(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                             @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes) {
        return servicio.reportes(desde, hasta, mes);
    }

    /** Contenido según el rol de la sesión. */
    @GetMapping("/dashboard")
    public DashboardVista dashboard(@Parameter(hidden = true) @RequestAttribute(Sesion.ATRIBUTO) Sesion s) {
        return servicio.dashboard(s);
    }
}
