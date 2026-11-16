package cz.uhk.ppro.akademietrutnov.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Statické prostředky a veřejné cesty
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/login", "/error").permitAll()
                .requestMatchers("/certificates/verify/**").permitAll()

                // Administrátorské operace (musí předcházet obecné /{id})
                .requestMatchers("/courses/new", "/courses/*/edit", "/courses/*/delete").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/courses", "/courses/*").hasRole("ADMIN")
                .requestMatchers("/terms/new", "/terms/*/edit", "/terms/*/delete", "/terms/*/promote-next", "/terms/*/days/**").hasRole("ADMIN")
                .requestMatchers("/reports/**").hasRole("ADMIN")

                // Lektoři a administrátoři (docházka, osvědčení)
                .requestMatchers("/terms/*/attendance/**").hasAnyRole("ADMIN", "LECTURER")
                .requestMatchers("/certificates", "/certificates/{id}").hasAnyRole("ADMIN", "LECTURER")
                .requestMatchers("/registrations/*/issue-certificate", "/registrations/*/reject").hasAnyRole("ADMIN", "LECTURER")

                // Veřejné čtení katalogu kurzů a termínů
                .requestMatchers(HttpMethod.GET, "/", "/courses", "/courses/{id}", "/terms", "/terms/{id}").permitAll()

                // Všechny ostatní požadavky (např. registrace, zrušení, moje přihlášky) vyžadují přihlášení
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/courses", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            // Povolení H2 konzole v iframu pro dev profil
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"));

        return http.build();
    }
}
