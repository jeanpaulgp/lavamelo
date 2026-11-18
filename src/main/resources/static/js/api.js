// Utilidades compartidas: llamadas a la API, formatos, modal, formularios y avisos.
const Views = {};

const ESTADOS = {
  EN_ESPERA: 'En espera', EN_PROCESO: 'En proceso', LISTO: 'Listo', FINALIZADO: 'Finalizado'
};
const CATEGORIAS = { MOTO: 'Moto', AUTOMOVIL: 'Automóvil', CAMIONETA: 'Camioneta' };
const METODOS = { EFECTIVO: 'Efectivo', NEQUI: 'Nequi', TARJETA: 'Tarjeta' };
const ROLES = { ADMIN: 'Administrador', RECEPCION: 'Recepcionista / Cajero', OPERARIO: 'Operario' };
const TIPOS_MOV = { ENTRADA: 'Entrada', SALIDA: 'Salida', AJUSTE: 'Ajuste' };

/** fetch a la API con JSON. Lanza Error con el mensaje del servidor. */
async function api(ruta, { method = 'GET', body } = {}) {
  const r = await fetch('/api' + ruta, {
    method,
    cache: 'no-store',
    credentials: 'same-origin',
    headers: body !== undefined ? { 'Content-Type': 'application/json' } : {},
    body: body !== undefined ? JSON.stringify(body) : undefined
  });
  if (r.status === 401) {
    location.href = '/login.html';
    throw new Error('Sesión expirada');
  }
  if (r.status === 204) return null;
  const datos = await r.json().catch(() => null);
  if (!r.ok) throw new Error((datos && datos.detail) || `Error ${r.status}`);
  return datos;
}

