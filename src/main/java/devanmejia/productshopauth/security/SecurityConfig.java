package devanmejia.productshopauth.security;

import devanmejia.productshopauth.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {
    @Autowired
    private JWTAuthenticationManager JWTAuthenticationManager;
    @Autowired
    private JWTSecurityContextRepository JWTSecurityContextRepository;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http.authenticationManager(JWTAuthenticationManager);
        http.securityContextRepository(JWTSecurityContextRepository);
        return http
                .exceptionHandling()
                .authenticationEntryPoint((swe, e) -> Mono.fromRunnable(() -> swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED)))
                .accessDeniedHandler((swe, e) -> Mono.fromRunnable(() -> swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN)))
                .and()
                .httpBasic().disable()
                .csrf().disable()
                .formLogin().disable()
                .logout().disable()
                .authorizeExchange()
                .pathMatchers("/auth/user/logIn", "/auth/user/signUp", "/auth/user/reset/status", "/auth/user/refresh/**").permitAll()
                .pathMatchers("/auth/check/verify-code", "/auth/user/verify-code").hasAuthority(Role.ROLE_UNAUTH_USER.name())
                .pathMatchers("/auth/check/reset-code", "/auth/user/reset-code").hasAuthority(Role.ROLE_RESET_USER.name())
                .pathMatchers("/auth/user/reset/password").hasAuthority(Role.ROLE_CHANGE_PASSWORD.name())
                .pathMatchers("/auth/user/info/**").permitAll()
                .and().build();
    }
    @Bean
    public PasswordEncoder getPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
