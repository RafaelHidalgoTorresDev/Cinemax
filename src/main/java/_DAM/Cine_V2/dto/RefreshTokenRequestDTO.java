package _DAM.Cine_V2.dto;

/**
 * DTO para solicitudes que envían un Refresh Token (refresh y logout).
 */
public record RefreshTokenRequestDTO(
        String refreshToken
) {}
