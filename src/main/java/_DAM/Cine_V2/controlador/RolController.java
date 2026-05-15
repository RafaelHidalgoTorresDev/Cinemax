package _DAM.Cine_V2.controlador;

import _DAM.Cine_V2.dto.rol.RolInputDTO;
import _DAM.Cine_V2.dto.rol.RolOutputDTO;
import _DAM.Cine_V2.servicio.RolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')") // 🔒 TODO el controlador es solo para ADMIN
public class RolController {

    private final RolService rolService;

    @GetMapping
    public ResponseEntity<List<RolOutputDTO>> findAll() {
        return ResponseEntity.ok(rolService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RolOutputDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(rolService.findById(id));
    }

    @PostMapping
    public ResponseEntity<RolOutputDTO> create(@Valid @RequestBody RolInputDTO rolDTO) {
        return new ResponseEntity<>(rolService.save(rolDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RolOutputDTO> update(@PathVariable Long id, @Valid @RequestBody RolInputDTO rolDTO) {
        return ResponseEntity.ok(rolService.update(id, rolDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        rolService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
