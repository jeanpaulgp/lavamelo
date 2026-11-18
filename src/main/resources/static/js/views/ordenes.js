// Órdenes: lista, detalle, crear, cambiar estado, asignar operario, cobrar.
Views.ordenes = async function (params) {
  if (params && params[0]) return detalleOrden(params[0]);

  const puedeCrear = tieneRol('ADMIN', 'RECEPCION');
  const acciones = puedeCrear ? `<button class="btn btn-primario" id="nueva-orden">➕ Nueva orden</button>` : '';
  const sub = tieneRol('OPERARIO') ? 'Solo se muestran las órdenes asignadas a usted.' : 'Flujo: En espera → En proceso → Listo → Finalizado.';
  document.getElementById('vista').innerHTML = encabezado('📝', 'Órdenes de servicio', sub, acciones) +
    `<div class="tarjeta p-4 mb-4 flex flex-wrap gap-3 items-end">
      <div><label class="etiqueta" for="f-estado">Estado</label>
        <select class="campo" id="f-estado"><option value="">Todos</option>
        ${Object.entries(ESTADOS).map(([v, l]) => `<option value="${v}">${l}</option>`).join('')}</select></div>
      <label class="flex items-center gap-2 pb-2 text-sm"><input type="checkbox" id="f-activas" class="w-4 h-4"> Solo activas</label>
    </div>
    <div class="tarjeta overflow-hidden"><div class="overflow-x-auto"><table class="tabla">
      <thead><tr><th>#</th><th>Fecha</th><th>Vehículo</th><th>Cliente</th><th>Servicios</th><th>Operario</th>
      <th class="text-right">Total</th><th>Estado</th><th></th></tr></thead>
      <tbody id="cuerpo-ord"></tbody></table></div></div>`;

  if (puedeCrear) document.getElementById('nueva-orden').onclick = nuevaOrden;
  const fEstado = document.getElementById('f-estado'), fActivas = document.getElementById('f-activas');
  fEstado.onchange = cargar;
  fActivas.onchange = cargar;
  await cargar();

  async function cargar() {
    const qs = new URLSearchParams();
    if (fEstado.value) qs.set('estado', fEstado.value);
    if (fActivas.checked) qs.set('activas', 'true');
    const lista = await api('/ordenes' + (qs.toString() ? '?' + qs : ''));
    document.getElementById('cuerpo-ord').innerHTML = lista.length ? lista.map((o) => `
      <tr class="borde-${o.estado}"><td class="font-mono">#${o.id}</td><td>${fechaHora(o.fecha)}</td>
      <td class="font-mono font-semibold">${esc(o.placa || '')}</td><td>${esc(o.clienteNombre || '—')}</td>
      <td class="max-w-[16rem] truncate">${esc(o.servicios.map((s) => s.nombre).join(', '))}</td>
      <td>${esc(o.operarioNombre || '—')}</td><td class="text-right font-semibold">${cop(o.total)}</td>
      <td>${estadoBadge(o.estado)}</td>
      <td><a class="btn btn-sm btn-suave" href="#/ordenes/${o.id}">Abrir</a></td></tr>`).join('') : vacio('No hay órdenes.', 9);
  }

  async function nuevaOrden() {
    const [vehiculos, servicios, operarios] = await Promise.all([
      api('/vehiculos'), api('/servicios?activos=true'), api('/empleados/operarios')
    ]);
    const cont = document.createElement('div');
    cont.className = 'space-y-4';
    cont.innerHTML = `
      <div><label class="etiqueta" for="o-veh">Vehículo *</label>
        <select class="campo" id="o-veh"><option value="">Seleccione…</option>
        ${vehiculos.map((v) => `<option value="${v.id}" data-cat="${v.categoria}">${esc(v.placa)} · ${esc(v.marca)} ${esc(v.modelo)} (${CATEGORIAS[v.categoria]}) — ${esc(v.clienteNombre)}</option>`).join('')}</select></div>
      <div><label class="etiqueta" for="o-op">Operario asignado *</label>
        <select class="campo" id="o-op"><option value="">Seleccione…</option>
        ${operarios.map((e) => `<option value="${e.id}">${esc(e.nombre)}</option>`).join('')}</select></div>
      <div><span class="etiqueta">Servicios *</span>
        <div id="o-servs" class="grid sm:grid-cols-2 gap-2 max-h-56 overflow-y-auto p-1"></div></div>
      <div><label class="etiqueta" for="o-obs">Observaciones</label><textarea class="campo" id="o-obs" rows="2"></textarea></div>
      <p class="text-right text-lg">Total estimado: <strong id="o-total" class="text-green-600">$0</strong></p>
      <p class="error hidden text-sm text-red-700 bg-red-50 border border-red-200 rounded-lg p-2" role="alert"></p>
      <div class="flex justify-end gap-2"><button class="btn btn-suave" id="o-cancel">Cancelar</button>
        <button class="btn btn-primario" id="o-guardar">Crear orden</button></div>`;
    abrirModal('Nueva orden', cont);

    const selVeh = cont.querySelector('#o-veh'), cont2 = cont.querySelector('#o-servs'), totalEl = cont.querySelector('#o-total');
    function pintarServicios() {
      const cat = selVeh.selectedOptions[0]?.dataset.cat;
      cont2.innerHTML = servicios.map((s) => {
        const precio = cat ? s.precios[cat] : null;
        return `<label class="flex items-center gap-2 border rounded-lg px-3 py-2 text-sm">
          <input type="checkbox" class="serv w-4 h-4" value="${s.id}" data-precio="${precio || 0}">
          <span>${esc(s.nombre)} ${cat ? `<strong class="text-cyan-700">${cop(precio)}</strong>` : '<span class="text-slate-400">(elija vehículo)</span>'}</span></label>`;
      }).join('');
      cont2.querySelectorAll('.serv').forEach((c) => c.onchange = calcular);
      calcular();
    }
    function calcular() {
      let total = 0;
      cont2.querySelectorAll('.serv:checked').forEach((c) => total += Number(c.dataset.precio));
      totalEl.textContent = cop(total);
    }
    selVeh.onchange = pintarServicios;
    pintarServicios();
    cont.querySelector('#o-cancel').onclick = cerrarModal;
    cont.querySelector('#o-guardar').onclick = async () => {
      const err = cont.querySelector('.error');
      err.classList.add('hidden');
      const vehiculoId = Number(selVeh.value), empleadoId = Number(cont.querySelector('#o-op').value);
      const servicioIds = [...cont2.querySelectorAll('.serv:checked')].map((c) => Number(c.value));
      if (!vehiculoId || !empleadoId || !servicioIds.length) {
        err.textContent = 'Seleccione vehículo, operario y al menos un servicio.';
        err.classList.remove('hidden');
        return;
      }
      try {
        const o = await api('/ordenes', { method: 'POST', body: { vehiculoId, empleadoId, servicioIds, observaciones: cont.querySelector('#o-obs').value } });
        cerrarModal();
        toast('Orden #' + o.id + ' creada');
        location.hash = '#/ordenes/' + o.id;
      } catch (e) {
        err.textContent = e.message;
        err.classList.remove('hidden');
      }
    };
  }
};

