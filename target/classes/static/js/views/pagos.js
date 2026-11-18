// Pagos: órdenes listas para cobrar, ingresos del día y listado de pagos.
Views.pagos = async function () {
  document.getElementById('vista').innerHTML = encabezado('💳', 'Pagos', 'Cobro de órdenes listas, ingresos del día y recibos.') +
    `<div id="ingresos" class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4 mb-6"></div>
     <div class="grid lg:grid-cols-2 gap-6">
       <div class="tarjeta overflow-hidden"><h2 class="text-lg font-semibold p-4 pb-0">Órdenes listas para cobrar</h2>
         <div class="overflow-x-auto p-4"><table class="tabla">
         <thead><tr><th>#</th><th>Vehículo</th><th>Cliente</th><th class="text-right">Total</th><th></th></tr></thead>
         <tbody id="cuerpo-listas"></tbody></table></div></div>
       <div class="tarjeta overflow-hidden">
         <div class="flex items-center justify-between p-4 pb-0"><h2 class="text-lg font-semibold">Pagos</h2>
           <input type="date" class="campo max-w-[10rem]" id="f-fecha" value="${hoyISO()}"></div>
         <div class="overflow-x-auto p-4"><table class="tabla">
         <thead><tr><th>Hora</th><th>#</th><th>Cliente</th><th>Método</th><th class="text-right">Monto</th><th></th></tr></thead>
         <tbody id="cuerpo-pagos"></tbody></table></div></div>
     </div>`;

  const fFecha = document.getElementById('f-fecha');
  fFecha.onchange = () => { cargarIngresos(); cargarPagos(); };
  await Promise.all([cargarListas(), cargarIngresos(), cargarPagos()]);

  async function cargarListas() {
    const listas = await api('/ordenes?estado=LISTO');
    document.getElementById('cuerpo-listas').innerHTML = listas.length ? listas.map((o) => `
      <tr><td class="font-mono">#${o.id}</td><td class="font-semibold">${esc(o.placa || '')}</td>
      <td>${esc(o.clienteNombre || '—')}</td><td class="text-right font-semibold">${cop(o.total)}</td>
      <td><button class="btn btn-sm btn-verde" data-cobrar="${o.id}">Cobrar</button></td></tr>`).join('') : vacio('No hay órdenes listas.', 5);
    const mapa = Object.fromEntries(listas.map((o) => [o.id, o]));
    document.querySelectorAll('[data-cobrar]').forEach((b) => b.onclick = () =>
      cobrar(mapa[b.dataset.cobrar], () => { cargarListas(); cargarIngresos(); cargarPagos(); }));
  }

  async function cargarIngresos() {
    const d = await api('/pagos/ingresos-dia?fecha=' + fFecha.value);
    const porMetodo = d.porMetodo.map((m) => `${METODOS[m.etiqueta]}: <strong>${cop(m.total)}</strong>`).join(' · ');
    document.getElementById('ingresos').innerHTML =
      kpi('💰', 'Ingresos del día', cop(d.total), 'text-green-600') +
      kpi('🧾', 'Pagos', d.pagos, 'text-cyan-700') +
      `<div class="tarjeta p-5 sm:col-span-2"><p class="text-sm text-slate-500 mb-2">Por método</p><p class="text-sm">${porMetodo}</p></div>`;
  }

  async function cargarPagos() {
    const pagos = await api(`/pagos?desde=${fFecha.value}&hasta=${fFecha.value}`);
    document.getElementById('cuerpo-pagos').innerHTML = pagos.length ? pagos.map((p) => `
      <tr><td>${hora(p.fecha)}</td><td class="font-mono">#${p.ordenId}</td><td>${esc(p.clienteNombre || '—')}</td>
      <td>${METODOS[p.metodo]}</td><td class="text-right font-semibold">${cop(p.monto)}</td>
      <td><a class="btn btn-sm btn-suave" href="/recibo.html?orden=${p.ordenId}" target="_blank" rel="noopener">Recibo</a></td></tr>`).join('') : vacio('Sin pagos en la fecha.', 6);
  }
};
