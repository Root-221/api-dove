package sn.dove.backend.dove.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sn.dove.backend.dove.config.DoveProperties;

@Component
public class DoveDevAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Dove-User-Id";

    private final DoveProperties properties;

    public DoveDevAuthenticationFilter(DoveProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        if (properties.getAuth().isDevHeaderEnabled() && SecurityContextHolder.getContext().getAuthentication() == null) {
            String userId = request.getHeader(HEADER);
            if (userId == null || userId.isBlank()) {
                userId = properties.getAuth().getDefaultUserId();
            }
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}
