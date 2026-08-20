package com.skala.day3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * 실습용 HTTP Basic 보안 설정. 사용자 ID는 요청 파라미터가 아니라 인증 주체에서만 가져온다.
 * 운영에서는 이 인메모리 계정을 조직의 IdP/JWT 인증으로 교체한다.
 */
@Configuration
@EnableMethodSecurity
@SecurityScheme(name = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic")
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // 브라우저 세션·폼이 없는 JSON 실습 API이므로 CSRF 토큰 대신 Basic 인증을 사용한다.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                                "/actuator/health").permitAll()
                        .requestMatchers("/lab3/admin/**", "/actuator/**").hasRole("ADMIN")
                        .requestMatchers("/lab3/**").authenticated()
                        .anyRequest().permitAll())
                .httpBasic(basic -> {})
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(Day3Properties props, PasswordEncoder encoder) {
        String user1Password = encoder.encode(props.security().user1Password());
        String user2Password = encoder.encode(props.security().user2Password());
        String adminPassword = encoder.encode(props.security().adminPassword());
        return new InMemoryUserDetailsManager(
                User.withUsername("user1").password(user1Password).roles("USER").build(),
                User.withUsername("user2").password(user2Password).roles("USER").build(),
                User.withUsername("admin").password(adminPassword).roles("ADMIN").build());
    }
}
