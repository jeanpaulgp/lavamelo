package com.lavamelo.web;

import com.lavamelo.auth.RequiereRol;
import com.lavamelo.model.Modelos.Producto;
import com.lavamelo.service.Dtos.*;
import com.lavamelo.service.InventarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.lavamelo.model.Modelos.Rol.ADMIN;

/** Productos, stock, salidas/ajustes y compras. Solo Administrador. */
@RestController
@RequestMapping("/api/inventario")
@RequiereRol(ADMIN)
public class InventarioController {

    private final InventarioService servicio;

    public InventarioController(InventarioService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/productos")
    public List<Producto> productos() {
        return servicio.listarProductos();
    }

    @GetMapping("/productos/stock-bajo")
    public List<Producto> stockBajo() {
        return servicio.stockBajo();
    }

    @GetMapping("/productos/{id}")
    public Producto producto(@PathVariable long id) {
        return servicio.obtenerProducto(id);
    }

    @PostMapping("/productos")
    @ResponseStatus(HttpStatus.CREATED)
    public Producto crearProducto(@Valid @RequestBody ProductoEntrada e) {
        return servicio.crearProducto(e);
    }

    @PutMapping("/productos/{id}")
    public Producto actualizarProducto(@PathVariable long id, @Valid @RequestBody ProductoEntrada e) {
        return servicio.actualizarProducto(id, e);
    }

    @DeleteMapping("/productos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarProducto(@PathVariable long id) {
        servicio.eliminarProducto(id);
    }

    @GetMapping("/movimientos")
    public List<MovimientoVista> movimientos(@RequestParam(required = false) Long productoId) {
        return servicio.movimientos(productoId);
    }

    /** Salida (uso/consumo) o ajuste con motivo. */
    @PostMapping("/ajustes")
    @ResponseStatus(HttpStatus.CREATED)
    public Producto ajuste(@Valid @RequestBody AjusteEntrada e) {
        return servicio.registrarAjuste(e);
    }

    @GetMapping("/compras")
    public List<CompraVista> compras() {
        return servicio.listarCompras();
    }

    @GetMapping("/compras/{id}")
    public CompraVista compra(@PathVariable long id) {
        return servicio.obtenerCompra(id);
    }

    /** Entrada de inventario por compra a proveedor. */
    @PostMapping("/compras")
    @ResponseStatus(HttpStatus.CREATED)
    public CompraVista registrarCompra(@Valid @RequestBody CompraEntrada e) {
        return servicio.registrarCompra(e);
    }
}
