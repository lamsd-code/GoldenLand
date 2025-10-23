package com.example.demo.config;

import com.example.demo.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    public WebSecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // cho demo đăng ký/OTP (nếu muốn bật CSRF, thêm input _csrf trong form)
                .csrf(csrf -> csrf.disable())

                // ✅ sử dụng authenticationProvider chính xác
                .authenticationProvider(authenticationProvider())

                .authorizeHttpRequests(auth -> auth
                        // các đường dẫn phục vụ ĐĂNG KÝ + OTP
                        .requestMatchers(
                                "/register", "/otp",
                                "/auth/register", "/auth/verify-otp", "/auth/otp/send/email",
                                "/", "/trang-chu", "/gioi-thieu", "/tin-tuc", "/lien-he", "/san-pham",
                                "/css/**", "/js/**", "/images/**", "/assets/**", "/webjars/**", "/favicon.ico", "/error",
                                "/login"
                        ).permitAll()

                        // [ADDED] Cho phép các request customer đã đăng nhập (profile + đổi mật khẩu)
                        .requestMatchers("/customer/**", "/api/customer/**").authenticated()
                        .requestMatchers("/admin/**").hasAnyAuthority("ADMIN","MANAGER","STAFF")
                        // mặc định còn lại phải đăng nhập
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/trang-chu", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }
}
