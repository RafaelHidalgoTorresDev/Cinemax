package _DAM.Cine_V2.util;

import _DAM.Cine_V2.modelo.*;
import _DAM.Cine_V2.repositorio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

// @Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

        private final RolRepository rolRepository;
        private final UsuarioRepository usuarioRepository;
        private final DirectorRepository directorRepository;
        private final ActorRepository actorRepository;
        private final PeliculaRepository peliculaRepository;
        private final SalaRepository salaRepository;
        private final FuncionRepository funcionRepository;

        @Override
        public void run(String... args) throws Exception {
                if (rolRepository.count() > 0) {
                        return; // Data already exists
                }

                // Roles
                Rol roleAdmin = rolRepository.save(Rol.builder().nombre("ADMIN").build());
                Rol roleUser = rolRepository.save(Rol.builder().nombre("USER").build());

                // Users
                Usuario admin = Usuario.builder()
                                .email("admin@cine.com")
                                .password("admin") // Plain text for demo
                                .enabled(true)
                                .roles(Set.of(roleAdmin))
                                .build();
                usuarioRepository.save(admin);

                Usuario user = Usuario.builder()
                                .email("user@cine.com")
                                .password("user")
                                .enabled(true)
                                .roles(Set.of(roleUser))
                                .build();
                usuarioRepository.save(user);

                // Director
                Director nolan = Director.builder().nombre("Christopher Nolan").build();
                directorRepository.save(nolan);

                // Actor
                Actor leo = Actor.builder().nombre("Leonardo DiCaprio").build();
                actorRepository.save(leo);

                // Pelicula
                Pelicula inception = Pelicula.builder()
                                .titulo("Inception")
                                .duracion(148)
                                .edadMinima(13)
                                .director(nolan)
                                .actores(Set.of(leo))
                                .build();
                peliculaRepository.save(inception);

                // Sala
                Sala sala1 = Sala.builder().nombre("Sala 1").capacidad(100).build();
                salaRepository.save(sala1);

                // Funcion
                Funcion funcion = Funcion.builder()
                                .pelicula(inception)
                                .sala(sala1)
                                .fechaHora(LocalDateTime.now().plusDays(1))
                                .precio(10.0)
                                .build();
                funcionRepository.save(funcion);

                System.out.println("Data loaded successfully!");
        }
}
