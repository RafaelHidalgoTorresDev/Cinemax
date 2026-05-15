package _DAM.Cine_V2.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Handler personalizado para errores 403 (Acceso Denegado).
 * Devuelve JSON en lugar de la página de error por defecto de Spring.
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        String json = """
                {
                    "status": 403,
                    "error": "Acceso Denegado",
                    "message": "No tienes permisos suficientes para acceder a este recurso.",
                    "path": "%s"
                }
                """.formatted(request.getServletPath());

        PrintWriter writer = response.getWriter();
        writer.print(json);
        writer.flush();
    }
}
