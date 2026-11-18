package com.lavamelo.service;

import com.lavamelo.model.Modelos.*;
import com.lavamelo.repository.Repositorio;
import com.lavamelo.service.Dtos.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Productos, movimientos de inventario (salidas/ajustes) y compras a proveedores (entradas). */
@Service
public class InventarioService {

    private final Repositorio repo;

    public InventarioService(Repositorio repo) {
        this.repo = repo;
    }

    // ------------------------------------------------------------ Productos

    public List<Producto> listarProductos() {
        return repo.listarProductos();
    }

    public List<Producto> stockBajo() {
        return repo.productosStockBajo();
    }

    public Producto obtenerProducto(long id) {
        return repo.obtenerProducto(id).orElseThrow(() -> ApiException.noEncontrado("Producto"));
    }

    public synchronized Producto crearProducto(ProductoEntrada e) {
        int inicial = e.stockInicial() == null ? 0 : e.stockInicial();
        Producto p = repo.crearProducto(new Producto(null, e.nombre().trim(), e.unidad().trim(), inicial, e.stockMinimo()));
        if (inicial > 0) {
            repo.crearMovimiento(new Inventario(null, p.id(), LocalDateTime.now(), TipoMovimiento.AJUSTE, inicial,
                    "Inventario inicial", null));
        }
        return p;
    }

    /** El stock no se edita aquí: cambia solo con compras, salidas o ajustes. */
    public synchronized Producto actualizarProducto(long id, ProductoEntrada e) {
        Producto actual = obtenerProducto(id);
        return repo.actualizarProducto(new Producto(id, e.nombre().trim(), e.unidad().trim(), actual.stock(), e.stockMinimo()));
    }

    public synchronized void eliminarProducto(long id) {
        obtenerProducto(id);
        if (!repo.listarMovimientos(id).isEmpty()) {
            throw ApiException.conflicto("No se puede eliminar: el producto tiene movimientos de inventario");
        }
        repo.eliminarProducto(id);
    }

    // ------------------------------------------------------------ Movimientos

    public List<MovimientoVista> movimientos(Long productoId) {
        return repo.listarMovimientos(productoId).stream().map(m -> new MovimientoVista(m.id(), m.fecha(), m.productoId(),
                repo.obtenerProducto(m.productoId()).map(Producto::nombre).orElse("Producto #" + m.productoId()),
                m.tipo(), m.cantidad(), m.motivo(), m.compraId())).toList();
    }

    /** SALIDA: descuenta una cantidad positiva. AJUSTE: suma o resta según el signo. Nunca deja stock negativo. */
    public synchronized Producto registrarAjuste(AjusteEntrada e) {
        Producto p = obtenerProducto(e.productoId());
        int delta = switch (e.tipo()) {
            case SALIDA -> {
                if (e.cantidad() <= 0) throw ApiException.invalido("La cantidad de salida debe ser mayor que 0");
                yield -e.cantidad();
            }
            case AJUSTE -> {
                if (e.cantidad() == 0) throw ApiException.invalido("La cantidad del ajuste no puede ser 0");
                yield e.cantidad();
            }
            case ENTRADA -> throw ApiException.invalido("Las entradas se registran mediante una compra");
        };
        int nuevo = p.stock() + delta;
        if (nuevo < 0) {
            throw ApiException.invalido("Stock insuficiente: hay " + p.stock() + " " + p.unidad() + " de " + p.nombre());
        }
        repo.crearMovimiento(new Inventario(null, p.id(), LocalDateTime.now(), e.tipo(), delta, e.motivo().trim(), null));
        return repo.actualizarProducto(p.conStock(nuevo));
    }

    // ------------------------------------------------------------ Compras

    public List<CompraVista> listarCompras() {
        return repo.listarCompras().stream().map(this::vista).toList();
    }

    public CompraVista obtenerCompra(long id) {
        return vista(repo.obtenerCompra(id).orElseThrow(() -> ApiException.noEncontrado("Compra")));
    }

    /** Crea la compra con sus detalles, suma stock y registra un movimiento ENTRADA por producto. */
    public synchronized CompraVista registrarCompra(CompraEntrada e) {
        repo.obtenerProveedor(e.proveedorId()).orElseThrow(() -> ApiException.noEncontrado("Proveedor"));
        List<Producto> productos = new ArrayList<>();
        long total = 0;
        for (ItemCompraEntrada it : e.items()) {
            productos.add(obtenerProducto(it.productoId()));
            total += it.cantidad() * it.costoUnitario();
        }
        LocalDateTime ahora = LocalDateTime.now();
        Compra compra = repo.crearCompra(new Compra(null, e.proveedorId(), ahora, total));
        for (int i = 0; i < e.items().size(); i++) {
            ItemCompraEntrada it = e.items().get(i);
            repo.crearDetalleCompra(new DetalleCompra(null, compra.id(), it.productoId(), it.cantidad(), it.costoUnitario()));
            repo.crearMovimiento(new Inventario(null, it.productoId(), ahora, TipoMovimiento.ENTRADA, it.cantidad(),
                    "Compra #" + compra.id(), compra.id()));
            // Se relee por si el mismo producto aparece dos veces en la compra
            Producto p = obtenerProducto(productos.get(i).id());
            repo.actualizarProducto(p.conStock(p.stock() + it.cantidad()));
        }
        return vista(compra);
    }

    private CompraVista vista(Compra c) {
        List<ItemCompraVista> items = repo.listarDetallesCompra(c.id()).stream().map(d -> new ItemCompraVista(
                d.productoId(), repo.obtenerProducto(d.productoId()).map(Producto::nombre).orElse("Producto #" + d.productoId()),
                d.cantidad(), d.costoUnitario(), d.cantidad() * d.costoUnitario())).toList();
        String proveedor = repo.obtenerProveedor(c.proveedorId()).map(Proveedor::nombre).orElse(null);
        return new CompraVista(c.id(), c.fecha(), c.proveedorId(), proveedor, c.total(), items);
    }
}
