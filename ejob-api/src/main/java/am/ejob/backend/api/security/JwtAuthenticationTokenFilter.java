package am.ejob.backend.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationTokenFilter.class);

    private final String tokenHeader;
    private final String jwtSecret;

    @Autowired
    @Qualifier("currentUserDetailService")
    private UserDetailsService userDetailsService;

    public JwtAuthenticationTokenFilter(String tokenHeader, String jwtSecret) {
        this.tokenHeader = tokenHeader;
        this.jwtSecret = jwtSecret;
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain chain) throws ServletException, IOException {
        try {

            final String authTokenHeader = request.getHeader(this.tokenHeader);

            if (authTokenHeader == null || !authTokenHeader.startsWith("Bearer ")) {
                chain.doFilter(request, response);
                return;
            }

            String authToken = StringUtils.substring(authTokenHeader, 7);

            Claims claims = null;
            try {
                claims = Jwts.parser()
                        .setSigningKey(jwtSecret)
                        .parseClaimsJws(authToken)
                        .getBody();
            } catch (Exception e) {
                log.error("Unable to parse JWT token. Error: {}", e.getMessage());
            }

            if (claims == null || claims.getExpiration().before(new Date())) {
                log.error("Invalid access token: {}", authTokenHeader);
                chain.doFilter(request, response);
                return;
            }

            String username = claims.getSubject();
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        chain.doFilter(request, response);
    }
}

