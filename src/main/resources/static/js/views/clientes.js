// Clientes: CRUD, búsqueda e historial.
Views.clientes = async function () {
  const acciones = `<button class="btn btn-primario" id="nuevo">➕ Nuevo cliente</button>`;
  document.getElementById('vista').innerHTML = encabezado('👥', 'Clientes', 'Datos personales, búsqueda e historial de visitas.', acciones) +
    `<div class="tarjeta p-4 mb-4">
      <label class="etiqueta" for="q-cli">Buscar por nombre, documento o teléfono</label>
      <input class="campo max-w-md" id="q-cli" placeholder="Ej: Juan, 1012345678, 300...">
    </div>
    <div class="tarjeta overflow-hidden"><div class="overflow-x-auto"><table class="tabla">
      <thead><tr><th>Documento</th><th>Nombre</th><th>Teléfono</th><th>Correo</th><th class="text-right">Acciones</th></tr></thead>
      <tbody id="cuerpo-cli"></tbody></table></div></div>`;

  document.getElementById('nuevo').onclick = () => formCliente();
  const q = document.getElementById('q-cli');
  let t;
  q.oninput = () => { clearTimeout(t); t = setTimeout(cargar, 250); };
  await cargar();

  async function cargar() {
    const lista = await api('/clientes' + (q.value.trim() ? `?q=${encodeURIComponent(q.value.trim())}` : ''));
    document.getElementById('cuerpo-cli').innerHTML = lista.length ? lista.map((c) => `
      <tr><td class="font-mono">${esc(c.documento)}</td><td class="font-semibold">${esc(c.nombre)}</td>
      <td>${esc(c.telefono)}</td><td>${esc(c.email || '—')}</td>
      <td class="text-right whitespace-nowrap">
        <button class="btn btn-sm btn-suave" data-hist="${c.id}">Historial</button>
        <button class="btn btn-sm btn-suave" data-edit="${c.id}">Editar</button>
        ${tieneRol('ADMIN') ? `<button class="btn btn-sm btn-peligro" data-del="${c.id}">Eliminar</button>` : ''}
      </td></tr>`).join('') : vacio('No se encontraron clientes.', 5);

    document.querySelectorAll('[data-edit]').forEach((b) => b.onclick = async () =>
      formCliente(await api('/clientes/' + b.dataset.edit)));
    document.querySelectorAll('[data-hist]').forEach((b) => b.onclick = () => historialCliente(b.dataset.hist));
    document.querySelectorAll('[data-del]').forEach((b) => b.onclick = async () => {
      if (!confirmar('¿Eliminar este cliente? Esta acción no se puede deshacer.')) return;
      try { await api('/clientes/' + b.dataset.del, { method: 'DELETE' }); toast('Cliente eliminado'); cargar(); }
      catch (e) { toast(e.message, 'error'); }
    });
  }

  function formCliente(c) {
    formularioModal(c ? 'Editar cliente' : 'Nuevo cliente', [
      { name: 'documento', label: 'Documento', required: true },
      { name: 'nombre', label: 'Nombre completo', required: true, ancho: 'full' },
      { name: 'telefono', label: 'Teléfono', required: true },
      { name: 'email', label: 'Correo', type: 'email' },
      { name: 'direccion', label: 'Dirección', ancho: 'full' }
    ], c, async (datos) => {
      if (c) await api('/clientes/' + c.id, { method: 'PUT', body: datos });
      else await api('/clientes', { method: 'POST', body: datos });
      toast('Cliente guardado');
      cargar();
    });
  }
};

async function historialCliente(id) {
  const h = await api('/clientes/' + id + '/historial');
  const vehiculos = h.vehiculos.map((v) => `<span class="estado bg-cyan-100 text-cyan-800">${esc(v.placa)} · ${esc(v.marca)} ${esc(v.modelo)}</span>`).join(' ') || '<span class="text-slate-400">Sin vehículos</span>';
  const filas = h.ordenes.length ? h.ordenes.map((o) => `<tr>
    <td>${fecha(o.fecha)}</td><td class="font-mono">${esc(o.placa || '')}</td>
    <td>${esc(o.servicios.map((s) => s.nombre).join(', '))}</td>
    <td>${estadoBadge(o.estado)}</td><td class="text-right">${cop(o.total)}</td></tr>`).join('') : vacio('Sin visitas registradas.', 5);
  abrirModal('Historial de ' + h.cliente.nombre, `
    <div class="grid grid-cols-3 gap-3 mb-4 text-center">
      ${kpi('🔁', 'Visitas', h.visitas)}
      ${kpi('💰', 'Total acumulado', cop(h.totalAcumulado), 'text-green-600')}
      ${kpi('📅', 'Última visita', fecha(h.ultimaVisita), 'text-cyan-700')}
    </div>
    <p class="mb-3 text-sm"><strong>Vehículos:</strong> ${vehiculos}</p>
    <div class="overflow-x-auto max-h-80"><table class="tabla">
      <thead><tr><th>Fecha</th><th>Placa</th><th>Servicios</th><th>Estado</th><th class="text-right">Total</th></tr></thead>
      <tbody>${filas}</tbody></table></div>`, 'max-w-3xl');
}
