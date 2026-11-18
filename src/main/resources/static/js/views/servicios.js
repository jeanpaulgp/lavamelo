// Servicios: catálogo con duración y precio por categoría.
Views.servicios = async function () {
  const acciones = `<button class="btn btn-primario" id="nuevo-serv">➕ Nuevo servicio</button>`;
  document.getElementById('vista').innerHTML = encabezado('🧼', 'Servicios', 'Catálogo con duración y precio por categoría de vehículo.', acciones) +
    `<div class="tarjeta overflow-hidden"><div class="overflow-x-auto"><table class="tabla">
      <thead><tr><th>Servicio</th><th>Duración</th><th class="text-right">Moto</th><th class="text-right">Automóvil</th>
      <th class="text-right">Camioneta</th><th>Estado</th><th class="text-right">Acciones</th></tr></thead>
      <tbody id="cuerpo-serv"></tbody></table></div></div>`;

  document.getElementById('nuevo-serv').onclick = () => formServicio();
  await cargar();

  async function cargar() {
    const lista = await api('/servicios');
    document.getElementById('cuerpo-serv').innerHTML = lista.length ? lista.map((s) => `
      <tr class="${s.activo ? '' : 'opacity-60'}">
      <td class="font-semibold">${esc(s.nombre)}<br><span class="text-xs text-slate-500">${esc(s.descripcion || '')}</span></td>
      <td>${s.duracionMin} min</td>
      <td class="text-right">${cop(s.precios.MOTO)}</td><td class="text-right">${cop(s.precios.AUTOMOVIL)}</td>
      <td class="text-right">${cop(s.precios.CAMIONETA)}</td>
      <td>${s.activo ? '<span class="estado estado-LISTO">Activo</span>' : '<span class="estado estado-FINALIZADO">Inactivo</span>'}</td>
      <td class="text-right whitespace-nowrap">
        <button class="btn btn-sm btn-suave" data-edit="${s.id}">Editar</button>
        ${s.activo ? `<button class="btn btn-sm btn-peligro" data-del="${s.id}">Desactivar</button>` : ''}
      </td></tr>`).join('') : vacio('No hay servicios.', 7);

    const mapa = Object.fromEntries(lista.map((s) => [s.id, s]));
    document.querySelectorAll('[data-edit]').forEach((b) => b.onclick = () => formServicio(mapa[b.dataset.edit]));
    document.querySelectorAll('[data-del]').forEach((b) => b.onclick = async () => {
      if (!confirmar('¿Desactivar este servicio? Dejará de aparecer al crear órdenes.')) return;
      try { await api('/servicios/' + b.dataset.del, { method: 'DELETE' }); toast('Servicio desactivado'); cargar(); }
      catch (e) { toast(e.message, 'error'); }
    });
  }

  function formServicio(s) {
    const v = s ? { ...s, moto: s.precios.MOTO, auto: s.precios.AUTOMOVIL, camioneta: s.precios.CAMIONETA } : { activo: true };
    formularioModal(s ? 'Editar servicio' : 'Nuevo servicio', [
      { name: 'nombre', label: 'Nombre', required: true, ancho: 'full' },
      { name: 'descripcion', label: 'Descripción', ancho: 'full' },
      { name: 'duracionMin', label: 'Duración (minutos)', type: 'number', required: true, min: 5 },
      { name: 'activo', label: 'Activo', type: 'checkbox' },
      { name: 'moto', label: 'Precio Moto', type: 'number', required: true, min: 0 },
      { name: 'auto', label: 'Precio Automóvil', type: 'number', required: true, min: 0 },
      { name: 'camioneta', label: 'Precio Camioneta', type: 'number', required: true, min: 0 }
    ], v, async (datos) => {
      const body = {
        nombre: datos.nombre, descripcion: datos.descripcion, duracionMin: datos.duracionMin, activo: datos.activo,
        precios: { MOTO: datos.moto, AUTOMOVIL: datos.auto, CAMIONETA: datos.camioneta }
      };
      if (s) await api('/servicios/' + s.id, { method: 'PUT', body });
      else await api('/servicios', { method: 'POST', body });
      toast('Servicio guardado');
      cargar();
    });
  }
};
