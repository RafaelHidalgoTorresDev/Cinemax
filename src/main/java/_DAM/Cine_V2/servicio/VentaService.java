package _DAM.Cine_V2.servicio;

import _DAM.Cine_V2.dto.entrada.EntradaInputDTO;
import _DAM.Cine_V2.dto.venta.VentaInputDTO;
import _DAM.Cine_V2.dto.venta.VentaOutputDTO;
import _DAM.Cine_V2.mapper.EntradaMapper;
import _DAM.Cine_V2.mapper.VentaMapper;
import _DAM.Cine_V2.modelo.*;
import _DAM.Cine_V2.repositorio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepository ventaRepository;
    private final UsuarioRepository usuarioRepository;
    private final FuncionRepository funcionRepository;
    private final EntradaRepository entradaRepository;
    private final VentaMapper ventaMapper;
    private final EntradaMapper entradaMapper;

    public List<VentaOutputDTO> findAll() {
        return ventaRepository.findAll().stream()
                .map(ventaMapper::toDTO)
                .collect(Collectors.toList());
    }

    public VentaOutputDTO findById(Long id) {
        return ventaRepository.findById(id)
                .map(ventaMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));
    }

    /**
     * 🔐 Busca una venta verificando que el usuario tenga permiso.
     * ADMIN puede ver cualquier venta. USUARIO solo las suyas.
     */
    public VentaOutputDTO findByIdSecured(Long id, Authentication auth) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));

        // Si es ADMIN, puede ver cualquier venta
        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))) {
            return ventaMapper.toDTO(venta);
        }

        // Si es USUARIO, solo puede ver las suyas
        if (venta.getUsuario() != null && venta.getUsuario().getEmail().equals(auth.getName())) {
            return ventaMapper.toDTO(venta);
        }

        throw new RuntimeException("No tienes permiso para ver esta venta.");
    }

    /**
     * 🔐 Busca las ventas de un usuario por su email.
     */
    public List<VentaOutputDTO> findByUserEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ventaRepository.findByUsuarioId(usuario.getId()).stream()
                .map(ventaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public VentaOutputDTO save(VentaInputDTO ventaDTO) {
        return saveInternal(ventaDTO);
    }

    /**
     * 🔐 Crear una venta de forma segura.
     * USUARIO solo puede crear ventas para sí mismo.
     */
    @Transactional
    public VentaOutputDTO saveSecured(VentaInputDTO ventaDTO, Authentication auth) {
        // Si es USUARIO, forzamos que la venta sea para él
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))) {
            Usuario usuario = usuarioRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Creamos un nuevo DTO con el ID del usuario autenticado
            ventaDTO = new VentaInputDTO(usuario.getId(), ventaDTO.metodoPago(), ventaDTO.entradas());
        }

        return saveInternal(ventaDTO);
    }

    private VentaOutputDTO saveInternal(VentaInputDTO ventaDTO) {
        Venta venta = ventaMapper.toEntity(ventaDTO);

        if (ventaDTO.usuarioId() != null) {
            Usuario usuario = usuarioRepository.findById(ventaDTO.usuarioId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + ventaDTO.usuarioId()));
            venta.setUsuario(usuario);
        }

        // Establecemos la fecha actual
        venta.setFecha(LocalDateTime.now());
        venta.setEstado("COMPLETADA");

        // If we want to create tickets along with sale:
        if (ventaDTO.entradas() != null) {
            Set<Entrada> entradasEntities = new HashSet<>();
            double total = 0;

            for (EntradaInputDTO eDTO : ventaDTO.entradas()) {
                // Check function
                if (eDTO.funcionId() == null)
                    throw new RuntimeException("Entrada sin funcion ID");
                Funcion funcion = funcionRepository.findById(eDTO.funcionId())
                        .orElseThrow(() -> new RuntimeException("Funcion no encontrada " + eDTO.funcionId()));

                // Check availability
                boolean occupied = entradaRepository.findByFuncionId(funcion.getId()).stream()
                        .anyMatch(e -> e.getFila() == eDTO.fila() && e.getAsiento() == eDTO.asiento()
                                && e.getEstado() != EstadoEntrada.CANCELADA);

                if (occupied) {
                    throw new RuntimeException("Asiento ocupado: " + eDTO.fila() + "-" + eDTO.asiento());
                }

                Entrada entrada = entradaMapper.toEntity(eDTO);
                entrada.setFuncion(funcion);
                entrada.setVenta(venta);
                if (entrada.getEstado() == null)
                    entrada.setEstado(EstadoEntrada.VENDIDA);
                entradasEntities.add(entrada);

                total += funcion.getPrecio();
            }
            venta.setEntradas(entradasEntities);
            venta.setImporteTotal(total);
        }

        Venta saved = ventaRepository.save(venta);
        return ventaMapper.toDTO(saved);
    }

    @Transactional
    public VentaOutputDTO update(Long id, VentaInputDTO ventaDTO) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));

        ventaMapper.update(ventaDTO, venta);

        if (ventaDTO.usuarioId() != null) {
            Usuario usuario = usuarioRepository.findById(ventaDTO.usuarioId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + ventaDTO.usuarioId()));
            venta.setUsuario(usuario);
        }

        return ventaMapper.toDTO(ventaRepository.save(venta));
    }

    public void deleteById(Long id) {
        if (!ventaRepository.existsById(id)) {
            throw new RuntimeException("Venta no encontrada con ID: " + id);
        }
        ventaRepository.deleteById(id);
    }

    /**
     * 🔐 Eliminar venta de forma segura.
     * ADMIN puede borrar cualquiera. USUARIO solo las suyas.
     */
    @Transactional
    public void deleteByIdSecured(Long id, Authentication auth) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));

        // Si es ADMIN, puede borrar cualquier venta
        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))) {
            ventaRepository.deleteById(id);
            return;
        }

        // Si es USUARIO, solo puede borrar las suyas
        if (venta.getUsuario() != null && venta.getUsuario().getEmail().equals(auth.getName())) {
            ventaRepository.deleteById(id);
            return;
        }

        throw new RuntimeException("No tienes permiso para cancelar esta venta.");
    }
}
