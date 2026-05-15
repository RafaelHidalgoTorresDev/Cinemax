package _DAM.Cine_V2.controlador;

import _DAM.Cine_V2.dto.LoginRequestDTO;
import _DAM.Cine_V2.dto.LoginResponseDTO;
import _DAM.Cine_V2.dto.RefreshTokenRequestDTO;
import _DAM.Cine_V2.dto.usuario.UsuarioInputDTO;
import _DAM.Cine_V2.servicio.UsuarioService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;

    /**
     * 🔹 ENDPOINT DE REGISTRO
     * Registra un nuevo usuario y devuelve directamente los tokens (Access + Refresh).
     * Así el usuario queda logueado automáticamente tras registrarse.
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponseDTO> register(@RequestBody UsuarioInputDTO req) {
        LoginResponseDTO respuesta = usuarioService.registerAndLogin(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /**
     * 🔹 ENDPOINT DE LOGIN
     * Devuelve Access Token + Refresh Token.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO req) {
        LoginResponseDTO respuesta = usuarioService.login(req);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * 🔹 ENDPOINT DE REFRESH TOKEN
     * Recibe un Refresh Token válido y devuelve un nuevo Access Token.
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(@RequestBody RefreshTokenRequestDTO req) {
        LoginResponseDTO respuesta = usuarioService.refreshAccessToken(req.refreshToken());
        return ResponseEntity.ok(respuesta);
    }

    /**
     * 🔹 ENDPOINT DE LOGOUT
     * Elimina el Refresh Token de la BD para cerrar sesión.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequestDTO req) {
        usuarioService.logout(req.refreshToken());
        return ResponseEntity.noContent().build();
    }
}