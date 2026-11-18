package com.lavamelo.auth;

import com.lavamelo.model.Modelos.Rol;

import java.io.Serializable;

/** Usuario autenticado guardado en la sesión HTTP. */
public record Sesion(long empleadoId, String nombre, String usuario, Rol rol) implements Serializable {

    public static final String ATRIBUTO = "sesion";

    public boolean es(Rol r) {
        return rol == r;
    }
}
