package _DAM.Cine_V2.controlador;

import _DAM.Cine_V2.dto.pelicula.PeliculaInputDTO;
import _DAM.Cine_V2.dto.pelicula.PeliculaOutputDTO;
import _DAM.Cine_V2.servicio.PeliculaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/peliculas")
@RequiredArgsConstructor
public class PeliculaController {

    private final PeliculaService peliculaService;

    // 🔓 PÚBLICO — Consulta de cartelera (cualquier persona puede ver las películas)
    @GetMapping
    public ResponseEntity<List<PeliculaOutputDTO>> findAll() {
        return ResponseEntity.ok(peliculaService.findAll());
    }

    // 🔓 PÚBLICO — Ver detalle de una película
    @GetMapping("/{id}")
    public ResponseEntity<PeliculaOutputDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(peliculaService.findById(id));
    }

    // 🔒 SOLO ADMIN — Crear película
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<PeliculaOutputDTO> create(@Valid @RequestBody PeliculaInputDTO peliculaDTO) {
        return new ResponseEntity<>(peliculaService.save(peliculaDTO), HttpStatus.CREATED);
    }

    // 🔒 SOLO ADMIN — Editar película
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<PeliculaOutputDTO> update(@PathVariable Long id,
            @Valid @RequestBody PeliculaInputDTO peliculaDTO) {
        return ResponseEntity.ok(peliculaService.update(id, peliculaDTO));
    }

    // 🔒 SOLO ADMIN — Eliminar película
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        peliculaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
