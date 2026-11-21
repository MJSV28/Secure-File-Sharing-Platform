   package com.example.secure_file_sharing.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.example.secure_file_sharing.security.JwtService;
import com.example.secure_file_sharing.user.AppUser;
import com.example.secure_file_sharing.user.AppUserRepository;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	@Autowired
	private AppUserRepository appUserRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtService jwtService;

	@PostMapping("/register")
	public ResponseEntity<?> register(@Valid @RequestBody AuthRequest request) {

		String email = request.getEmail().trim().toLowerCase();

		if (appUserRepository.existsByEmail(email)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
		}

		AppUser user = new AppUser();
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setRole("USER");

		appUserRepository.save(user);

		return ResponseEntity.status(HttpStatus.CREATED).body("Registration successful");
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {

		String email = request.getEmail().trim().toLowerCase();

		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(email, request.getPassword()));

		String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

		String token = jwtService.generateToken(email, role);

		return ResponseEntity.ok(new AuthResponse(token, email, "Bearer", 900));
	}

	public static class AuthRequest {

		@NotBlank
		@Email
		private String email;

		@NotBlank
		@Size(min = 8, max = 72)
		private String password;

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
	}

	public static class AuthResponse {

		private String token;
		private String email;
		private String tokenType;
		private long expiresIn;

		public AuthResponse(String token, String email, String tokenType, long expiresIn) {
			this.token = token;
			this.email = email;
			this.tokenType = tokenType;
			this.expiresIn = expiresIn;
		}

		public String getToken() {
			return token;
		}

		public String getEmail() {
			return email;
		}

		public String getTokenType() {
			return tokenType;
		}

		public long getExpiresIn() {
			return expiresIn;
		}
	}

}
