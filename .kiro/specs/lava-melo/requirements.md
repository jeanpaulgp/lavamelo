# Requisitos — Lava-Melo

Sistema web para gestionar un lavadero de vehículos. Un solo proceso sirve la API (`/api/*`), el frontend (`/`) y la documentación (`/docs`).

## Roles
- R1. **Administrador**: acceso total.
- R2. **Recepcionista/Cajero**: clientes, vehículos, historiales, crear órdenes, pagos y recibos. Sin empleados, inventario ni reportes.
- R3. **Operario**: solo sus órdenes asignadas; ver detalle y pasar a *En proceso* / *Listo*.
- R4. Los permisos se validan en el backend (401 sin sesión, 403 sin permiso) y el frontend oculta lo no permitido.

## Funcionales
1. **Login** con usuario/contraseña (hash PBKDF2) y sesión por cookie.
2. **Clientes**: CRUD, búsqueda por nombre/documento/teléfono, historial de visitas.
3. **Vehículos**: placa única, modelo, categoría (MOTO, AUTOMOVIL, CAMIONETA), cliente dueño. Historial: visitas, último servicio, total acumulado.
4. **Servicios**: catálogo con duración (min) y precio por categoría.
5. **Órdenes**: cliente + vehículo + servicio(s) + operario. Estados *En espera → En proceso → Listo → Finalizado*, solo hacia adelante y de a un paso. Total = suma de precios según categoría del vehículo.
6. **Pagos**: solo órdenes *Listo*; métodos Efectivo, Nequi, Tarjeta; una orden se paga una sola vez; el pago la pasa a *Finalizado*; ingresos del día; recibo HTML imprimible.
7. **Inventario**: productos con stock y stock mínimo; entradas por compras; salidas/ajustes con motivo; alerta de stock bajo (stock ≤ mínimo) en dashboard y lista.
8. **Empleados y proveedores**: CRUD (solo Administrador).
9. **Reportes** (Administrador): vehículos atendidos hoy; ingresos de la semana; servicio más solicitado; clientes más frecuentes; servicios por operario; insumos por agotarse; clientes sin regresar en 60 días; día del mes con mayores ventas; ventas por método de pago. Rango de fechas donde aplique.
10. **Dashboard** por rol (Admin: KPIs + alertas; Recepción: órdenes activas; Operario: mis órdenes).

## Datos
- D1. Todo acceso a datos pasa por una interfaz de repositorio; implementación en memoria por defecto y esqueleto SQL. Selección con `REPO=memory|sql`.
- D2. Semilla con el caso Juan Pérez / ABC123 Renault Logan: 17 visitas × $30.000 = $510.000, operario Carlos M., último pago Nequi, última orden Finalizada; más datos suficientes para todos los reportes.

## No funcionales
- Español, formato COP y fechas locales; tema visual agua/limpieza; responsive básico.
- Arranque con un solo comando sin configuración.
- Pocas pruebas: transiciones de estado, total por categoría, permisos por rol, pago único.
- Fuera de alcance: Docker, ORM, migraciones, colas, websockets, notificaciones, pagos en línea, facturación electrónica, multi-sucursal, SPA frameworks.
