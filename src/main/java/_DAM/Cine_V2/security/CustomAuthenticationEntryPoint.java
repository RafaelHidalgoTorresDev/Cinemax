package _DAM.Cine_V2.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Punto de entrada personalizado para errores 401 (No Autenticado).
 * En lugar de devolver una página HTML de Spring, devuelve un JSON limpio.
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String json = """
                {
                    "status": 401,
                    "error": "No Autorizado",
                    "message": "Necesitas iniciar sesion para acceder a este recurso.",
                    "path": "%s"
                }
                """.formatted(request.getServletPath());

        PrintWriter writer = response.getWriter();
        writer.print(json);
        writer.flush();
    }
}
