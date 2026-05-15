package _DAM.Cine_V2.controlador;

import _DAM.Cine_V2.dto.sala.SalaInputDTO;
import _DAM.Cine_V2.dto.sala.SalaOutputDTO;
import _DAM.Cine_V2.servicio.SalaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/salas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')") // 🔒 TODO el controlador es solo para ADMIN
public class SalaController {

    private final SalaService salaService;

    @GetMapping
    public ResponseEntity<List<SalaOutputDTO>> findAll() {
        return ResponseEntity.ok(salaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalaOutputDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(salaService.findById(id));
    }

    @PostMapping
    public ResponseEntity<SalaOutputDTO> create(@Valid @RequestBody SalaInputDTO salaDTO) {
        return new ResponseEntity<>(salaService.save(salaDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalaOutputDTO> update(@PathVariable Long id, @Valid @RequestBody SalaInputDTO salaDTO) {
        return ResponseEntity.ok(salaService.update(id, salaDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        salaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
