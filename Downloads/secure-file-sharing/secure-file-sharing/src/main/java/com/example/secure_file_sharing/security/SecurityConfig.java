package com.example.secure_file_sharing.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.secure_file_sharing.user.AppUser;
import com.example.secure_file_sharing.user.AppUserRepository;

@Configuration
public class SecurityConfig {

	@Autowired
	private AppUserRepository appUserRepository;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public UserDetailsService userDetailsService() {
		return email -> {
			AppUser user = appUserRepository.findByEmail(email)
					.orElseThrow(() -> new UsernameNotFoundException("User not found"));

			return User.withUsername(user.getEmail()).password(user.getPassword()).roles(user.getRole()).build();
		};
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder) {

		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);

		provider.setPasswordEncoder(passwordEncoder);
		return provider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, DaoAuthenticationProvider authenticationProvider,
			JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

		http.cors(cors -> {
		}).csrf(csrf -> csrf.disable()).formLogin(form -> form.disable()).httpBasic(basic -> basic.disable())

				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				.authenticationProvider(authenticationProvider)

				.authorizeHttpRequests(auth -> auth.requestMatchers("/api/auth/**").permitAll()
						.requestMatchers("/api/files/**").authenticated().anyRequest().denyAll())

				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
