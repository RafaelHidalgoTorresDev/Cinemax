package _DAM.Cine_V2.controlador;

import _DAM.Cine_V2.dto.venta.VentaInputDTO;
import _DAM.Cine_V2.dto.venta.VentaOutputDTO;
import _DAM.Cine_V2.servicio.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;

    /**
     * 🔒 ADMIN — Ve TODAS las ventas de cualquier usuario.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<VentaOutputDTO>> findAll() {
        return ResponseEntity.ok(ventaService.findAll());
    }

    /**
     * 🔒 ADMIN o el propio USUARIO — Ve una venta concreta.
     * La lógica de pertenencia se verifica en el servicio.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'USUARIO')")
    public ResponseEntity<VentaOutputDTO> findById(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ventaService.findByIdSecured(id, auth));
    }

    /**
     * 🔒 USUARIO — Un usuario compra entradas (crea una venta).
     * ADMIN también puede crear ventas.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'USUARIO')")
    public ResponseEntity<VentaOutputDTO> create(@Valid @RequestBody VentaInputDTO ventaDTO, Authentication auth) {
        return new ResponseEntity<>(ventaService.saveSecured(ventaDTO, auth), HttpStatus.CREATED);
    }

    /**
     * 🔒 ADMIN — Actualizar una venta.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<VentaOutputDTO> update(@PathVariable Long id, @Valid @RequestBody VentaInputDTO ventaDTO) {
        return ResponseEntity.ok(ventaService.update(id, ventaDTO));
    }

    /**
     * 🔒 ADMIN o el propio USUARIO (solo sus ventas) — Cancelar/eliminar una venta.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'USUARIO')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        ventaService.deleteByIdSecured(id, auth);
        return ResponseEntity.noContent().build();
    }

    /**
     * 🔒 USUARIO — Ver solo MIS ventas.
     */
    @GetMapping("/mis-ventas")
    @PreAuthorize("hasAnyRole('USUARIO', 'ADMINISTRADOR')")
    public ResponseEntity<List<VentaOutputDTO>> findMyVentas(Authentication auth) {
        return ResponseEntity.ok(ventaService.findByUserEmail(auth.getName()));
    }
}
