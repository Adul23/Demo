package com.example.demo.config;

import com.example.demo.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService,
            HandlerExceptionResolver handlerExceptionResolver
    ) {
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Не фильтруем внутренние перенаправления ошибок
        return request.getServletPath().equals("/error");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();
        String method = request.getMethod();
        log.info("[JWT FILTER] Запрос пришел: {} {}", method, path);

        final String authHeader = request.getHeader("Authorization");
        log.info("[JWT FILTER] Заголовок Authorization: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("[JWT FILTER] Токен отсутствует или не валиден. Пропускаем запрос дальше по цепочке.");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String userEmail = jwtService.extractUsername(jwt);
            log.info("[JWT FILTER] Извлечен email из токена: {}", userEmail);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            log.info("[JWT FILTER] Текущая аутентификация в контексте: {}", authentication);

            if (userEmail != null && authentication == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                log.info("[JWT FILTER] Пользователь найден в бд: {}, статус Enabled: {}", userDetails.getUsername(), userDetails.isEnabled());

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("[JWT FILTER] Пользователь успешно аутентифицирован в SecurityContext");
                } else {
                    log.warn("[JWT FILTER] Токен оказался невалидным для данного пользователя!");
                }
            }

            filterChain.doFilter(request, response);
            log.info("[JWT FILTER] Запрос успешно ушел дальше после проверки токена.");

        } catch (Exception e) {
            log.error("[JWT FILTER] КРИТИЧЕСКАЯ ОШИБКА ВНУТРИ ФИЛЬТРА: ", e);
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }
}