async function detalleOrden(id) {
  const o = await api('/ordenes/' + id);
  const items = o.servicios.map((s) => `<tr><td>${esc(s.nombre)}</td><td>${s.duracionMin} min</td><td class="text-right">${cop(s.precio)}</td></tr>`).join('');
  const acciones = [];
  const siguiente = { EN_ESPERA: 'EN_PROCESO', EN_PROCESO: 'LISTO' }[o.estado];
  if (siguiente && tieneRol('ADMIN', 'OPERARIO'))
    acciones.push(`<button class="btn btn-azul" data-estado="${siguiente}">Marcar como ${ESTADOS[siguiente]}</button>`);
  if (o.estado === 'LISTO' && tieneRol('ADMIN', 'RECEPCION'))
    acciones.push(`<button class="btn btn-verde" data-cobrar>💳 Registrar pago</button>`);
  if (o.pago) acciones.push(`<a class="btn btn-suave" href="/recibo.html?orden=${o.id}" target="_blank" rel="noopener">🧾 Ver recibo</a>`);
  if ((o.estado === 'EN_ESPERA' || o.estado === 'EN_PROCESO') && tieneRol('ADMIN', 'RECEPCION'))
    acciones.push(`<button class="btn btn-suave" data-reasignar>Reasignar operario</button>`);

  document.getElementById('vista').innerHTML =
    encabezado('📝', 'Orden #' + o.id, '', `<a class="btn btn-suave" href="#/ordenes">← Volver</a>`) +
    `<div class="grid lg:grid-cols-3 gap-4">
      <div class="tarjeta p-5 lg:col-span-2 borde-${o.estado}">
        <div class="flex items-center justify-between mb-3">${estadoBadge(o.estado)}<span class="text-slate-500">${fechaHora(o.fecha)}</span></div>
        <div class="grid sm:grid-cols-2 gap-3 text-sm mb-4">
          <p>🚗 <strong>${esc(o.placa || '')}</strong> · ${esc(o.vehiculo || '')} (${CATEGORIAS[o.categoria] || ''})</p>
          <p>👤 ${esc(o.clienteNombre || '—')} <span class="text-slate-500">${esc(o.clienteTelefono || '')}</span></p>
          <p>🧑‍🔧 Operario: ${esc(o.operarioNombre || '—')}</p>
          <p>⏱️ Duración estimada: ${o.duracionTotalMin} min</p>
        </div>
        ${o.observaciones ? `<p class="text-sm bg-slate-50 rounded-lg p-3 mb-4">📌 ${esc(o.observaciones)}</p>` : ''}
        <table class="tabla"><thead><tr><th>Servicio</th><th>Duración</th><th class="text-right">Precio</th></tr></thead>
          <tbody>${items}</tbody>
          <tfoot><tr><td colspan="2" class="text-right font-semibold">Total</td>
          <td class="text-right font-bold text-green-600 text-lg">${cop(o.total)}</td></tr></tfoot></table>
      </div>
      <div class="tarjeta p-5 h-fit">
        <h2 class="font-semibold mb-3">Acciones</h2>
        <div class="flex flex-col gap-2">${acciones.length ? acciones.join('') : '<p class="text-slate-500 text-sm">Sin acciones disponibles.</p>'}</div>
        ${o.pago ? `<div class="mt-4 pt-4 border-t text-sm text-slate-600">
          <p>✅ Pagado el ${fechaHora(o.pago.fecha)}</p><p>Método: <strong>${METODOS[o.pago.metodo]}</strong></p>
          <p>Monto: <strong>${cop(o.pago.monto)}</strong></p></div>` : ''}
      </div></div>`;

  const bEstado = document.querySelector('[data-estado]');
  if (bEstado) bEstado.onclick = async () => {
    try { await api('/ordenes/' + id + '/estado', { method: 'PATCH', body: { estado: bEstado.dataset.estado } });
      toast('Estado actualizado'); detalleOrden(id); }
    catch (e) { toast(e.message, 'error'); }
  };
  const bCobrar = document.querySelector('[data-cobrar]');
  if (bCobrar) bCobrar.onclick = () => cobrar(o, () => detalleOrden(id));
  const bReasig = document.querySelector('[data-reasignar]');
  if (bReasig) bReasig.onclick = async () => {
    const operarios = await api('/empleados/operarios');
    formularioModal('Reasignar operario', [
      { name: 'empleadoId', label: 'Operario', type: 'select', required: true, ancho: 'full',
        options: operarios.map((e) => ({ value: e.id, label: e.nombre })) }
    ], { empleadoId: o.empleadoId }, async (datos) => {
      await api('/ordenes/' + id + '/operario', { method: 'PATCH', body: { empleadoId: Number(datos.empleadoId) } });
      toast('Operario reasignado');
      detalleOrden(id);
    });
  };
}

// Compartido con la vista de pagos.
function cobrar(orden, alTerminar) {
  formularioModal(`Cobrar orden #${orden.id} — ${cop(orden.total)}`, [
    { name: 'metodo', label: 'Método de pago', type: 'select', required: true, ancho: 'full',
      options: Object.entries(METODOS).map(([value, label]) => ({ value, label })) }
  ], { metodo: 'EFECTIVO' }, async (datos) => {
    await api('/pagos', { method: 'POST', body: { ordenId: orden.id, metodo: datos.metodo } });
    toast('Pago registrado. Orden finalizada.');
    if (alTerminar) alTerminar();
  }, 'Registrar pago');
}
