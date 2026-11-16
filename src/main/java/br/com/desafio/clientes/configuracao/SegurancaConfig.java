package br.com.desafio.clientes.configuracao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
@EnableMethodSecurity
public class SegurancaConfig {

    @Bean
    SecurityFilterChain filtroDeSeguranca(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(requisicoes -> requisicoes
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/customers/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/customers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/customers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/customers/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(conversorDeJwt())))
                .build();
    }

    private Converter<Jwt, AbstractAuthenticationToken> conversorDeJwt() {
        return jwt -> new JwtAuthenticationToken(jwt, extrairPerfis(jwt), jwt.getSubject());
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extrairPerfis(Jwt jwt) {
        Map<String, Object> acessoRealm = jwt.getClaimAsMap("realm_access");
        if (acessoRealm == null || !(acessoRealm.get("roles") instanceof List<?> perfis)) {
            return List.of();
        }
        return perfis.stream()
                .map(String::valueOf)
                .filter(perfil -> perfil.equals("USER") || perfil.equals("ADMIN"))
                .map(perfil -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + perfil))
                .toList();
    }
}
