package com.web.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import com.web.oauth.PrincipalOauth2UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Configuration
@EnableWebSecurity// 스프링 시큐리티 필터가 스프링 필터체인에 등록이 됨.
@EnableMethodSecurity
public class SecurityConfig{

    @Autowired
    private PrincipalOauth2UserService principalOauth2UserService;


    @Bean
    protected SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
    	http.csrf().ignoringRequestMatchers("/main/register/**", "/main/find-password/**", "/main/community/**", "/main/profile/user-info");
    	http
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/user/**").authenticated()
            .requestMatchers("/manager/**").hasAnyAuthority("ROLE_ADMIN","ROLE_MANAGER")
//            .requestMatchers("/main/community/traveldiary/**").authenticated()
//            .requestMatchers("/main/community/writediary/**").authenticated()
            .anyRequest().permitAll()
        )
        .formLogin(formLogin -> formLogin
            .loginPage("/main/login")
            .loginProcessingUrl("/login")
            .defaultSuccessUrl("/main")

        )
        .oauth2Login(oauth2Login -> oauth2Login
            .loginPage("/main/login")
            .defaultSuccessUrl("/main")
            .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
                .userService(principalOauth2UserService)
            )
        );
        
        return http.build();
    } 
}
