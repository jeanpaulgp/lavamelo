// Inventario: productos, stock, salidas/ajustes y alertas de stock bajo.
Views.inventario = async function () {
  const acciones = `<button class="btn btn-suave" id="mov">📜 Movimientos</button>
    <button class="btn btn-azul" id="ajuste">➖ Salida / Ajuste</button>
    <button class="btn btn-primario" id="nuevo-prod">➕ Nuevo producto</button>`;
  document.getElementById('vista').innerHTML = encabezado('📦', 'Inventario', 'Productos, existencias y alertas de stock bajo.', acciones) +
    `<div id="alerta-inv"></div>
     <div class="tarjeta overflow-hidden"><div class="overflow-x-auto"><table class="tabla">
      <thead><tr><th>Producto</th><th>Unidad</th><th class="text-right">Stock</th><th class="text-right">Mínimo</th>
      <th>Estado</th><th class="text-right">Acciones</th></tr></thead>
      <tbody id="cuerpo-inv"></tbody></table></div></div>`;

  document.getElementById('nuevo-prod').onclick = () => formProducto();
  document.getElementById('ajuste').onclick = () => formAjuste();
  document.getElementById('mov').onclick = () => verMovimientos();
  await cargar();

  async function cargar() {
    const lista = await api('/inventario/productos');
    const bajos = lista.filter((p) => p.stock <= p.stockMinimo);
    document.getElementById('alerta-inv').innerHTML = bajos.length ?
      `<div class="alerta-stock rounded-xl p-4 mb-4">⚠️ <strong>${bajos.length}</strong> producto(s) en stock bajo: ${bajos.map((p) => esc(p.nombre)).join(', ')}.</div>` : '';
    document.getElementById('cuerpo-inv').innerHTML = lista.length ? lista.map((p) => {
      const bajo = p.stock <= p.stockMinimo;
      return `<tr class="${bajo ? 'fila-stock-bajo' : ''}"><td class="font-semibold">${esc(p.nombre)}</td>
      <td>${esc(p.unidad)}</td><td class="text-right font-bold">${p.stock}</td><td class="text-right">${p.stockMinimo}</td>
      <td>${bajo ? '<span class="estado estado-EN_ESPERA">⚠ Bajo</span>' : '<span class="estado estado-LISTO">OK</span>'}</td>
      <td class="text-right whitespace-nowrap">
        <button class="btn btn-sm btn-suave" data-edit="${p.id}">Editar</button>
        <button class="btn btn-sm btn-peligro" data-del="${p.id}">Eliminar</button></td></tr>`;
    }).join('') : vacio('No hay productos.', 6);

    const mapa = Object.fromEntries(lista.map((p) => [p.id, p]));
    document.querySelectorAll('[data-edit]').forEach((b) => b.onclick = () => formProducto(mapa[b.dataset.edit]));
    document.querySelectorAll('[data-del]').forEach((b) => b.onclick = async () => {
      if (!confirmar('¿Eliminar este producto?')) return;
      try { await api('/inventario/productos/' + b.dataset.del, { method: 'DELETE' }); toast('Producto eliminado'); cargar(); }
      catch (e) { toast(e.message, 'error'); }
    });
  }

  function formProducto(p) {
    formularioModal(p ? 'Editar producto' : 'Nuevo producto', [
      { name: 'nombre', label: 'Nombre', required: true, ancho: 'full' },
      { name: 'unidad', label: 'Unidad', required: true, placeholder: 'litro, galón, unidad…' },
      { name: 'stockMinimo', label: 'Stock mínimo', type: 'number', required: true, min: 0 },
      ...(p ? [] : [{ name: 'stockInicial', label: 'Stock inicial', type: 'number', min: 0 }])
    ], p, async (datos) => {
      if (p) await api('/inventario/productos/' + p.id, { method: 'PUT', body: datos });
      else await api('/inventario/productos', { method: 'POST', body: datos });
      toast('Producto guardado');
      cargar();
    });
  }

  async function formAjuste() {
    const productos = await api('/inventario/productos');
    formularioModal('Registrar salida o ajuste', [
      { name: 'productoId', label: 'Producto', type: 'select', required: true, ancho: 'full',
        options: productos.map((p) => ({ value: p.id, label: `${p.nombre} (stock: ${p.stock} ${p.unidad})` })) },
      { name: 'tipo', label: 'Tipo', type: 'select', required: true,
        options: [{ value: 'SALIDA', label: 'Salida (uso/consumo)' }, { value: 'AJUSTE', label: 'Ajuste (+/-)' }] },
      { name: 'cantidad', label: 'Cantidad', type: 'number', required: true,
        ayuda: 'Salida: cantidad positiva a descontar. Ajuste: use negativo para restar.' },
      { name: 'motivo', label: 'Motivo', required: true, ancho: 'full' }
    ], { tipo: 'SALIDA' }, async (datos) => {
      await api('/inventario/ajustes', { method: 'POST', body: { ...datos, productoId: Number(datos.productoId), cantidad: Number(datos.cantidad) } });
      toast('Movimiento registrado');
      cargar();
    });
  }

  async function verMovimientos() {
    const movs = await api('/inventario/movimientos');
    const filas = movs.length ? movs.slice(0, 100).map((m) => `<tr>
      <td>${fechaHora(m.fecha)}</td><td>${esc(m.producto)}</td>
      <td>${TIPOS_MOV[m.tipo]}</td><td class="text-right ${m.cantidad < 0 ? 'text-red-600' : 'text-green-600'} font-semibold">${m.cantidad > 0 ? '+' : ''}${m.cantidad}</td>
      <td>${esc(m.motivo || '')}</td></tr>`).join('') : vacio('Sin movimientos.', 5);
    abrirModal('Movimientos de inventario', `<div class="overflow-x-auto max-h-96"><table class="tabla">
      <thead><tr><th>Fecha</th><th>Producto</th><th>Tipo</th><th class="text-right">Cantidad</th><th>Motivo</th></tr></thead>
      <tbody>${filas}</tbody></table></div>`, 'max-w-3xl');
  }
};
