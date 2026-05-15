package _DAM.Cine_V2.modelo;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = { "entradas", "usuario" })
@ToString(exclude = { "entradas", "usuario" })
@EntityListeners(AuditingEntityListener.class) // 🔍 Habilita la auditoría automática
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fecha;
    private double importeTotal;
    private String metodoPago;
    private String estado;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Entrada> entradas = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // 🔍 AUDITORÍA — Se rellena automáticamente con el email del usuario que creó la venta
    @CreatedBy
    @Column(updatable = false)
    private String creadoPor;

    // 🔍 AUDITORÍA — Se rellena automáticamente con la fecha de creación
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime creadoEn;
}
