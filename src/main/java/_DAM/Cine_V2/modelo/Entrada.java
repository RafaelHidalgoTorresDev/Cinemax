package _DAM.Cine_V2.modelo;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = { "funcion", "venta" })
@ToString(exclude = { "funcion", "venta" })
@EntityListeners(AuditingEntityListener.class) // 🔍 Habilita la auditoría automática
public class Entrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo;
    private int fila;
    private int asiento;

    @Enumerated(EnumType.STRING)
    private EstadoEntrada estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funcion_id")
    private Funcion funcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id")
    private Venta venta;

    // 🔍 AUDITORÍA — Se rellena automáticamente con el email del usuario que creó la entrada
    @CreatedBy
    @Column(updatable = false)
    private String creadoPor;

    // 🔍 AUDITORÍA — Se rellena automáticamente con la fecha de creación
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime creadoEn;
}
