package com.lavamelo.service;

import com.lavamelo.auth.Contrasenas;
import com.lavamelo.auth.Sesion;
import com.lavamelo.model.Modelos.*;
import com.lavamelo.repository.Repositorio;
import com.lavamelo.service.Dtos.EmpleadoEntrada;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmpleadoService {

    private final Repositorio repo;

    public EmpleadoService(Repositorio repo) {
        this.repo = repo;
    }

    public List<Empleado> listar() {
        return repo.listarEmpleados();
    }

    public List<Empleado> operariosActivos() {
        return repo.listarEmpleados().stream().filter(e -> e.rol() == Rol.OPERARIO && e.activo()).toList();
    }

    public Empleado obtener(long id) {
        return repo.obtenerEmpleado(id).orElseThrow(() -> ApiException.noEncontrado("Empleado"));
    }

    public synchronized Empleado crear(EmpleadoEntrada e) {
        String usuario = e.usuario().trim().toLowerCase();
        validarUsuarioLibre(null, usuario);
        String password = validarPassword(e.password(), true);
        return repo.crearEmpleado(new Empleado(null, e.documento().trim(), e.nombre().trim(), Util.limpio(e.telefono()),
                usuario, Contrasenas.hash(password), e.rol(), e.activo() == null || e.activo()));
    }

    public synchronized Empleado actualizar(Sesion sesion, long id, EmpleadoEntrada e) {
        Empleado actual = obtener(id);
        String usuario = e.usuario().trim().toLowerCase();
        validarUsuarioLibre(id, usuario);
        boolean activo = e.activo() == null ? actual.activo() : e.activo();
        if (id == sesion.empleadoId() && (!activo || e.rol() != actual.rol())) {
            throw ApiException.invalido("No puede desactivarse ni cambiar su propio rol");
        }
        String password = validarPassword(e.password(), false);
        String hash = password == null ? actual.passwordHash() : Contrasenas.hash(password);
        return repo.actualizarEmpleado(new Empleado(id, e.documento().trim(), e.nombre().trim(), Util.limpio(e.telefono()),
                usuario, hash, e.rol(), activo));
    }

    public void eliminar(Sesion sesion, long id) {
        obtener(id);
        if (id == sesion.empleadoId()) throw ApiException.invalido("No puede eliminar su propio usuario");
        if (!repo.listarOrdenes(new FiltroOrdenes(null, id, null, null, null, null)).isEmpty()) {
            throw ApiException.conflicto("El empleado tiene órdenes registradas; desactívelo en lugar de eliminarlo");
        }
        repo.eliminarEmpleado(id);
    }

    private void validarUsuarioLibre(Long id, String usuario) {
        repo.buscarEmpleadoPorUsuario(usuario).filter(x -> !x.id().equals(id)).ifPresent(x -> {
            throw ApiException.conflicto("El usuario \"" + usuario + "\" ya existe");
        });
    }

    private static String validarPassword(String password, boolean obligatoria) {
        String p = password == null || password.isBlank() ? null : password;
        if (p == null && obligatoria) throw ApiException.invalido("La contraseña es obligatoria");
        if (p != null && p.length() < 6) throw ApiException.invalido("La contraseña debe tener al menos 6 caracteres");
        return p;
    }
}
