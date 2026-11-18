package com.lavamelo;

import com.lavamelo.auth.Sesion;
import com.lavamelo.model.Modelos.*;
import com.lavamelo.repository.MemoryRepositorio;
import com.lavamelo.repository.Repositorio;
import com.lavamelo.service.*;
import com.lavamelo.service.Dtos.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Flujo completo sobre el repositorio en memoria: pago único y filtro por operario. */
class FlujoServicioTest {

    private Repositorio repo;
    private OrdenService ordenes;
    private PagoService pagos;
    private Sesion admin;

    @BeforeEach
    void preparar() {
        repo = new MemoryRepositorio(false);
        ordenes = new OrdenService(repo);
        pagos = new PagoService(repo, ordenes);
        admin = new Sesion(1, "Admin", "admin", Rol.ADMIN);

        Empleado carlos = repo.crearEmpleado(new Empleado(null, "1", "Carlos", "3", "carlos", "x", Rol.OPERARIO, true));
        Empleado pedro = repo.crearEmpleado(new Empleado(null, "2", "Pedro", "3", "pedro", "x", Rol.OPERARIO, true));
        Cliente cli = repo.crearCliente(new Cliente(null, "100", "Juan", "300", null, null, java.time.LocalDate.now()));
        repo.crearVehiculo(new Vehiculo(null, "ABC123", "Renault", "Logan", "Gris", Categoria.AUTOMOVIL, cli.id()));
        var precios = new java.util.EnumMap<Categoria, Long>(Categoria.class);
        precios.put(Categoria.MOTO, 20_000L);
        precios.put(Categoria.AUTOMOVIL, 30_000L);
        precios.put(Categoria.CAMIONETA, 38_000L);
        repo.crearServicio(new Servicio(null, "Lavado Completo + Cera", "", 60, precios, true));
        // ids: carlos=1, pedro=2, cliente=1, vehiculo=1, servicio=1
    }

    private OrdenVista crearOrden(long operarioId) {
        return ordenes.crear(new OrdenEntrada(1L, List.of(1L), operarioId, null));
    }

    @Test
    void flujoCompletoHastaFinalizar() {
        OrdenVista o = crearOrden(1);
        assertEquals(EstadoOrden.EN_ESPERA, o.estado());
        assertEquals(30_000, o.total());

        ordenes.cambiarEstado(admin, o.id(), EstadoOrden.EN_PROCESO);
        ordenes.cambiarEstado(admin, o.id(), EstadoOrden.LISTO);
        OrdenVista finalizada = pagos.registrar(new PagoEntrada(o.id(), MetodoPago.NEQUI));

        assertEquals(EstadoOrden.FINALIZADO, finalizada.estado());
        assertNotNull(finalizada.pago());
        assertEquals(30_000, finalizada.pago().monto());
    }

    @Test
    void unaOrdenSePagaUnaSolaVez() {
        OrdenVista o = crearOrden(1);
        ordenes.cambiarEstado(admin, o.id(), EstadoOrden.EN_PROCESO);
        ordenes.cambiarEstado(admin, o.id(), EstadoOrden.LISTO);
        pagos.registrar(new PagoEntrada(o.id(), MetodoPago.EFECTIVO));

        ApiException ex = assertThrows(ApiException.class, () -> pagos.registrar(new PagoEntrada(o.id(), MetodoPago.TARJETA)));
        assertTrue(ex.getMessage().toLowerCase().contains("ya fue pagada"));
    }

    @Test
    void noSePuedeCobrarSiNoEstaLista() {
        OrdenVista o = crearOrden(1);
        assertThrows(ApiException.class, () -> pagos.registrar(new PagoEntrada(o.id(), MetodoPago.EFECTIVO)));
    }

    @Test
    void operarioSoloVeSusOrdenes() {
        crearOrden(1); // Carlos
        crearOrden(2); // Pedro
        Sesion carlos = new Sesion(1, "Carlos", "carlos", Rol.OPERARIO);
        Sesion pedro = new Sesion(2, "Pedro", "pedro", Rol.OPERARIO);

        assertEquals(1, ordenes.listar(carlos, null, null, null, false).size());
        assertEquals(1, ordenes.listar(pedro, null, null, null, false).size());
        assertEquals(2, ordenes.listar(admin, null, null, null, false).size());
    }

    @Test
    void operarioNoAccedeAOrdenAjena() {
        OrdenVista dePedro = crearOrden(2);
        Sesion carlos = new Sesion(1, "Carlos", "carlos", Rol.OPERARIO);
        assertThrows(ApiException.class, () -> ordenes.obtener(carlos, dePedro.id()));
    }
}
