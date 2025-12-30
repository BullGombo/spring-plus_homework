// ############################################## 2 - 4 ##############################################
package org.example.expert.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {return new BCryptPasswordEncoder();}

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                // 필터 순서 jwtFilter -> SecurityContextHolderAwareRequestFilter
                .addFilterBefore(jwtFilter, SecurityContextHolderAwareRequestFilter.class) // JWTFilter 하단 코드의 SecurityContextHolder 참조, 그 정보를 기준으로 인가 처리
                // 인가 정책 정의, 내부적으로 FilterSecurityInterceptor -> AccessDecisionManager 자동 구성
                .authorizeHttpRequests(auth -> auth
                                // 토큰을 발급받는 로그인의 경우, 아직 토큰이 없기 때문에 -> 토큰 검사를 하지 않아도 통과
                                .requestMatchers("/auth/**").permitAll() // 인증없이 접근을 허용, 빼도 상관 없지만 명시적으로 작성
                                .requestMatchers("/admin/**").hasRole("ADMIN") // ADMIN만 허용
                                .requestMatchers("/users/**").hasRole("USER")
                                .anyRequest().authenticated() // 위의 것 외엔 모두 인증 필요
                )
                .build();
    }

}
