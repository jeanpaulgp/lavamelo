package com.lavamelo.service;

import com.lavamelo.auth.Contrasenas;
import com.lavamelo.auth.Sesion;
import com.lavamelo.model.Modelos.Empleado;
import com.lavamelo.repository.Repositorio;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final Repositorio repo;

    public AuthService(Repositorio repo) {
        this.repo = repo;
    }

    public Sesion login(String usuario, String password) {
        Empleado e = repo.buscarEmpleadoPorUsuario(usuario.trim().toLowerCase())
                .filter(Empleado::activo)
                .filter(x -> Contrasenas.verificar(password, x.passwordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos"));
        return new Sesion(e.id(), e.nombre(), e.usuario(), e.rol());
    }
}
