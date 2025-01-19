package org.example.autopark.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.autopark.service.GeneralDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final GeneralDetailsService generalDetailsService;

    public JwtRequestFilter(JwtUtil jwtUtil, GeneralDetailsService generalDetailsService) {
        this.jwtUtil = jwtUtil;
        this.generalDetailsService = generalDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;
        // Проверяем наличие JWT в заголовке Authorization
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = jwtUtil.extractUsername(jwt);
        }


        // Если username не пустой и пользователь еще не аутентифицирован
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Загружаем пользователя через GeneralDetailsService
            UserDetails userDetails = this.generalDetailsService.loadUserByUsername(username);

            // Проверяем валидность токена
            if (jwtUtil.isTokenValid(jwt, userDetails.getUsername())) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                //Добавляет в объект authentication информацию о текущем запросе (н-р, IP-адрес клиента, хост, с которого он пришел).
                //Это может быть полезно для мониторинга и аудита.
                //Этот шаг можно пропустить, если вам не нужно отслеживать детали запроса.
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Устанавливаем аутентификацию в SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // Продолжаем цепочку фильтров
        filterChain.doFilter(request, response);

    }
}
