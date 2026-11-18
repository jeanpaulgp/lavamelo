// Shell de la aplicación: sesión, menú por rol y enrutador por hash.
const App = { sesion: null };

// Módulos del menú con el rol que puede verlos (el backend es la autoridad real).
const MODULOS = [
  { id: 'inicio', icono: '🏠', nombre: 'Inicio', roles: ['ADMIN', 'RECEPCION', 'OPERARIO'], render: () => Views.dashboard() },
  { id: 'ordenes', icono: '📝', nombre: 'Órdenes', roles: ['ADMIN', 'RECEPCION', 'OPERARIO'], render: (p) => Views.ordenes(p) },
  { id: 'clientes', icono: '👥', nombre: 'Clientes', roles: ['ADMIN', 'RECEPCION'], render: () => Views.clientes() },
  { id: 'vehiculos', icono: '🚗', nombre: 'Vehículos', roles: ['ADMIN', 'RECEPCION'], render: () => Views.vehiculos() },
  { id: 'pagos', icono: '💳', nombre: 'Pagos', roles: ['ADMIN', 'RECEPCION'], render: () => Views.pagos() },
  { id: 'servicios', icono: '🧼', nombre: 'Servicios', roles: ['ADMIN'], render: () => Views.servicios() },
  { id: 'inventario', icono: '📦', nombre: 'Inventario', roles: ['ADMIN'], render: () => Views.inventario() },
  { id: 'compras', icono: '🧾', nombre: 'Compras', roles: ['ADMIN'], render: () => Views.compras() },
  { id: 'proveedores', icono: '🏭', nombre: 'Proveedores', roles: ['ADMIN'], render: () => Views.proveedores() },
  { id: 'empleados', icono: '🧑‍🔧', nombre: 'Empleados', roles: ['ADMIN'], render: () => Views.empleados() },
  { id: 'reportes', icono: '📊', nombre: 'Reportes', roles: ['ADMIN'], render: () => Views.reportes() }
];

function modulosVisibles() {
  return MODULOS.filter((m) => m.roles.includes(App.sesion.rol));
}

function pintarMenu() {
  document.getElementById('menu').innerHTML = modulosVisibles()
    .map((m) => `<a href="#/${m.id}" data-id="${m.id}" class="menu-item">
      <span aria-hidden="true" class="text-lg">${m.icono}</span><span>${m.nombre}</span></a>`).join('');
}

function marcarActivo(id) {
  document.querySelectorAll('#menu .menu-item').forEach((a) =>
    a.classList.toggle('activo', a.dataset.id === id));
}

// ---------------------------------------------------------------- Enrutador
async function router() {
  const partes = (location.hash.replace(/^#\/?/, '') || 'inicio').split('/');
  const id = partes[0];
  const modulo = MODULOS.find((m) => m.id === id);
  const vista = document.getElementById('vista');

  if (!modulo) { location.hash = '#/inicio'; return; }
  if (!modulo.roles.includes(App.sesion.rol)) {
    vista.innerHTML = `<div class="tarjeta p-8 text-center">
      <p class="text-5xl mb-3">🔒</p><h1 class="text-xl font-bold">Acceso restringido</h1>
      <p class="text-slate-500 mt-2">Su rol (${ROLES[App.sesion.rol]}) no tiene acceso a este módulo.</p></div>`;
    return;
  }
  marcarActivo(id);
  cerrarSidebarMovil();
  Graficos.limpiar();
  vista.innerHTML = `<div class="text-center text-slate-400 py-16">Cargando…</div>`;
  try {
    await modulo.render(partes.slice(1));
    vista.focus();
  } catch (e) {
    vista.innerHTML = `<div class="tarjeta p-8 text-center">
      <p class="text-4xl mb-2">⚠️</p><p class="text-red-700 font-semibold">${esc(e.message)}</p>
      <button class="btn btn-suave mt-4" onclick="router()">Reintentar</button></div>`;
  }
}

// ---------------------------------------------------------------- Sidebar móvil
function abrirSidebarMovil() {
  document.getElementById('sidebar').classList.remove('-translate-x-full');
  document.getElementById('sidebar-velo').classList.remove('hidden');
}
function cerrarSidebarMovil() {
  if (window.innerWidth < 768) {
    document.getElementById('sidebar').classList.add('-translate-x-full');
    document.getElementById('sidebar-velo').classList.add('hidden');
  }
}

// ---------------------------------------------------------------- Arranque
async function iniciar() {
  try {
    App.sesion = await api('/auth/me');
  } catch {
    location.href = '/login.html';
    return;
  }
  document.getElementById('usuario-nombre').textContent = App.sesion.nombre;
  document.getElementById('usuario-rol').textContent = ROLES[App.sesion.rol] || App.sesion.rol;
  pintarMenu();

  document.getElementById('btn-salir').onclick = async () => {
    await api('/auth/logout', { method: 'POST' }).catch(() => {});
    location.href = '/login.html';
  };
  document.getElementById('btn-menu').onclick = abrirSidebarMovil;
  document.getElementById('sidebar-velo').onclick = cerrarSidebarMovil;
  document.getElementById('modal-cerrar').onclick = cerrarModal;
  document.getElementById('modal-fondo').addEventListener('click', (e) => {
    if (e.target.id === 'modal-fondo') cerrarModal();
  });
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && !document.getElementById('modal-fondo').classList.contains('hidden')) cerrarModal();
  });

  window.addEventListener('hashchange', router);
  if (!location.hash) location.hash = '#/inicio';
  router();
}

iniciar();
