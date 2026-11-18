// Reportes: las 8 preguntas del negocio + ventas por método, con rango de fechas.
Views.reportes = async function () {
  const hoy = hoyISO();
  const hace30 = isoLocal(new Date(Date.now() - 29 * 864e5));
  const mesActual = hoy.slice(0, 7);
  document.getElementById('vista').innerHTML = encabezado('📊', 'Reportes', 'Indicadores del negocio.') +
    `<div class="tarjeta p-4 mb-6 flex flex-wrap gap-3 items-end">
      <div><label class="etiqueta" for="r-desde">Desde</label><input type="date" class="campo" id="r-desde" value="${hace30}"></div>
      <div><label class="etiqueta" for="r-hasta">Hasta</label><input type="date" class="campo" id="r-hasta" value="${hoy}"></div>
      <div><label class="etiqueta" for="r-mes">Mes (ventas por día)</label><input type="month" class="campo" id="r-mes" value="${mesActual}"></div>
      <button class="btn btn-primario" id="r-aplicar">Aplicar</button>
    </div><div id="r-cont"></div>`;

  document.getElementById('r-aplicar').onclick = cargar;
  await cargar();

  async function cargar() {
    Graficos.limpiar();
    const desde = document.getElementById('r-desde').value, hasta = document.getElementById('r-hasta').value;
    const mes = document.getElementById('r-mes').value;
    const r = await api(`/reportes?desde=${desde}&hasta=${hasta}&mes=${mes}`);
    const cont = document.getElementById('r-cont');

    const servicio = r.servicioMasSolicitado;
    cont.innerHTML = `
      <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4 mb-6">
        ${kpi('🚗', '1. Vehículos atendidos hoy', r.vehiculosHoy)}
        ${kpi('💰', '2. Ingresos de esta semana', cop(r.ingresosSemana), 'text-green-600', `<p class="text-xs text-slate-500 mt-1">desde ${fecha(r.semanaDesde)}</p>`)}
        ${kpi('🧼', '3. Servicio más solicitado', `<span class="text-xl">${servicio ? esc(servicio.etiqueta) : '—'}</span>`, 'text-cyan-700', servicio ? `<p class="text-xs text-slate-500 mt-1">${servicio.cantidad} veces</p>` : '')}
        ${kpi('📅', '8. Mejor día del mes', r.mejorDiaMes ? fecha(r.mejorDiaMes.fecha) : '—', 'text-navy', r.mejorDiaMes ? `<p class="text-xs text-green-600 mt-1">${cop(r.mejorDiaMes.total)}</p>` : '')}
      </div>

      <div class="grid lg:grid-cols-2 gap-6 mb-6">
        <div class="tarjeta p-5"><h2 class="font-semibold mb-3">3. Servicios más solicitados</h2>
          <canvas id="g-serv" height="200"></canvas></div>
        <div class="tarjeta p-5"><h2 class="font-semibold mb-3">Ventas por método de pago</h2>
          <canvas id="g-metodo" height="200"></canvas></div>
      </div>

      <div class="tarjeta p-5 mb-6"><h2 class="font-semibold mb-3">8. Ventas por día — ${esc(r.mes)}</h2>
        <canvas id="g-ventas" height="90"></canvas></div>

      <div class="grid lg:grid-cols-2 gap-6 mb-6">
        <div class="tarjeta overflow-hidden"><h2 class="font-semibold p-4 pb-0">4. Clientes más frecuentes</h2>
          <div class="overflow-x-auto p-4"><table class="tabla"><thead><tr><th>Cliente</th><th class="text-right">Visitas</th><th class="text-right">Total</th></tr></thead>
          <tbody>${filasConteo(r.clientesFrecuentes)}</tbody></table></div></div>
        <div class="tarjeta overflow-hidden"><h2 class="font-semibold p-4 pb-0">5. Servicios por operario</h2>
          <div class="overflow-x-auto p-4"><table class="tabla"><thead><tr><th>Operario</th><th class="text-right">Servicios</th><th class="text-right">Total</th></tr></thead>
          <tbody>${filasConteo(r.serviciosPorOperario)}</tbody></table></div></div>
      </div>

      <div class="grid lg:grid-cols-2 gap-6">
        <div class="tarjeta overflow-hidden"><h2 class="font-semibold p-4 pb-0">6. Insumos próximos a agotarse</h2>
          <div class="overflow-x-auto p-4"><table class="tabla"><thead><tr><th>Producto</th><th class="text-right">Stock</th><th class="text-right">Mínimo</th></tr></thead>
          <tbody>${r.insumosPorAgotarse.length ? r.insumosPorAgotarse.map((p) => `<tr class="fila-stock-bajo"><td>${esc(p.nombre)}</td><td class="text-right font-bold">${p.stock}</td><td class="text-right">${p.stockMinimo}</td></tr>`).join('') : vacio('Todo con stock suficiente.', 3)}</tbody></table></div></div>
        <div class="tarjeta overflow-hidden"><h2 class="font-semibold p-4 pb-0">7. Clientes sin volver (60+ días)</h2>
          <div class="overflow-x-auto p-4"><table class="tabla"><thead><tr><th>Cliente</th><th>Teléfono</th><th class="text-right">Última visita</th></tr></thead>
          <tbody>${r.clientesInactivos.length ? r.clientesInactivos.map((c) => `<tr><td>${esc(c.nombre)}</td><td>${esc(c.telefono || '—')}</td><td class="text-right">${fecha(c.ultimaVisita)}</td></tr>`).join('') : vacio('Todos han vuelto recientemente.', 3)}</tbody></table></div></div>
      </div>`;

    // Gráficos
    const CYAN = '#0891b2', COLORES = ['#0891b2', '#2563eb', '#16a34a', '#d97706', '#7c3aed', '#db2777'];
    const topServ = r.servicios.slice(0, 6);
    Graficos.crear(document.getElementById('g-serv'), {
      type: 'bar',
      data: { labels: topServ.map((s) => s.etiqueta), datasets: [{ label: 'Veces', data: topServ.map((s) => s.cantidad), backgroundColor: CYAN, borderRadius: 6 }] },
      options: { plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true, ticks: { precision: 0 } } } }
    });
    Graficos.crear(document.getElementById('g-metodo'), {
      type: 'doughnut',
      data: { labels: r.ventasPorMetodo.map((m) => METODOS[m.etiqueta] || m.etiqueta), datasets: [{ data: r.ventasPorMetodo.map((m) => m.total), backgroundColor: COLORES }] },
      options: { plugins: { tooltip: { callbacks: { label: (c) => `${c.label}: ${cop(c.raw)}` } } } }
    });
    Graficos.crear(document.getElementById('g-ventas'), {
      type: 'line',
      data: { labels: r.ventasMes.map((v) => fecha(v.fecha)), datasets: [{ label: 'Ventas', data: r.ventasMes.map((v) => v.total), borderColor: CYAN, backgroundColor: 'rgba(8,145,178,.15)', fill: true, tension: .3 }] },
      options: { plugins: { legend: { display: false }, tooltip: { callbacks: { label: (c) => cop(c.raw) } } }, scales: { y: { beginAtZero: true, ticks: { callback: (v) => cop(v) } } } }
    });
  }

  function filasConteo(lista) {
    return lista.length ? lista.map((c) => `<tr><td>${esc(c.etiqueta)}</td><td class="text-right font-semibold">${c.cantidad}</td><td class="text-right">${cop(c.total)}</td></tr>`).join('') : vacio('Sin datos en el rango.', 3);
  }
};
