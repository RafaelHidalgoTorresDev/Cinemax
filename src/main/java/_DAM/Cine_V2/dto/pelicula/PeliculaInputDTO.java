package _DAM.Cine_V2.dto.pelicula;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public record PeliculaInputDTO(
        @NotBlank(message = "El título es obligatorio") String titulo,
        @Min(value = 1, message = "La duración debe ser mayor a 0") int duracion,
        @Min(value = 0, message = "La edad mínima no puede ser negativa") int edadMinima,
        String posterUrl,
        String trailerUrl,
        String sinopsis,
        String genero,
        double puntuacion,
        Long directorId,
        Set<Long> actorIds) {
}
