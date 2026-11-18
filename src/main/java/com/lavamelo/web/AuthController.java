package com.lavamelo.web;

import com.lavamelo.auth.Sesion;
import com.lavamelo.service.AuthService;
import com.lavamelo.service.Dtos.LoginEntrada;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/login")
    public Sesion login(@Valid @RequestBody LoginEntrada e, HttpServletRequest req) {
        Sesion sesion = auth.login(e.usuario(), e.password());
        HttpSession anterior = req.getSession(false);
        if (anterior != null) anterior.invalidate(); // evita fijación de sesión
        req.getSession(true).setAttribute(Sesion.ATRIBUTO, sesion);
        return sesion;
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s != null) s.invalidate();
        return Map.of("detail", "Sesión cerrada");
    }

    @GetMapping("/me")
    public Sesion yo(@Parameter(hidden = true) @RequestAttribute(Sesion.ATRIBUTO) Sesion sesion) {
        return sesion;
    }
}
