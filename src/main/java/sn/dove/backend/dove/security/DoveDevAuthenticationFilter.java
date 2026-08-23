package sn.dove.backend.dove.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sn.dove.backend.dove.config.DoveProperties;
import tech.jhipster.config.JHipsterConstants;

@Component
public class DoveDevAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Dove-User-Id";

    private final DoveProperties properties;

    public DoveDevAuthenticationFilter(DoveProperties properties, Environment environment) {
        this.properties = properties;
        // Defensive safety net: dove.auth.dev-header-enabled=true allows full authentication
        // bypass via the X-Dove-User-Id header. It is (and must stay) false in
        // application-prod.yml, but refuse to even start if the "prod" profile is ever active
        // together with this flag, rather than silently exposing every endpoint.
        if (properties.getAuth().isDevHeaderEnabled() && environment.acceptsProfiles(Profiles.of(JHipsterConstants.SPRING_PROFILE_PRODUCTION))) {
            throw new IllegalStateException(
                "dove.auth.dev-header-enabled must not be true while the 'prod' Spring profile is active: " +
                "it allows full authentication bypass via the X-Dove-User-Id header. Refusing to start."
            );
        }
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
