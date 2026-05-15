package _DAM.Cine_V2.servicio;

import _DAM.Cine_V2.dto.funcion.FuncionInputDTO;
import _DAM.Cine_V2.dto.funcion.FuncionOutputDTO;
import _DAM.Cine_V2.mapper.FuncionMapper;
import _DAM.Cine_V2.modelo.Funcion;
import _DAM.Cine_V2.modelo.Pelicula;
import _DAM.Cine_V2.modelo.Sala;
import _DAM.Cine_V2.repositorio.FuncionRepository;
import _DAM.Cine_V2.repositorio.PeliculaRepository;
import _DAM.Cine_V2.repositorio.SalaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FuncionService {

    private final FuncionRepository funcionRepository;
    private final PeliculaRepository peliculaRepository;
    private final SalaRepository salaRepository;
    private final _DAM.Cine_V2.repositorio.EntradaRepository entradaRepository;
    private final FuncionMapper funcionMapper;

    @Transactional(readOnly = true)
    public List<FuncionOutputDTO> findAll() {
        return funcionRepository.findAll().stream()
                .map(funcionMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FuncionOutputDTO findById(Long id) {
        return funcionRepository.findById(id)
                .map(funcionMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Funcion no encontrada con ID: " + id));
    }

    @Transactional
    public FuncionOutputDTO save(FuncionInputDTO funcionDTO) {
        Funcion funcion = funcionMapper.toEntity(funcionDTO);

        if (funcionDTO.peliculaId() != null) {
            Pelicula pelicula = peliculaRepository.findById(funcionDTO.peliculaId())
                    .orElseThrow(
                            () -> new RuntimeException("Pelicula no encontrada con ID: " + funcionDTO.peliculaId()));
            funcion.setPelicula(pelicula);
        }

        if (funcionDTO.salaId() != null) {
            Sala sala = salaRepository.findById(funcionDTO.salaId())
                    .orElseThrow(() -> new RuntimeException("Sala no encontrada con ID: " + funcionDTO.salaId()));
            funcion.setSala(sala);
        }

        Funcion saved = funcionRepository.save(funcion);
        return funcionMapper.toDTO(saved);
    }

    @Transactional
    public FuncionOutputDTO update(Long id, FuncionInputDTO funcionDTO) {
        Funcion funcion = funcionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Funcion no encontrada con ID: " + id));

        funcionMapper.update(funcionDTO, funcion);

        if (funcionDTO.peliculaId() != null) {
            Pelicula pelicula = peliculaRepository.findById(funcionDTO.peliculaId())
                    .orElseThrow(
                            () -> new RuntimeException("Pelicula no encontrada con ID: " + funcionDTO.peliculaId()));
            funcion.setPelicula(pelicula);
        }

        if (funcionDTO.salaId() != null) {
            Sala sala = salaRepository.findById(funcionDTO.salaId())
                    .orElseThrow(() -> new RuntimeException("Sala no encontrada con ID: " + funcionDTO.salaId()));
            funcion.setSala(sala);
        }

        return funcionMapper.toDTO(funcionRepository.save(funcion));
    }

    @Transactional
    public void deleteById(Long id) {
        if (!funcionRepository.existsById(id)) {
            throw new RuntimeException("Funcion no encontrada con ID: " + id);
        }
        funcionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<java.util.Map<String, Integer>> getAsientosOcupados(Long id) {
        if (!funcionRepository.existsById(id)) {
            throw new RuntimeException("Funcion no encontrada con ID: " + id);
        }
        return entradaRepository.findByFuncionId(id).stream()
                .filter(e -> e.getEstado() != _DAM.Cine_V2.modelo.EstadoEntrada.CANCELADA)
                .map(e -> java.util.Map.of("fila", e.getFila(), "asiento", e.getAsiento()))
                .collect(Collectors.toList());
    }
}
