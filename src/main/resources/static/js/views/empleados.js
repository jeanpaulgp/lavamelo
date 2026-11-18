// Empleados: CRUD (solo Administrador). Incluye usuario, rol y estado activo.
Views.empleados = async function () {
  document.getElementById('vista').innerHTML = encabezado('🧑‍🔧', 'Empleados', 'Usuarios del sistema con su rol y estado.',
    `<button class="btn btn-primario" id="nuevo-emp">➕ Nuevo empleado</button>`) +
    `<div class="tarjeta overflow-hidden"><div class="overflow-x-auto"><table class="tabla">
      <thead><tr><th>Nombre</th><th>Usuario</th><th>Rol</th><th>Documento</th><th>Estado</th><th class="text-right">Acciones</th></tr></thead>
      <tbody id="cuerpo-emp"></tbody></table></div></div>`;

  document.getElementById('nuevo-emp').onclick = () => formEmp();
  await cargar();

  async function cargar() {
    const lista = await api('/empleados');
    document.getElementById('cuerpo-emp').innerHTML = lista.map((e) => `
      <tr class="${e.activo ? '' : 'opacity-60'}"><td class="font-semibold">${esc(e.nombre)}</td>
      <td class="font-mono">${esc(e.usuario)}</td><td>${ROLES[e.rol]}</td><td>${esc(e.documento)}</td>
      <td>${e.activo ? '<span class="estado estado-LISTO">Activo</span>' : '<span class="estado estado-FINALIZADO">Inactivo</span>'}</td>
      <td class="text-right whitespace-nowrap">
        <button class="btn btn-sm btn-suave" data-edit="${e.id}">Editar</button>
        <button class="btn btn-sm btn-peligro" data-del="${e.id}">Eliminar</button></td></tr>`).join('');
    const mapa = Object.fromEntries(lista.map((e) => [e.id, e]));
    document.querySelectorAll('[data-edit]').forEach((b) => b.onclick = () => formEmp(mapa[b.dataset.edit]));
    document.querySelectorAll('[data-del]').forEach((b) => b.onclick = async () => {
      if (!confirmar('¿Eliminar este empleado?')) return;
      try { await api('/empleados/' + b.dataset.del, { method: 'DELETE' }); toast('Empleado eliminado'); cargar(); }
      catch (e) { toast(e.message, 'error'); }
    });
  }

  function formEmp(e) {
    formularioModal(e ? 'Editar empleado' : 'Nuevo empleado', [
      { name: 'nombre', label: 'Nombre completo', required: true, ancho: 'full' },
      { name: 'documento', label: 'Documento', required: true },
      { name: 'telefono', label: 'Teléfono' },
      { name: 'usuario', label: 'Usuario', required: true },
      { name: 'rol', label: 'Rol', type: 'select', required: true,
        options: Object.entries(ROLES).map(([value, label]) => ({ value, label })) },
      { name: 'password', label: e ? 'Nueva contraseña' : 'Contraseña', type: 'password', required: !e,
        ayuda: e ? 'Déjela vacía para no cambiarla.' : 'Mínimo 6 caracteres.' },
      { name: 'activo', label: 'Activo', type: 'checkbox' }
    ], e, async (datos) => {
      if (e) await api('/empleados/' + e.id, { method: 'PUT', body: datos });
      else await api('/empleados', { method: 'POST', body: datos });
      toast('Empleado guardado');
      cargar();
    });
  }
};
