package _DAM.Cine_V2.controlador;

import _DAM.Cine_V2.dto.actor.ActorInputDTO;
import _DAM.Cine_V2.dto.actor.ActorOutputDTO;
import _DAM.Cine_V2.servicio.ActorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/actores")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')") // 🔒 TODO el controlador es solo para ADMIN
public class ActorController {

    private final ActorService actorService;

    @GetMapping
    public ResponseEntity<List<ActorOutputDTO>> findAll() {
        return ResponseEntity.ok(actorService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActorOutputDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(actorService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ActorOutputDTO> create(@Valid @RequestBody ActorInputDTO actorDTO) {
        return new ResponseEntity<>(actorService.save(actorDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActorOutputDTO> update(@PathVariable Long id, @Valid @RequestBody ActorInputDTO actorDTO) {
        return ResponseEntity.ok(actorService.update(id, actorDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        actorService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
