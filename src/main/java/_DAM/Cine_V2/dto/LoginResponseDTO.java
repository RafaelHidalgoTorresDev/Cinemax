package _DAM.Cine_V2.dto;

import java.util.Set;

/**
 * Respuesta del login/registro — Incluye Access Token + Refresh Token.
 */
public record LoginResponseDTO(
        String email,
        String message,
        String accessToken,
        String refreshToken,
        Set<String> roles
) {}
