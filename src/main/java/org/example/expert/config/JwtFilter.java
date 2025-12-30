package org.example.expert.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// ############################################## 2 - 4 ##############################################
// JwtFilter를 다른곳에서 오토와이어링 가능하게 Bean등록
@Component
@Slf4j
@RequiredArgsConstructor
//  public class JwtFilter implements Filter {
    public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

//  @Override
//  public void init(FilterConfig filterConfig) throws ServletException {
//      Filter.super.init(filterConfig);
//  }

    @Override
//  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    // doFilter -> doFilterInternal 로 변경
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

//      HttpServletRequest httpRequest = (HttpServletRequest) request;
//      HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 발급시점은 JWT 검증 스킵
        String url = request.getRequestURI();
        if (url.startsWith("/auth")) {
            chain.doFilter(request, response);
            return;
        }

        // 토큰 존재 유무 검사
        String bearerJwt = request.getHeader("Authorization");
        if (bearerJwt == null) {
            // 토큰이 없는 경우 400을 반환합니다.
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "JWT 토큰이 필요합니다.");
            return;
        }

//      String jwt = jwtUtil.substringToken(bearerJwt);
        try {
            // JWT 유효성 검사와 claims 추출
            String jwt = jwtUtil.substringToken(bearerJwt);
            Claims claims = jwtUtil.extractClaims(jwt);
            if (claims == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 JWT 토큰입니다.");
                return;
            }

            // setAttribute -> SecurityContextHolder로 대체
//          httpRequest.setAttribute("userId", Long.parseLong(claims.getSubject()));
//          httpRequest.setAttribute("email", claims.get("email"));
//          httpRequest.setAttribute("userRole", claims.get("userRole"));
// ############################################## 1 - 2 ##############################################
//          httpRequest.setAttribute("nickname", claims.get("nickname")); // @Auth AuthUser authUser 사용

            // SecurityConfig에서 처리
//            UserRole userRole = UserRole.valueOf(role);
//            if (url.startsWith("/admin")) {
//                // 관리자 권한이 없는 경우 403을 반환합니다.
//                if (!UserRole.ADMIN.equals(userRole)) {
//                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "관리자 권한이 없습니다.");
//                    return;
//                }
//                chain.doFilter(request, response);
//                return;
//            }

            // SecurityContextHolder에 인증 정보 저장
            String role = claims.get("userRole", String.class);

            // id, email, role, nickname
            AuthUser authUser = new AuthUser(
                    Long.parseLong(claims.getSubject()),
                    claims.get("email", String.class),
                    UserRole.valueOf(role),
                    claims.get("nickname", String.class)
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            authUser,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            chain.doFilter(request, response);

        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.", e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않는 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token, 만료된 JWT token 입니다.", e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "지원되지 않는 JWT 토큰입니다.");
        } catch (Exception e) {
            log.error("Internal server error", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

//    @Override
//    public void destroy() {
//        Filter.super.destroy();
//    }
}
