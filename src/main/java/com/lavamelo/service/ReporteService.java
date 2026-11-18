package com.lavamelo.service;

import com.lavamelo.auth.Sesion;
import com.lavamelo.model.Modelos.*;
import com.lavamelo.repository.Repositorio;
import com.lavamelo.service.Dtos.*;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Reportes del negocio (8 preguntas) y dashboards por rol. */
@Service
public class ReporteService {

    private final Repositorio repo;
    private final OrdenService ordenes;

    public ReporteService(Repositorio repo, OrdenService ordenes) {
        this.repo = repo;
        this.ordenes = ordenes;
    }

    /**
     * @param desde/hasta rango para servicios, clientes frecuentes, operarios y métodos de pago (default: últimos 30 días)
     * @param mes         mes para "día con mayores ventas" (default: mes actual)
     */
    public Reportes reportes(LocalDate desde, LocalDate hasta, YearMonth mes) {
        LocalDate hoy = LocalDate.now();
        LocalDate h = hasta == null ? hoy : hasta;
        LocalDate d = desde == null ? h.minusDays(29) : desde;
        if (d.isAfter(h)) throw ApiException.invalido("La fecha inicial no puede ser posterior a la final");
        YearMonth m = mes == null ? YearMonth.from(hoy) : mes;
        LocalDate lunes = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        List<Conteo> servicios = repo.serviciosMasSolicitados(d, h);
        List<VentaDia> ventasMes = repo.ventasPorDia(m.atDay(1), m.atEndOfMonth());
        VentaDia mejorDia = ventasMes.stream().max(Comparator.comparingLong(VentaDia::total)).orElse(null);

        return new Reportes(d, h,
                repo.contarVehiculosAtendidos(hoy, hoy),
                lunes, repo.sumarIngresos(lunes, hoy),
                servicios.isEmpty() ? null : servicios.get(0), servicios,
                repo.clientesFrecuentes(d, h, 10),
                repo.serviciosPorOperario(d, h),
                repo.productosStockBajo(),
                repo.clientesSinVisitasDesde(hoy.minusDays(60)),
                m.toString(), mejorDia, ventasMes,
                PagoService.completarMetodos(repo.ventasPorMetodo(d, h)),
                repo.sumarIngresos(d, h));
    }

    public DashboardVista dashboard(Sesion s) {
        LocalDate hoy = LocalDate.now();
        List<OrdenVista> activas = ordenes.listar(s, null, null, null, true);
        Map<EstadoOrden, Long> porEstado = new EnumMap<>(EstadoOrden.class);
        for (EstadoOrden e : EstadoOrden.values()) porEstado.put(e, 0L);
        activas.forEach(o -> porEstado.merge(o.estado(), 1L, Long::sum));

        return switch (s.rol()) {
            case ADMIN -> new DashboardVista(s.rol(), s.nombre(), repo.contarVehiculosAtendidos(hoy, hoy),
                    repo.sumarIngresos(hoy, hoy), (long) repo.listarPagos(hoy, hoy).size(), porEstado, activas,
                    repo.productosStockBajo(), null);
            case RECEPCION -> new DashboardVista(s.rol(), s.nombre(), repo.contarVehiculosAtendidos(hoy, hoy),
                    repo.sumarIngresos(hoy, hoy), (long) repo.listarPagos(hoy, hoy).size(), porEstado, activas, null, null);
            case OPERARIO -> {
                long terminadas = repo.listarOrdenes(new FiltroOrdenes(null, s.empleadoId(), null, null, hoy, hoy)).stream()
                        .filter(o -> o.estado() == EstadoOrden.LISTO || o.estado() == EstadoOrden.FINALIZADO).count();
                yield new DashboardVista(s.rol(), s.nombre(), null, null, null, porEstado, activas, null, terminadas);
            }
        };
    }
}
