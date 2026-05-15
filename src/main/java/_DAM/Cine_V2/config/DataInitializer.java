package _DAM.Cine_V2.config;

import _DAM.Cine_V2.modelo.*;
import _DAM.Cine_V2.repositorio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Inicializador de datos — Carga películas reales, salas, funciones y usuarios al arrancar.
 * Solo carga si la BD está vacía.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final SalaRepository salaRepository;
    private final PeliculaRepository peliculaRepository;
    private final FuncionRepository funcionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // 1. ROLES
        Rol rolUsuario = rolRepository.findByNombre("USUARIO")
                .orElseGet(() -> rolRepository.save(Rol.builder().nombre("USUARIO").build()));
        Rol rolAdmin = rolRepository.findByNombre("ADMINISTRADOR")
                .orElseGet(() -> rolRepository.save(Rol.builder().nombre("ADMINISTRADOR").build()));

        // 2. ADMIN por defecto
        if (usuarioRepository.findByEmail("admin@cinemax.com").isEmpty()) {
            usuarioRepository.save(Usuario.builder()
                    .email("admin@cinemax.com")
                    .password(passwordEncoder.encode("admin123"))
                    .enabled(true)
                    .roles(Set.of(rolAdmin, rolUsuario))
                    .build());
        }

        // 3. USUARIO de prueba
        if (usuarioRepository.findByEmail("usuario@cinemax.com").isEmpty()) {
            usuarioRepository.save(Usuario.builder()
                    .email("usuario@cinemax.com")
                    .password(passwordEncoder.encode("user123"))
                    .enabled(true)
                    .roles(Set.of(rolUsuario))
                    .build());
        }

        // 4. SALAS
        if (salaRepository.count() == 0) {
            salaRepository.saveAll(List.of(
                    Sala.builder().nombre("Sala IMAX").capacidad(200).build(),
                    Sala.builder().nombre("Sala Premium").capacidad(120).build(),
                    Sala.builder().nombre("Sala 3D").capacidad(150).build(),
                    Sala.builder().nombre("Sala Clásica").capacidad(180).build(),
                    Sala.builder().nombre("Sala VIP").capacidad(60).build()
            ));
        }

        // 5. PELÍCULAS con posters reales de TMDB
        if (peliculaRepository.count() == 0) {
            String tmdb = "https://image.tmdb.org/t/p/w500";

            List<Pelicula> peliculas = List.of(
                p("Dune: Parte Dos", 166, 13, "https://image.tmdb.org/t/p/w500/6izwz7rsy95ARzTR3poZ8H6c5pp.jpg", "https://www.youtube.com/embed/Way9Dexny3w",
                  "Paul Atreides se une a los Fremen mientras busca venganza contra los conspiradores que destruyeron a su familia.", "Ciencia Ficción", 8.2),
                p("Oppenheimer", 180, 16, tmdb+"/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg", "https://www.youtube.com/embed/uYPbbksJxIg",
                  "La historia del físico teórico J. Robert Oppenheimer y su papel en el desarrollo de la bomba atómica.", "Drama", 8.5),
                p("Interstellar", 169, 12, tmdb+"/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg", "https://www.youtube.com/embed/zSWdZVtXT7E",
                  "Un grupo de exploradores viaja a través de un agujero de gusano en el espacio en un intento de asegurar la supervivencia de la humanidad.", "Ciencia Ficción", 8.7),
                p("El Caballero Oscuro", 152, 13, "https://upload.wikimedia.org/wikipedia/en/1/1c/The_Dark_Knight_%282008_film%29.jpg", "https://www.youtube.com/embed/EXeTwQWrcwY",
                  "Batman se enfrenta al Joker, un criminal psicópata que siembra el caos en Gotham City.", "Acción", 9.0),
                p("Inception", 148, 13, tmdb+"/ljsZTbVsrQSqZgWeep2B1QiDKuh.jpg", "https://www.youtube.com/embed/YoHD9XEInc0",
                  "Un ladrón con la habilidad de entrar en los sueños de las personas recibe una última misión imposible.", "Ciencia Ficción", 8.8),
                p("Gladiator II", 148, 16, tmdb+"/2cxhvwyEwRlysAmRH4iodkvo0z5.jpg", "https://www.youtube.com/embed/4rgYUipGJNo",
                  "Años después de la caída de Máximo, Lucio se ve forzado a entrar en el Coliseo para luchar por su libertad.", "Acción", 7.5),
                p("Spider-Man: No Way Home", 148, 12, tmdb+"/1g0dhYtq4irTY1GPXvft6k4YLjm.jpg", "https://www.youtube.com/embed/rt-2cxAiPJk",
                  "Peter Parker pide ayuda a Doctor Strange cuando su identidad como Spider-Man es revelada.", "Acción", 8.3),
                p("Avatar: El Sentido del Agua", 192, 12, tmdb+"/t6HIqrRAclMCA60NsSmeqe9RmNV.jpg", "https://www.youtube.com/embed/d9MyW72ELq0",
                  "Jake Sully y Neytiri se han formado una familia y hacen todo lo posible por permanecer juntos.", "Aventura", 7.8),
                p("Parásitos", 132, 16, tmdb+"/7IiTTgloJzvGI1TAYymCfbfl3vT.jpg", "https://www.youtube.com/embed/5xH0HfJHxYI",
                  "La familia Kim, que vive en un semisótano, idea un plan para infiltrarse en la rica familia Park.", "Thriller", 8.6),
                p("Todo a la Vez en Todas Partes", 139, 16, "https://upload.wikimedia.org/wikipedia/en/1/1e/Everything_Everywhere_All_at_Once.jpg", "https://www.youtube.com/embed/wxN1T1uxQ2g",
                  "Una mujer china-americana se ve envuelta en una aventura en la que solo ella puede salvar el multiverso.", "Ciencia Ficción", 8.0),
                p("Top Gun: Maverick", 130, 12, tmdb+"/62HCnUTziyWcpDaBO2i1DX17ljH.jpg", "https://www.youtube.com/embed/giXco2jaZ_4",
                  "Pete Mitchell entrena a un grupo de pilotos de élite para una misión especializada sin precedentes.", "Acción", 8.4),
                p("El Señor de los Anillos: El Retorno del Rey", 201, 12, "https://m.media-amazon.com/images/M/MV5BNzA5ZDNlZWMtM2NhNS00NDJjLTk4NDItYTRmY2EwMWZlMTY3XkEyXkFqcGdeQXVyNzkwMjQ5NzM@._V1_.jpg", "https://www.youtube.com/embed/r5X-hFf6Bwo",
                  "Gandalf y Aragorn lideran el Mundo de los Hombres contra el ejército de Sauron mientras Frodo y Sam se acercan al Monte del Destino.", "Fantasía", 9.0),
                p("Matrix", 136, 16, tmdb+"/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg", "https://www.youtube.com/embed/vKQi3bBA1y8",
                  "Un hacker descubre que la realidad tal como la conoce es una simulación creada por máquinas.", "Ciencia Ficción", 8.7),
                p("Joker", 122, 18, tmdb+"/udDclJoHjfjb8Ekgsd4FDteOkCU.jpg", "https://www.youtube.com/embed/zAGVQLHvwOY",
                  "Arthur Fleck, un comediante fracasado, es llevado a la locura y se convierte en el criminal conocido como Joker.", "Drama", 8.4),
                p("Blade Runner 2049", 163, 16, tmdb+"/gajva2L0rPYkEWjzgFlBXCAVBE5.jpg", "https://www.youtube.com/embed/gCcx85zbxz4",
                  "Un nuevo blade runner descubre un secreto enterrado que podría sumir en el caos a lo que queda de sociedad.", "Ciencia Ficción", 8.0),
                p("La La Land", 128, 7, tmdb+"/uDO8zWDhfWwoFdKS4fzkUJt0Rf0.jpg", "https://www.youtube.com/embed/0pdqf4P9MB8",
                  "Un pianista de jazz y una aspirante a actriz se enamoran mientras persiguen sus sueños en Los Ángeles.", "Romance", 8.0),
                p("Whiplash", 107, 16, "https://upload.wikimedia.org/wikipedia/en/0/01/Whiplash_poster.jpg", "https://www.youtube.com/embed/7d_jQycdQGo",
                  "Un joven baterista se inscribe en un conservatorio donde un temible instructor lo empujará al límite.", "Drama", 8.5),
                p("Inside Out 2", 100, 0, tmdb+"/vpnVM9B6NMmQpWeZvzLvDESb2QY.jpg", "https://www.youtube.com/embed/LEjhY15eCx0",
                  "Riley es ahora una adolescente y nuevas emociones aparecen inesperadamente en su mente.", "Animación", 7.6)
            );

            peliculaRepository.saveAll(peliculas);

            // 6. FUNCIONES — Crear horarios para las próximas semanas
            List<Sala> salas = salaRepository.findAll();
            List<Pelicula> savedPeliculas = peliculaRepository.findAll();
            LocalDateTime base = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);

            for (int i = 0; i < savedPeliculas.size(); i++) {
                Pelicula pel = savedPeliculas.get(i);
                Sala sala = salas.get(i % salas.size());

                // 3 funciones por película en los próximos días
                for (int d = 0; d < 3; d++) {
                    LocalDateTime hora = base.plusDays(d).withHour(16 + (i % 3) * 3);
                    double precio = 8.50 + (i % 4) * 1.50;

                    funcionRepository.save(Funcion.builder()
                            .pelicula(pel)
                            .sala(sala)
                            .fechaHora(hora)
                            .precio(precio)
                            .build());
                }
            }
        }
    }

    private Pelicula p(String titulo, int duracion, int edad, String poster, String trailer, String sinopsis, String genero, double punt) {
        return Pelicula.builder()
                .titulo(titulo).duracion(duracion).edadMinima(edad)
                .posterUrl(poster).trailerUrl(trailer).sinopsis(sinopsis).genero(genero).puntuacion(punt)
                .build();
    }
}