// ---------------------------------------------------------------- Formatos
const cop = (n) => '$' + Math.round(Number(n) || 0).toLocaleString('es-CO');
const fecha = (s) => s ? new Date(s.length === 10 ? s + 'T00:00:00' : s).toLocaleDateString('es-CO') : '—';
const fechaHora = (s) => s ? new Date(s).toLocaleString('es-CO', { dateStyle: 'short', timeStyle: 'short' }) : '—';
const hora = (s) => s ? new Date(s).toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit' }) : '';
function isoLocal(d) {
  const z = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${z(d.getMonth() + 1)}-${z(d.getDate())}`;
}
const hoyISO = () => isoLocal(new Date());

/** Escapa HTML para evitar inyección al construir plantillas. */
function esc(v) {
  return String(v ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

const estadoBadge = (e) => `<span class="estado estado-${e}">${ESTADOS[e] || e}</span>`;

function tieneRol(...roles) {
  return roles.includes(App.sesion.rol);
}

// ---------------------------------------------------------------- Avisos
function toast(mensaje, tipo = 'ok') {
  const colores = { ok: 'bg-green-600', error: 'bg-red-600', info: 'bg-cyan-700' };
  const el = document.createElement('div');
  el.className = `${colores[tipo] || colores.ok} text-white px-4 py-3 rounded-xl shadow-lg max-w-sm text-sm`;
  el.setAttribute('role', tipo === 'error' ? 'alert' : 'status');
  el.textContent = mensaje;
  document.getElementById('toasts').appendChild(el);
  setTimeout(() => el.remove(), tipo === 'error' ? 6000 : 3500);
}

const confirmar = (mensaje) => window.confirm(mensaje);

// ---------------------------------------------------------------- Modal
let focoPrevio = null;
function abrirModal(titulo, contenido, ancho = 'max-w-xl') {
  focoPrevio = document.activeElement;
  document.getElementById('modal-titulo').textContent = titulo;
  const cuerpo = document.getElementById('modal-cuerpo');
  cuerpo.innerHTML = '';
  if (typeof contenido === 'string') cuerpo.innerHTML = contenido; else cuerpo.appendChild(contenido);
  const modal = document.getElementById('modal');
  modal.className = `tarjeta w-full ${ancho} p-6 my-8`;
  const fondo = document.getElementById('modal-fondo');
  fondo.classList.remove('hidden');
  fondo.classList.add('flex');
  setTimeout(() => (cuerpo.querySelector('input,select,textarea,button') || modal).focus(), 30);
  return cuerpo;
}
function cerrarModal() {
  const fondo = document.getElementById('modal-fondo');
  fondo.classList.add('hidden');
  fondo.classList.remove('flex');
  if (focoPrevio) focoPrevio.focus();
}

/**
 * Formulario genérico en modal.
 * campos: [{name, label, type: text|number|email|select|textarea|checkbox|password|date, options: [{value,label}], required, min, placeholder, ayuda, ancho: 'full'}]
 * onSubmit(datos) debe devolver una promesa; si falla se muestra el error sin cerrar.
 */
function formularioModal(titulo, campos, valores, onSubmit, textoBoton = 'Guardar') {
  const form = document.createElement('form');
  form.noValidate = true;
  form.className = 'space-y-4';
  const filas = campos.map((c, i) => {
    const id = `f-${c.name}-${i}`;
    const v = valores?.[c.name];
    const req = c.required ? 'required' : '';
    const col = c.ancho === 'full' ? 'sm:col-span-2' : '';
    const ayuda = c.ayuda ? `<p class="text-xs text-slate-500 mt-1">${esc(c.ayuda)}</p>` : '';
    const asterisco = c.required ? ' <span class="text-red-600" aria-hidden="true">*</span>' : '';
    let control;
    if (c.type === 'select') {
      control = `<select class="campo" id="${id}" name="${c.name}" ${req}>
        ${c.placeholder ? `<option value="">${esc(c.placeholder)}</option>` : ''}
        ${c.options.map((o) => `<option value="${esc(o.value)}" ${String(o.value) === String(v ?? '') ? 'selected' : ''}>${esc(o.label)}</option>`).join('')}
      </select>`;
    } else if (c.type === 'textarea') {
      control = `<textarea class="campo" id="${id}" name="${c.name}" rows="2" ${req}>${esc(v)}</textarea>`;
    } else if (c.type === 'checkbox') {
      return `<div class="${col} flex items-center gap-2 pt-6">
        <input type="checkbox" id="${id}" name="${c.name}" class="w-4 h-4" ${v === undefined || v ? 'checked' : ''}>
        <label for="${id}" class="text-sm font-semibold text-slate-700">${esc(c.label)}</label></div>`;
    } else {
      control = `<input class="campo" id="${id}" name="${c.name}" type="${c.type || 'text'}" value="${esc(v)}"
        ${c.min !== undefined ? `min="${c.min}"` : ''} ${c.step ? `step="${c.step}"` : ''}
        placeholder="${esc(c.placeholder || '')}" ${req} autocomplete="off">`;
    }
    return `<div class="${col}"><label class="etiqueta" for="${id}">${esc(c.label)}${asterisco}</label>${control}${ayuda}</div>`;
  });
  form.innerHTML = `
    <div class="grid sm:grid-cols-2 gap-4">${filas.join('')}</div>
    <p class="error hidden text-sm text-red-700 bg-red-50 border border-red-200 rounded-lg p-2" role="alert"></p>
    <div class="flex justify-end gap-2 pt-2">
      <button type="button" class="btn btn-suave" data-cancelar>Cancelar</button>
      <button type="submit" class="btn btn-primario">${esc(textoBoton)}</button>
    </div>`;
  form.querySelector('[data-cancelar]').onclick = cerrarModal;
  form.onsubmit = async (ev) => {
    ev.preventDefault();
    const err = form.querySelector('.error');
    err.classList.add('hidden');
    // Validación básica en el cliente: campos obligatorios
    const faltan = campos.filter((c) => c.required && c.type !== 'checkbox' && !form.elements[c.name].value.trim());
    if (faltan.length) {
      err.textContent = 'Complete los campos obligatorios: ' + faltan.map((c) => c.label).join(', ');
      err.classList.remove('hidden');
      form.elements[faltan[0].name].focus();
      return;
    }
    const datos = {};
    for (const c of campos) {
      const el = form.elements[c.name];
      if (c.type === 'checkbox') datos[c.name] = el.checked;
      else if (c.type === 'number') datos[c.name] = el.value === '' ? null : Number(el.value);
      else datos[c.name] = el.value.trim() === '' ? null : el.value.trim();
    }
    const btn = form.querySelector('[type=submit]');
    btn.disabled = true;
    try {
      await onSubmit(datos);
      cerrarModal();
    } catch (e) {
      err.textContent = e.message;
      err.classList.remove('hidden');
    } finally {
      btn.disabled = false;
    }
  };
  abrirModal(titulo, form);
  return form;
}

/** Encabezado de página estándar. */
function encabezado(icono, titulo, subtitulo = '', acciones = '') {
  return `<div class="flex flex-wrap items-center justify-between gap-3 mb-6">
    <div><h1 class="text-2xl md:text-3xl font-bold"><span aria-hidden="true">${icono}</span> ${esc(titulo)}</h1>
    ${subtitulo ? `<p class="text-slate-500 mt-1">${subtitulo}</p>` : ''}</div>
    <div class="flex flex-wrap gap-2">${acciones}</div></div>`;
}

/** Tarjeta KPI. */
function kpi(icono, etiqueta, valor, color = 'text-cyan-700', extra = '') {
  return `<div class="tarjeta p-5">
    <p class="text-sm text-slate-500 flex items-center gap-2"><span aria-hidden="true">${icono}</span>${esc(etiqueta)}</p>
    <p class="text-3xl font-bold font-titulo mt-2 ${color}">${valor}</p>${extra}</div>`;
}

const vacio = (texto, cols = 1) => `<tr><td colspan="${cols}" class="text-center text-slate-500 py-8">${esc(texto)}</td></tr>`;

/** Borra gráficos anteriores antes de dibujar nuevos (Chart.js). */
const Graficos = {
  lista: [],
  crear(canvas, config) {
    const g = new Chart(canvas, config);
    this.lista.push(g);
    return g;
  },
  limpiar() {
    this.lista.forEach((g) => g.destroy());
    this.lista = [];
  }
};
