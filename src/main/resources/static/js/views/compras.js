// Compras a proveedores: registran entradas de inventario.
Views.compras = async function () {
  document.getElementById('vista').innerHTML = encabezado('🧾', 'Compras', 'Entradas de inventario desde proveedores.',
    `<button class="btn btn-primario" id="nueva-compra">➕ Nueva compra</button>`) +
    `<div class="tarjeta overflow-hidden"><div class="overflow-x-auto"><table class="tabla">
      <thead><tr><th>#</th><th>Fecha</th><th>Proveedor</th><th>Productos</th><th class="text-right">Total</th><th></th></tr></thead>
      <tbody id="cuerpo-compras"></tbody></table></div></div>`;

  document.getElementById('nueva-compra').onclick = nuevaCompra;
  await cargar();

  async function cargar() {
    const lista = await api('/inventario/compras');
    document.getElementById('cuerpo-compras').innerHTML = lista.length ? lista.map((c) => `
      <tr><td class="font-mono">#${c.id}</td><td>${fechaHora(c.fecha)}</td><td>${esc(c.proveedorNombre || '—')}</td>
      <td>${c.items.length} ítem(s)</td><td class="text-right font-semibold">${cop(c.total)}</td>
      <td><button class="btn btn-sm btn-suave" data-ver="${c.id}">Detalle</button></td></tr>`).join('') : vacio('No hay compras.', 6);
    const mapa = Object.fromEntries(lista.map((c) => [c.id, c]));
    document.querySelectorAll('[data-ver]').forEach((b) => b.onclick = () => {
      const c = mapa[b.dataset.ver];
      const filas = c.items.map((i) => `<tr><td>${esc(i.producto)}</td><td class="text-right">${i.cantidad}</td>
        <td class="text-right">${cop(i.costoUnitario)}</td><td class="text-right">${cop(i.subtotal)}</td></tr>`).join('');
      abrirModal(`Compra #${c.id}`, `<p class="mb-3 text-slate-600">${esc(c.proveedorNombre)} · ${fechaHora(c.fecha)}</p>
        <table class="tabla"><thead><tr><th>Producto</th><th class="text-right">Cant.</th><th class="text-right">Costo</th><th class="text-right">Subtotal</th></tr></thead>
        <tbody>${filas}</tbody><tfoot><tr><td colspan="3" class="text-right font-semibold">Total</td>
        <td class="text-right font-bold text-green-600">${cop(c.total)}</td></tr></tfoot></table>`);
    });
  }

  async function nuevaCompra() {
    const [proveedores, productos] = await Promise.all([api('/proveedores'), api('/inventario/productos')]);
    if (!proveedores.length) return toast('Registre un proveedor primero.', 'error');
    if (!productos.length) return toast('Registre un producto primero.', 'error');
    const cont = document.createElement('div');
    cont.className = 'space-y-4';
    cont.innerHTML = `
      <div><label class="etiqueta" for="c-prov">Proveedor *</label>
        <select class="campo" id="c-prov">${proveedores.map((p) => `<option value="${p.id}">${esc(p.nombre)}</option>`).join('')}</select></div>
      <div><div class="flex items-center justify-between mb-2"><span class="etiqueta">Productos *</span>
        <button type="button" class="btn btn-sm btn-suave" id="c-add">➕ Agregar ítem</button></div>
        <div id="c-items" class="space-y-2"></div></div>
      <p class="text-right text-lg">Total: <strong id="c-total" class="text-green-600">$0</strong></p>
      <p class="error hidden text-sm text-red-700 bg-red-50 border border-red-200 rounded-lg p-2" role="alert"></p>
      <div class="flex justify-end gap-2"><button class="btn btn-suave" id="c-cancel">Cancelar</button>
        <button class="btn btn-primario" id="c-guardar">Registrar compra</button></div>`;
    abrirModal('Nueva compra', cont, 'max-w-2xl');

    const cItems = cont.querySelector('#c-items'), totalEl = cont.querySelector('#c-total');
    const opciones = productos.map((p) => `<option value="${p.id}">${esc(p.nombre)}</option>`).join('');
    function agregar() {
      const fila = document.createElement('div');
      fila.className = 'flex gap-2 items-center';
      fila.innerHTML = `<select class="campo it-prod">${opciones}</select>
        <input type="number" min="1" value="1" class="campo it-cant w-24" placeholder="Cant." aria-label="Cantidad">
        <input type="number" min="0" value="0" class="campo it-costo w-32" placeholder="Costo" aria-label="Costo unitario">
        <button type="button" class="btn btn-sm btn-peligro it-quitar" aria-label="Quitar">✕</button>`;
      cItems.appendChild(fila);
      fila.querySelector('.it-quitar').onclick = () => { fila.remove(); calcular(); };
      fila.querySelectorAll('input').forEach((i) => i.oninput = calcular);
      calcular();
    }
    function calcular() {
      let total = 0;
      cItems.querySelectorAll('.flex').forEach((f) => total += (Number(f.querySelector('.it-cant').value) || 0) * (Number(f.querySelector('.it-costo').value) || 0));
      totalEl.textContent = cop(total);
    }
    cont.querySelector('#c-add').onclick = agregar;
    agregar();
    cont.querySelector('#c-cancel').onclick = cerrarModal;
    cont.querySelector('#c-guardar').onclick = async () => {
      const err = cont.querySelector('.error');
      err.classList.add('hidden');
      const items = [...cItems.querySelectorAll('.flex')].map((f) => ({
        productoId: Number(f.querySelector('.it-prod').value),
        cantidad: Number(f.querySelector('.it-cant').value),
        costoUnitario: Number(f.querySelector('.it-costo').value)
      }));
      if (!items.length || items.some((i) => i.cantidad < 1)) {
        err.textContent = 'Agregue al menos un producto con cantidad válida.';
        err.classList.remove('hidden');
        return;
      }
      try {
        await api('/inventario/compras', { method: 'POST', body: { proveedorId: Number(cont.querySelector('#c-prov').value), items } });
        cerrarModal();
        toast('Compra registrada. Stock actualizado.');
        cargar();
      } catch (e) {
        err.textContent = e.message;
        err.classList.remove('hidden');
      }
    };
  }
};
