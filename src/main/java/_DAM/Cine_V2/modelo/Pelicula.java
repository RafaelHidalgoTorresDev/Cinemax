package _DAM.Cine_V2.modelo;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = { "director", "actores", "funciones" })
@ToString(exclude = { "director", "actores", "funciones" })
public class Pelicula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private int duracion;
    private int edadMinima;

    // 🆕 Campos nuevos para enriquecer la cartelera
    @Column(length = 500)
    private String posterUrl;

    @Column(length = 2000)
    private String sinopsis;

    private String genero;
    private double puntuacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "director_id")
    private Director director;

    @ManyToMany
    @JoinTable(name = "pelicula_actor", joinColumns = @JoinColumn(name = "pelicula_id"), inverseJoinColumns = @JoinColumn(name = "actor_id"))
    private Set<Actor> actores = new HashSet<>();

    @OneToMany(mappedBy = "pelicula", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Funcion> funciones = new HashSet<>();
}
