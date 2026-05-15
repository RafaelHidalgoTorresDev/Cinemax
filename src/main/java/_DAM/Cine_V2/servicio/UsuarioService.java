package _DAM.Cine_V2.servicio;

import _DAM.Cine_V2.dto.usuario.UsuarioInputDTO;
import _DAM.Cine_V2.dto.usuario.UsuarioOutputDTO;
// IMPORTS PARA EL LOGIN
import _DAM.Cine_V2.dto.LoginRequestDTO;
import _DAM.Cine_V2.dto.LoginResponseDTO;
import _DAM.Cine_V2.mapper.UsuarioMapper;
import _DAM.Cine_V2.modelo.RefreshToken;
import _DAM.Cine_V2.modelo.Rol;
import _DAM.Cine_V2.modelo.Usuario;
import _DAM.Cine_V2.repositorio.RolRepository;
import _DAM.Cine_V2.repositorio.UsuarioRepository;
import _DAM.Cine_V2.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioMapper usuarioMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public List<UsuarioOutputDTO> findAll() {
        return usuarioRepository.findAll().stream()
                .map(usuarioMapper::toDTO)
                .collect(Collectors.toList());
    }

    public UsuarioOutputDTO findById(Long id) {
        return usuarioRepository.findById(id)
                .map(usuarioMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrada con ID: " + id));
    }

    /**
     * Busca un usuario por email. Útil para el perfil propio (/me).
     */
    public UsuarioOutputDTO findByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .map(usuarioMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));
    }

    @Transactional
    public UsuarioOutputDTO save(UsuarioInputDTO usuarioDTO) {
        Usuario usuario = usuarioMapper.toEntity(usuarioDTO);

        // Handle Roles
        if (usuarioDTO.roles() != null && !usuarioDTO.roles().isEmpty()) {
            Set<Rol> roles = new HashSet<>();
            for (String rolNombre : usuarioDTO.roles()) {
                Rol rol = rolRepository.findByNombre(rolNombre)
                        .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + rolNombre));
                roles.add(rol);
            }
            usuario.setRoles(roles);
        }

        // Ciframos la contraseña
        if (usuarioDTO.password() != null && !usuarioDTO.password().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(usuarioDTO.password()));
        }

        Usuario saved = usuarioRepository.save(usuario);
        return usuarioMapper.toDTO(saved);
    }

    /**
     * 🆕 Registro + Login automático — Devuelve tokens directamente.
     * Usado por el endpoint /api/v1/auth/register.
     */
    @Transactional
    public LoginResponseDTO registerAndLogin(UsuarioInputDTO usuarioDTO) {
        // 1. Verificamos que no exista
        if (usuarioRepository.findByEmail(usuarioDTO.email()).isPresent()) {
            throw new RuntimeException("Ya existe un usuario con ese email.");
        }

        Usuario usuario = usuarioMapper.toEntity(usuarioDTO);

        // 2. Asignamos rol USUARIO por defecto si no se especifica
        Set<Rol> roles = new HashSet<>();
        if (usuarioDTO.roles() != null && !usuarioDTO.roles().isEmpty()) {
            for (String rolNombre : usuarioDTO.roles()) {
                Rol rol = rolRepository.findByNombre(rolNombre)
                        .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + rolNombre));
                roles.add(rol);
            }
        } else {
            Rol rolUsuario = rolRepository.findByNombre("USUARIO")
                    .orElseThrow(() -> new RuntimeException("Rol USUARIO no encontrado en la BD"));
            roles.add(rolUsuario);
        }
        usuario.setRoles(roles);
        usuario.setEnabled(true);

        // 3. Ciframos la contraseña
        if (usuarioDTO.password() != null && !usuarioDTO.password().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(usuarioDTO.password()));
        }

        Usuario saved = usuarioRepository.save(usuario);

        // 4. Generamos los tokens
        String accessToken = jwtUtil.generateToken(saved);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(saved);

        Set<String> roleNames = saved.getRoles().stream()
                .map(Rol::getNombre)
                .collect(Collectors.toSet());

        return new LoginResponseDTO(
                saved.getEmail(),
                "Registro exitoso",
                accessToken,
                refreshToken.getToken(),
                roleNames
        );
    }

    @Transactional
    public UsuarioOutputDTO update(Long id, UsuarioInputDTO usuarioDTO) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrada con ID: " + id));

        usuarioMapper.update(usuarioDTO, usuario);

        // Handle Roles
        if (usuarioDTO.roles() != null) {
            Set<Rol> roles = new HashSet<>();
            for (String rolNombre : usuarioDTO.roles()) {
                Rol rol = rolRepository.findByNombre(rolNombre)
                        .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + rolNombre));
                roles.add(rol);
            }
            usuario.setRoles(roles);
        }

        // Ciframos la contraseña si se actualiza
        if (usuarioDTO.password() != null && !usuarioDTO.password().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(usuarioDTO.password()));
        }

        return usuarioMapper.toDTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioOutputDTO updateMe(String emailAuth, UsuarioInputDTO usuarioDTO) {
        Usuario usuario = usuarioRepository.findByEmail(emailAuth)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + emailAuth));

        // Actualizamos email (si cambia y no está en uso)
        if (!usuario.getEmail().equals(usuarioDTO.email())) {
            if (usuarioRepository.findByEmail(usuarioDTO.email()).isPresent()) {
                throw new RuntimeException("El email ya está en uso por otro usuario.");
            }
            usuario.setEmail(usuarioDTO.email());
        }

        // Ciframos la nueva contraseña si se envía
        if (usuarioDTO.password() != null && !usuarioDTO.password().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(usuarioDTO.password()));
        }

        return usuarioMapper.toDTO(usuarioRepository.save(usuario));
    }

    public void deleteById(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    /**
     * 🔐 LOGIN — Genera Access Token + Refresh Token.
     */
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO req) {
        Usuario u = usuarioRepository.findByEmail(req.email())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(req.password(), u.getPassword())) {
            throw new RuntimeException("Credenciales incorrectas");
        }

        // 🎟️ Generamos el Access Token
        String accessToken = jwtUtil.generateToken(u);

        // 🔄 Generamos el Refresh Token (se guarda en la BD)
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(u);

        Set<String> roleNames = u.getRoles().stream()
                .map(Rol::getNombre)
                .collect(Collectors.toSet());

        return new LoginResponseDTO(
                u.getEmail(),
                "Login exitoso",
                accessToken,
                refreshToken.getToken(),
                roleNames
        );
    }

    /**
     * 🔄 REFRESH — Verifica el Refresh Token y genera un nuevo Access Token.
     */
    @Transactional
    public LoginResponseDTO refreshAccessToken(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(refreshTokenStr);
        Usuario usuario = refreshToken.getUsuario();

        String newAccessToken = jwtUtil.generateToken(usuario);

        Set<String> roleNames = usuario.getRoles().stream()
                .map(Rol::getNombre)
                .collect(Collectors.toSet());

        return new LoginResponseDTO(
                usuario.getEmail(),
                "Token renovado",
                newAccessToken,
                refreshToken.getToken(),
                roleNames
        );
    }

    /**
     * 🚪 LOGOUT — Elimina el Refresh Token de la BD.
     */
    @Transactional
    public void logout(String refreshTokenStr) {
        refreshTokenService.deleteByToken(refreshTokenStr);
    }
}