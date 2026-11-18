package com.lavamelo;

import com.lavamelo.model.Modelos.*;
import com.lavamelo.repository.MemoryRepositorio;
import com.lavamelo.repository.Repositorio;
import com.lavamelo.service.OrdenService;
import com.lavamelo.service.VehiculoService;
import com.lavamelo.service.Dtos.HistorialVehiculo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Verifica el caso práctico de la presentación en los datos semilla. */
class SemillaTest {

    @Test
    void casoJuanPerezABC123() {
        Repositorio repo = new MemoryRepositorio();
        OrdenService ordenes = new OrdenService(repo);
        VehiculoService vehiculos = new VehiculoService(repo, ordenes);

        Vehiculo abc = repo.buscarVehiculoPorPlaca("ABC123").orElseThrow();
        HistorialVehiculo h = vehiculos.historial(abc.id());

        assertEquals("Juan Pérez", h.cliente().nombre());
        assertEquals(17, h.visitas(), "El caso práctico debe tener 17 visitas");
        assertEquals(510_000, h.totalAcumulado(), "17 × $30.000 = $510.000");
        assertEquals(EstadoOrden.FINALIZADO, h.ordenes().get(0).estado(), "La última orden debe estar Finalizada");
        assertEquals(MetodoPago.NEQUI, h.ordenes().get(0).pago().metodo(), "El último pago fue por Nequi");
    }

    @Test
    void hayDosProductosEnStockBajo() {
        Repositorio repo = new MemoryRepositorio();
        assertEquals(2, repo.productosStockBajo().size());
    }

    @Test
    void reportesTienenDatos() {
        Repositorio repo = new MemoryRepositorio();
        var hoy = java.time.LocalDate.now();
        assertFalse(repo.serviciosMasSolicitados(hoy.minusMonths(6), hoy).isEmpty());
        assertFalse(repo.clientesSinVisitasDesde(hoy.minusDays(60)).isEmpty(), "Debe haber clientes inactivos");
        assertTrue(repo.sumarIngresos(hoy.minusMonths(6), hoy) > 0);
    }
}
