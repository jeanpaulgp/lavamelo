package com.lavamelo.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;

/**
 * Protege /api/**: 401 si no hay sesión, 403 si el rol no está permitido por {@link RequiereRol}.
 * Deja la sesión disponible como atributo de la petición ("sesion").
 */
public class RolInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws IOException {
        if (!(handler instanceof HandlerMethod metodo)) return true;

        // La respuesta de la API nunca debe cachearse: evita bucles de redirección por estado de sesión obsoleto.
        res.setHeader("Cache-Control", "no-store");

        HttpSession http = req.getSession(false);
        Sesion sesion = http == null ? null : (Sesion) http.getAttribute(Sesion.ATRIBUTO);
        if (sesion == null) {
            return rechazar(res, HttpServletResponse.SC_UNAUTHORIZED, "Debe iniciar sesión");
        }

        RequiereRol regla = metodo.getMethodAnnotation(RequiereRol.class);
        if (regla == null) regla = metodo.getBeanType().getAnnotation(RequiereRol.class);
        if (regla != null && Arrays.stream(regla.value()).noneMatch(sesion::es)) {
            return rechazar(res, HttpServletResponse.SC_FORBIDDEN, "No tiene permiso para esta acción");
        }

        req.setAttribute(Sesion.ATRIBUTO, sesion);
        return true;
    }

    private static boolean rechazar(HttpServletResponse res, int status, String mensaje) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"detail\":\"" + mensaje + "\"}");
        return false;
    }
}
