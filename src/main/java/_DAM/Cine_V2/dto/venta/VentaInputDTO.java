package _DAM.Cine_V2.dto.venta;

import _DAM.Cine_V2.dto.entrada.EntradaInputDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record VentaInputDTO(
        Long usuarioId,
        @NotBlank(message = "El método de pago es obligatorio") String metodoPago,
        Set<EntradaInputDTO> entradas) {
}
