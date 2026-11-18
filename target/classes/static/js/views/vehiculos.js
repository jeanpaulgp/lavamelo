// Vehículos: registro con placa única, vinculado a cliente, historial automático.
Views.vehiculos = async function () {
  const acciones = `<button class="btn btn-primario" id="nuevo-veh">➕ Nuevo vehículo</button>`;
  document.getElementById('vista').innerHTML = encabezado('🚗', 'Vehículos', 'Placa única, categoría y dueño. Historial por vehículo.', acciones) +
    `<div class="tarjeta p-4 mb-4">
      <label class="etiqueta" for="q-veh">Buscar por placa, marca, modelo o dueño</label>
      <input class="campo max-w-md" id="q-veh" placeholder="Ej: ABC123, Renault, Juan...">
    </div>
    <div class="tarjeta overflow-hidden"><div class="overflow-x-auto"><table class="tabla">
      <thead><tr><th>Placa</th><th>Vehículo</th><th>Categoría</th><th>Dueño</th><th class="text-right">Acciones</th></tr></thead>
      <tbody id="cuerpo-veh"></tbody></table></div></div>`;

  const clientes = await api('/clientes');
  document.getElementById('nuevo-veh').onclick = () => formVehiculo(null, clientes);
  const q = document.getElementById('q-veh');
  let t;
  q.oninput = () => { clearTimeout(t); t = setTimeout(cargar, 250); };
  await cargar();

  async function cargar() {
    const lista = await api('/vehiculos' + (q.value.trim() ? `?q=${encodeURIComponent(q.value.trim())}` : ''));
    document.getElementById('cuerpo-veh').innerHTML = lista.length ? lista.map((v) => `
      <tr><td class="font-mono font-bold">${esc(v.placa)}</td>
      <td>${esc(v.marca)} ${esc(v.modelo)}<br><span class="text-xs text-slate-500">${esc(v.color || '')}</span></td>
      <td>${CATEGORIAS[v.categoria]}</td><td>${esc(v.clienteNombre || '—')}</td>
      <td class="text-right whitespace-nowrap">
        <button class="btn btn-sm btn-suave" data-hist="${v.id}">Historial</button>
        <button class="btn btn-sm btn-suave" data-edit="${v.id}">Editar</button>
        ${tieneRol('ADMIN') ? `<button class="btn btn-sm btn-peligro" data-del="${v.id}">Eliminar</button>` : ''}
      </td></tr>`).join('') : vacio('No se encontraron vehículos.', 5);

    document.querySelectorAll('[data-edit]').forEach((b) => b.onclick = async () =>
      formVehiculo(await api('/vehiculos/' + b.dataset.edit), clientes));
    document.querySelectorAll('[data-hist]').forEach((b) => b.onclick = () => historialVehiculo(b.dataset.hist));
    document.querySelectorAll('[data-del]').forEach((b) => b.onclick = async () => {
      if (!confirmar('¿Eliminar este vehículo?')) return;
      try { await api('/vehiculos/' + b.dataset.del, { method: 'DELETE' }); toast('Vehículo eliminado'); cargar(); }
      catch (e) { toast(e.message, 'error'); }
    });
  }

  function formVehiculo(v, clientes) {
    formularioModal(v ? 'Editar vehículo' : 'Nuevo vehículo', [
      { name: 'placa', label: 'Placa', required: true, placeholder: 'ABC123' },
      { name: 'categoria', label: 'Categoría', type: 'select', required: true, placeholder: 'Seleccione…',
        options: Object.entries(CATEGORIAS).map(([value, label]) => ({ value, label })) },
      { name: 'marca', label: 'Marca', required: true },
      { name: 'modelo', label: 'Modelo', required: true },
      { name: 'color', label: 'Color' },
      { name: 'clienteId', label: 'Dueño', type: 'select', required: true, placeholder: 'Seleccione cliente…',
        options: clientes.map((c) => ({ value: c.id, label: `${c.nombre} (${c.documento})` })) }
    ], v, async (datos) => {
      datos.clienteId = Number(datos.clienteId);
      if (v) await api('/vehiculos/' + v.id, { method: 'PUT', body: datos });
      else await api('/vehiculos', { method: 'POST', body: datos });
      toast('Vehículo guardado');
      cargar();
    });
  }
};

async function historialVehiculo(id) {
  const h = await api('/vehiculos/' + id + '/historial');
  const v = h.vehiculo;
  const filas = h.ordenes.length ? h.ordenes.map((o) => `<tr>
    <td>${fecha(o.fecha)}</td><td>${esc(o.servicios.map((s) => s.nombre).join(', '))}</td>
    <td>${esc(o.operarioNombre || '—')}</td><td>${estadoBadge(o.estado)}</td>
    <td class="text-right">${o.pago ? cop(o.pago.monto) : '—'}</td></tr>`).join('') : vacio('Sin visitas.', 5);
  abrirModal(`Historial · ${v.placa}`, `
    <p class="text-slate-600 mb-3">${esc(v.marca)} ${esc(v.modelo)} · ${CATEGORIAS[v.categoria]} · Dueño: <strong>${esc(v.clienteNombre || '—')}</strong></p>
    <div class="grid grid-cols-3 gap-3 mb-4 text-center">
      ${kpi('🔁', 'Visitas', h.visitas)}
      ${kpi('💰', 'Total acumulado', cop(h.totalAcumulado), 'text-green-600')}
      ${kpi('🧼', 'Último servicio', `<span class="text-base">${esc(h.ultimoServicio || '—')}</span>`, 'text-cyan-700',
        `<p class="text-xs text-slate-500 mt-1">${fecha(h.ultimaVisita)}</p>`)}
    </div>
    <div class="overflow-x-auto max-h-80"><table class="tabla">
      <thead><tr><th>Fecha</th><th>Servicios</th><th>Operario</th><th>Estado</th><th class="text-right">Pagado</th></tr></thead>
      <tbody>${filas}</tbody></table></div>`, 'max-w-3xl');
}
