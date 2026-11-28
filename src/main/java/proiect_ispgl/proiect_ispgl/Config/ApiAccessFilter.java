package proiect_ispgl.proiect_ispgl.Config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import proiect_ispgl.proiect_ispgl.Controller.PasswordResetController;
import proiect_ispgl.proiect_ispgl.Service.AccountSessionService;
import proiect_ispgl.proiect_ispgl.Service.ShelterApprovalService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.regex.Pattern;

public class ApiAccessFilter extends OncePerRequestFilter {
    private static final Set<String> PUBLIC_GET = Set.of("/api/animals", "/api/animals/count", "/api/shelters",
            ShelterApprovalService.PATH);
    private static final Set<String> PUBLIC_POST = Set.of("/api/auth/register", "/api/auth/login",
            PasswordResetController.PATH + "/request", PasswordResetController.PATH + "/confirm",
            ShelterApprovalService.PATH, "/api/contact", "/api/donatii", "/api/voluntariat");
    private static final Pattern ANIMAL_DETAIL = Pattern.compile("/api/animals/[+-]?[0-9]+");
    private static final Pattern FAVORITE = Pattern.compile("/api/favorites/[0-9]+");

    private final AccountSessionService sessions;

    public ApiAccessFilter(AccountSessionService sessions) {
        this.sessions = sessions;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        String method = request.getMethod();
        boolean read = "GET".equals(method) || "HEAD".equals(method);
        boolean post = "POST".equals(method);
        if ((read && (PUBLIC_GET.contains(path) || ANIMAL_DETAIL.matcher(path).matches()))
                || (post && PUBLIC_POST.contains(path))) {
            chain.doFilter(request, response);
            return;
        }

        boolean sessionRoute = read && "/api/auth/session".equals(path);
        boolean favoriteWrite = (post || "DELETE".equals(method)) && FAVORITE.matcher(path).matches();
        if (!sessionRoute && !favoriteWrite) {
            reject(response, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND");
            return;
        }

        var session = sessions.verify(request);
        if (session == null) {
            reject(response, HttpServletResponse.SC_UNAUTHORIZED, "NO_SESSION");
            return;
        }
        if (favoriteWrite && !"adopter".equals(session.account().role())) {
            reject(response, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN");
            return;
        }
        if (favoriteWrite && !session.matchesCsrfToken(request.getHeader(AccountSessionService.CSRF_HEADER))) {
            reject(response, HttpServletResponse.SC_FORBIDDEN, "INVALID_CSRF_TOKEN");
            return;
        }
        request.setAttribute(AccountSessionService.VERIFIED_SESSION_ATTRIBUTE, session);
        chain.doFilter(request, response);
    }

    private static void reject(HttpServletResponse response, int status, String code) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.getWriter().write("{\"code\":\"" + code + "\"}");
    }
}
