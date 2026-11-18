package com.lavamelo;

import com.lavamelo.model.Modelos.*;
import com.lavamelo.service.ApiException;
import com.lavamelo.service.OrdenService;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Reglas puras: transiciones de estado y cálculo del total por categoría. */
class OrdenReglasTest {

    private Servicio servicio(long moto, long auto, long camioneta) {
        Map<Categoria, Long> p = new EnumMap<>(Categoria.class);
        p.put(Categoria.MOTO, moto);
        p.put(Categoria.AUTOMOVIL, auto);
        p.put(Categoria.CAMIONETA, camioneta);
        return new Servicio(1L, "S", "", 30, p, true);
    }

    @Test
    void totalSegunCategoriaDelVehiculo() {
        Servicio lavado = servicio(20_000, 30_000, 38_000);
        Servicio motor = servicio(15_000, 25_000, 30_000);
        assertEquals(30_000, OrdenService.calcularTotal(Categoria.AUTOMOVIL, List.of(lavado)));
        assertEquals(68_000, OrdenService.calcularTotal(Categoria.CAMIONETA, List.of(lavado, motor)));
        assertEquals(35_000, OrdenService.calcularTotal(Categoria.MOTO, List.of(lavado, motor)));
    }

    @Test
    void casoJuanPerez17Visitas() {
        Servicio lavadoCera = servicio(20_000, 30_000, 38_000);
        long unaVisita = OrdenService.calcularTotal(Categoria.AUTOMOVIL, List.of(lavadoCera));
        assertEquals(30_000, unaVisita);
        assertEquals(510_000, unaVisita * 17);
    }

    @Test
    void transicionesSoloAvanzanUnPaso() {
        assertDoesNotThrow(() -> OrdenService.validarTransicion(EstadoOrden.EN_ESPERA, EstadoOrden.EN_PROCESO));
        assertDoesNotThrow(() -> OrdenService.validarTransicion(EstadoOrden.EN_PROCESO, EstadoOrden.LISTO));
        assertDoesNotThrow(() -> OrdenService.validarTransicion(EstadoOrden.LISTO, EstadoOrden.FINALIZADO));
    }

    @Test
    void transicionesInvalidasFallan() {
        // Saltar pasos
        assertThrows(ApiException.class, () -> OrdenService.validarTransicion(EstadoOrden.EN_ESPERA, EstadoOrden.LISTO));
        // Retroceder
        assertThrows(ApiException.class, () -> OrdenService.validarTransicion(EstadoOrden.LISTO, EstadoOrden.EN_PROCESO));
        // Después de finalizado no hay más
        assertThrows(ApiException.class, () -> OrdenService.validarTransicion(EstadoOrden.FINALIZADO, EstadoOrden.FINALIZADO));
    }

    @Test
    void servicioSinPrecioParaLaCategoriaFalla() {
        Map<Categoria, Long> p = new EnumMap<>(Categoria.class);
        p.put(Categoria.AUTOMOVIL, 30_000L);
        Servicio soloAuto = new Servicio(1L, "S", "", 30, p, true);
        assertThrows(ApiException.class, () -> OrdenService.calcularTotal(Categoria.MOTO, List.of(soloAuto)));
    }
}
