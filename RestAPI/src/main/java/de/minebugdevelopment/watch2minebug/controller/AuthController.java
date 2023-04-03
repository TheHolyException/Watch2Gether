package de.minebugdevelopment.watch2minebug.controller;

import de.minebugdevelopment.watch2minebug.dtos.UserDTO;
import de.minebugdevelopment.watch2minebug.entity.UserEntity;
import de.minebugdevelopment.watch2minebug.entity.UserGroupEntity;
import de.minebugdevelopment.watch2minebug.repository.UserGroupRepository;
import de.minebugdevelopment.watch2minebug.repository.UserRepository;
import de.minebugdevelopment.watch2minebug.requests.AuthRequest;
import de.minebugdevelopment.watch2minebug.requests.UserChangeRequest;
import de.minebugdevelopment.watch2minebug.security.JwtTokenProvider;
import de.minebugdevelopment.watch2minebug.utils.dto.DTObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private UserGroupRepository userGroupRepository;
	
	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@PutMapping(value = "/register")
	public DTObject register(@RequestBody UserChangeRequest authRequest) {
		Optional<UserEntity> userOptional = userRepository.findByUsername(authRequest.getUsername());
		
		if (userOptional.isPresent()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

		UserEntity user = new UserEntity();
		user.setUsername(authRequest.getUsername());
		user.setPassword(passwordEncoder.encode(authRequest.getPassword()));
		user.setFirstName(null);
		user.setLastName(null);
		
		Optional<UserGroupEntity> ugeOptional = userGroupRepository.findByName("default");
		if (ugeOptional.isEmpty()) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		user.getUserGroups().add(ugeOptional.get());

		UserEntity created = userRepository.save(user);
		return new UserDTO().parseFrom(created);
	}

	@Deprecated
	@PostMapping(value = "/login")
	public ResponseEntity<String> login(@RequestBody AuthRequest authRequest) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						authRequest.getUsername(),
						authRequest.getPassword()
				)
		);
		return ResponseEntity.ok(jwtTokenProvider.generateToken(authentication));
	}

	@GetMapping("/login")
	public List<Object> login(@RequestParam String username,
							  @RequestParam String password) {
		Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username,password));
		Optional<UserEntity> optionalUserEntity = userRepository.findByUsername(username);
		if (optionalUserEntity.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		return Arrays.asList(jwtTokenProvider.generateToken(authentication), new UserDTO().parseFrom(optionalUserEntity.get()));
	}

	@GetMapping(value = "/validate")
	public ResponseEntity<String> validate() {
		return ResponseEntity.ok("");
	}
	
}
