package _DAM.Cine_V2.controlador;

import _DAM.Cine_V2.dto.funcion.FuncionInputDTO;
import _DAM.Cine_V2.dto.funcion.FuncionOutputDTO;
import _DAM.Cine_V2.servicio.FuncionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/funciones")
@RequiredArgsConstructor
public class FuncionController {

    private final FuncionService funcionService;

    // 🔓 PÚBLICO — Consulta de horarios/sesiones
    @GetMapping
    public ResponseEntity<List<FuncionOutputDTO>> findAll() {
        return ResponseEntity.ok(funcionService.findAll());
    }

    // 🔓 PÚBLICO — Ver detalle de una función
    @GetMapping("/{id}")
    public ResponseEntity<FuncionOutputDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(funcionService.findById(id));
    }

    // 🔒 SOLO ADMIN — Crear función/sesión
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<FuncionOutputDTO> create(@Valid @RequestBody FuncionInputDTO funcionDTO) {
        return new ResponseEntity<>(funcionService.save(funcionDTO), HttpStatus.CREATED);
    }

    // 🔒 SOLO ADMIN — Editar función/sesión
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<FuncionOutputDTO> update(@PathVariable Long id,
            @Valid @RequestBody FuncionInputDTO funcionDTO) {
        return ResponseEntity.ok(funcionService.update(id, funcionDTO));
    }

    // 🔒 SOLO ADMIN — Eliminar función/sesión
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        funcionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // 🔓 PÚBLICO — Obtener butacas ocupadas de una función
    @GetMapping("/{id}/asientos-ocupados")
    public ResponseEntity<List<java.util.Map<String, Integer>>> getAsientosOcupados(@PathVariable Long id) {
        return ResponseEntity.ok(funcionService.getAsientosOcupados(id));
    }
}
