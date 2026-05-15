package _DAM.Cine_V2.controlador;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class PruebaController {

    // 🔓 PÚBLICO — Endpoint de prueba sin autenticación
    @GetMapping("/ping")
    public String ping() {
        return "🏓 Pong! El servidor está funcionando.";
    }

    // 🔒 ADMIN — Endpoint de prueba solo para administradores
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String holaJefe() { return "¡Hola Jefe! Eres ADMINISTRADOR."; }

    // 🔒 USUARIO — Endpoint de prueba para usuarios
    @GetMapping("/user")
    @PreAuthorize("hasRole('USUARIO')")
    public String holaUsuario(Authentication auth) {
        return "¡Hola " + auth.getName() + "! Eres USUARIO.";
    }
}