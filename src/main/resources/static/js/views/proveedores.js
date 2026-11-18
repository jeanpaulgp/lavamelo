// Proveedores: CRUD simple (solo Administrador).
Views.proveedores = async function () {
  document.getElementById('vista').innerHTML = encabezado('🏭', 'Proveedores', 'Datos de contacto de proveedores de insumos.',
    `<button class="btn btn-primario" id="nuevo-prov">➕ Nuevo proveedor</button>`) +
    `<div class="tarjeta overflow-hidden"><div class="overflow-x-auto"><table class="tabla">
      <thead><tr><th>NIT</th><th>Nombre</th><th>Teléfono</th><th>Correo</th><th class="text-right">Acciones</th></tr></thead>
      <tbody id="cuerpo-prov"></tbody></table></div></div>`;

  document.getElementById('nuevo-prov').onclick = () => formProv();
  await cargar();

  async function cargar() {
    const lista = await api('/proveedores');
    document.getElementById('cuerpo-prov').innerHTML = lista.length ? lista.map((p) => `
      <tr><td class="font-mono">${esc(p.nit)}</td><td class="font-semibold">${esc(p.nombre)}</td>
      <td>${esc(p.telefono || '—')}</td><td>${esc(p.email || '—')}</td>
      <td class="text-right whitespace-nowrap">
        <button class="btn btn-sm btn-suave" data-edit="${p.id}">Editar</button>
        <button class="btn btn-sm btn-peligro" data-del="${p.id}">Eliminar</button></td></tr>`).join('') : vacio('No hay proveedores.', 5);
    const mapa = Object.fromEntries(lista.map((p) => [p.id, p]));
    document.querySelectorAll('[data-edit]').forEach((b) => b.onclick = () => formProv(mapa[b.dataset.edit]));
    document.querySelectorAll('[data-del]').forEach((b) => b.onclick = async () => {
      if (!confirmar('¿Eliminar este proveedor?')) return;
      try { await api('/proveedores/' + b.dataset.del, { method: 'DELETE' }); toast('Proveedor eliminado'); cargar(); }
      catch (e) { toast(e.message, 'error'); }
    });
  }

  function formProv(p) {
    formularioModal(p ? 'Editar proveedor' : 'Nuevo proveedor', [
      { name: 'nit', label: 'NIT', required: true },
      { name: 'nombre', label: 'Nombre', required: true },
      { name: 'telefono', label: 'Teléfono' },
      { name: 'email', label: 'Correo', type: 'email' },
      { name: 'direccion', label: 'Dirección', ancho: 'full' }
    ], p, async (datos) => {
      if (p) await api('/proveedores/' + p.id, { method: 'PUT', body: datos });
      else await api('/proveedores', { method: 'POST', body: datos });
      toast('Proveedor guardado');
      cargar();
    });
  }
};
