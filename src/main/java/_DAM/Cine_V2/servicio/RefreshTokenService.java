package _DAM.Cine_V2.servicio;

import _DAM.Cine_V2.modelo.RefreshToken;
import _DAM.Cine_V2.modelo.Usuario;
import _DAM.Cine_V2.repositorio.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Servicio para gestionar los Refresh Tokens.
 * Se encarga de crear, verificar y eliminar tokens de refresco en la BD.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpirationMs;

    /**
     * Crea un nuevo Refresh Token para el usuario.
     * Si ya tenía uno anterior, lo elimina primero.
     */
    @Transactional
    public RefreshToken createRefreshToken(Usuario usuario) {
        // Eliminamos cualquier token anterior del usuario
        refreshTokenRepository.deleteByUsuario(usuario);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .fechaExpiracion(Instant.now().plusMillis(refreshExpirationMs))
                .usuario(usuario)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Busca un Refresh Token por su valor y verifica que no haya expirado.
     * Si ha expirado, lo elimina de la BD y lanza excepción.
     */
    @Transactional
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh Token no encontrado. Inicia sesión de nuevo."));

        if (refreshToken.getFechaExpiracion().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("El Refresh Token ha expirado. Inicia sesión de nuevo.");
        }

        return refreshToken;
    }

    /**
     * Elimina el Refresh Token (para logout).
     */
    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshTokenRepository::delete);
    }
}
