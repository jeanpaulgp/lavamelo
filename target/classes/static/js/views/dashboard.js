// Dashboard de inicio según rol.
Views.dashboard = async function () {
  const d = await api('/dashboard');
  const vista = document.getElementById('vista');
  const saludo = `<p class="text-slate-500 mt-1">Hola, ${esc(d.nombre)} · ${ROLES[d.rol]}</p>`;

  let kpis = '';
  if (d.rol === 'ADMIN' || d.rol === 'RECEPCION') {
    kpis = `<div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 mb-6">
      ${kpi('🚗', 'Vehículos atendidos hoy', d.vehiculosHoy)}
      ${kpi('💳', 'Ingresos de hoy', cop(d.ingresosHoy), 'text-green-600')}
      ${kpi('🧾', 'Pagos de hoy', d.pagosHoy, 'text-cyan-700')}
    </div>`;
  } else {
    const enProceso = d.porEstado.EN_PROCESO || 0, listas = d.porEstado.LISTO || 0;
    kpis = `<div class="grid gap-4 sm:grid-cols-3 mb-6">
      ${kpi('🔧', 'En proceso', enProceso, 'text-blue-600')}
      ${kpi('✅', 'Listas', listas, 'text-green-600')}
      ${kpi('🏁', 'Terminadas hoy', d.terminadasHoy || 0)}
    </div>`;
  }

  // Alertas de stock (solo admin)
  let alertas = '';
  if (d.stockBajo && d.stockBajo.length) {
    alertas = `<div class="alerta-stock rounded-xl p-4 mb-6">
      <p class="font-semibold mb-2">⚠️ ${d.stockBajo.length} producto(s) con stock bajo</p>
      <ul class="text-sm space-y-1">${d.stockBajo.map((p) =>
        `<li>• ${esc(p.nombre)}: <strong>${p.stock} ${esc(p.unidad)}</strong> (mínimo ${p.stockMinimo})</li>`).join('')}</ul>
      ${tieneRol('ADMIN') ? `<a href="#/inventario" class="btn btn-sm btn-suave mt-3">Ir a inventario</a>` : ''}</div>`;
  }

  const titulo = d.rol === 'OPERARIO' ? 'Mis órdenes asignadas' : 'Órdenes activas';
  const filas = d.ordenes.length ? d.ordenes.map(filaOrdenDash).join('') : vacio('No hay órdenes activas.', 6);

  vista.innerHTML = encabezado('🏠', 'Panel de inicio', '') + saludo +
    `<div class="mt-6">${kpis}${alertas}
      <div class="tarjeta overflow-hidden">
        <h2 class="text-lg font-semibold p-4 pb-0">${titulo}</h2>
        <div class="overflow-x-auto p-4"><table class="tabla">
          <thead><tr><th>#</th><th>Vehículo</th><th>Cliente</th><th>Operario</th><th>Estado</th><th></th></tr></thead>
          <tbody>${filas}</tbody></table></div></div></div>`;
};

function filaOrdenDash(o) {
  return `<tr class="borde-${o.estado}">
    <td class="font-mono">#${o.id}</td>
    <td><strong>${esc(o.placa || '')}</strong><br><span class="text-xs text-slate-500">${esc(o.vehiculo || '')}</span></td>
    <td>${esc(o.clienteNombre || '—')}</td>
    <td>${esc(o.operarioNombre || '—')}</td>
    <td>${estadoBadge(o.estado)}</td>
    <td><a class="btn btn-sm btn-suave" href="#/ordenes/${o.id}">Ver</a></td></tr>`;
}
