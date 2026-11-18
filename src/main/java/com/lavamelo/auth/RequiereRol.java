package com.lavamelo.auth;

import com.lavamelo.model.Modelos.Rol;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Roles permitidos para un controlador o endpoint. La del método tiene prioridad sobre la de la clase.
 * Sin anotación: basta con tener sesión iniciada.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiereRol {
    Rol[] value();
}
