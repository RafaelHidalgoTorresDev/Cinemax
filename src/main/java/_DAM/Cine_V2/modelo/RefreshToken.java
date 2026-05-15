package _DAM.Cine_V2.modelo;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Entidad RefreshToken — Almacena los tokens de refresco en la BD.
 * Cada usuario tiene un solo RefreshToken activo a la vez.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant fechaExpiracion;

    @OneToOne
    @JoinColumn(name = "usuario_id", referencedColumnName = "id")
    private Usuario usuario;
}
