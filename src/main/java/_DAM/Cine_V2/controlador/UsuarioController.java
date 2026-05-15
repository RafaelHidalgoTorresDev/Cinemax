package _DAM.Cine_V2.controlador;

import _DAM.Cine_V2.dto.usuario.UsuarioInputDTO;
import _DAM.Cine_V2.dto.usuario.UsuarioOutputDTO;
import _DAM.Cine_V2.servicio.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * 🔒 ADMIN — Ver todos los usuarios.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<UsuarioOutputDTO>> findAll() {
        return ResponseEntity.ok(usuarioService.findAll());
    }

    /**
     * 🔒 USUARIO — Ver mi propio perfil.
     * Endpoint especial que usa el email del token JWT.
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'USUARIO')")
    public ResponseEntity<UsuarioOutputDTO> getMyProfile(Authentication auth) {
        return ResponseEntity.ok(usuarioService.findByEmail(auth.getName()));
    }

    /**
     * 🔒 USUARIO — Actualizar mi propio perfil (email, password).
     */
    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'USUARIO')")
    public ResponseEntity<UsuarioOutputDTO> updateMyProfile(Authentication auth,
            @Valid @RequestBody UsuarioInputDTO usuarioDTO) {
        return ResponseEntity.ok(usuarioService.updateMe(auth.getName(), usuarioDTO));
    }

    /**
     * 🔒 ADMIN — Ver usuario por ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UsuarioOutputDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.findById(id));
    }

    /**
     * 🔒 ADMIN — Crear usuario (diferente del registro público).
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UsuarioOutputDTO> create(@Valid @RequestBody UsuarioInputDTO usuarioDTO) {
        return new ResponseEntity<>(usuarioService.save(usuarioDTO), HttpStatus.CREATED);
    }

    /**
     * 🔒 ADMIN — Editar cualquier usuario.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UsuarioOutputDTO> update(@PathVariable Long id,
            @Valid @RequestBody UsuarioInputDTO usuarioDTO) {
        return ResponseEntity.ok(usuarioService.update(id, usuarioDTO));
    }

    /**
     * 🔒 ADMIN — Eliminar usuario.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        usuarioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
