package _DAM.Cine_V2.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CineUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 1. Miramos si la petición trae el Header "Authorization"
        String authHeader = request.getHeader("Authorization");

        // 2. Si no trae nada o no empieza por "Bearer ", seguimos adelante sin hacer nada
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            // 4. Si hay username y el usuario no está ya autenticado en el sistema...
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Cargamos los datos del usuario de la BD
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 5. Si el token es válido, le damos el "sello" de autenticado
                if (jwtUtil.validateToken(token)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Guardamos la autenticación en el contexto de seguridad (la caja fuerte)
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Si el token expira o es inválido, simplemente ignoramos y la petición 
            // sigue adelante como anónima. Si la ruta requería auth, Spring Security lo bloqueará.
            System.out.println("Token inválido o expirado: " + e.getMessage());
        }

        // 6. Pasamos la petición al siguiente filtro o al controlador
        filterChain.doFilter(request, response);
    }
}