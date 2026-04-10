package org.cognizant.programmanagement.config;

import org.cognizant.programmanagement.filter.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // Use this instead of @EnableWebMvc for Security
public class SecurityConfig {

    @Autowired
    JwtFilter jwtFilter;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                     .requestMatchers("/api/users/login", "/api/users/createUser","/api/citizens/createCitizen","/api/logs/**","/api/programs/viewAll","/api/resources/viewAll").permitAll() // ALLOW THESE WITHOUT LOGIN
                      .requestMatchers("/api/citizens/createCitizen", "/api/citizens/update/{id}", "/api/documents/upload","/api/documents/delete/{id}", "/api/users/login","/api/reports/getreportbyid/{id}","/api/reports/createreport","/api/reports/getreportwithcitizendetails/{id}/details/","/api/programs/").hasRole("CITIZEN")
                       .requestMatchers("/api/audits/**", "/api/logs/**").hasAnyRole("AUDITOR","MANAGER","COMPLIANCE")
                   .requestMatchers("/api/logs/GetAllLogs", "/api/logs/CreateLog","/api/citizens/getAllCitizens").hasRole("AUDITOR")
                    .requestMatchers("/api/compliance-records/**").hasRole("COMPLIANCE")
                             // Officer Endpoints
                       .requestMatchers("/api/citizens/getCitizenById/{id}", "/api/citizens/getAllCitizens", "/api/citizens/delete/{id}", "/api/documents/getDocById/{id}", "/api/incidents/**", "/api/shelters/**", "/api/recoveries/**", "/api/distributions/**","/api/reports/getreportbyid/{id}").hasRole("OFFICER")


                    .requestMatchers("/api/users/getByUserId/{id}", "/api/users/getAllUsers", "/api/users/update/{id}", "/api/users/delete/{id}",
                                     "/api/citizens/getCitizenById/{id}", "/api/citizens/getAllCitizens", "/api/citizens/delete/{id}", "/api/documents/getDocById/{id}",
                                       "/api/incidents/**", "/api/shelters/**", "/api/programs/**","/api/resources/**", "/api/distributions/**").hasRole("MANAGER")
                              //  .anyRequest().permitAll() // LOCK EVERYTHING ELSE
                )

                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    /*
   // @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration){
        return configuration.getAuthenticationManager();

    }

   // @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(new BCryptPasswordEncoder());
        return provider;
    }
*/

}