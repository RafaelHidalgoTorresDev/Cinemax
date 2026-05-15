package _DAM.Cine_V2.dto.pelicula;

import java.util.Set;

public record PeliculaOutputDTO(
        Long id,
        String titulo,
        int duracion,
        int edadMinima,
        String posterUrl,
        String trailerUrl,
        String sinopsis,
        String genero,
        double puntuacion,
        Long directorId,
        Set<Long> actorIds) {
}
