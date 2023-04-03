package de.minebugdevelopment.watch2minebug.security;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Autowired
	private JwtTokenProvider jwtTokenProvider;
	
	@Autowired
	private CustomUserDetailsService customUserDetailsService;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		String jwt = getJwtFromRequest(request);
		if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
			String username = jwtTokenProvider.getUsernameFromToken(jwt);
			UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}

		filterChain.doFilter(request, response);
	}
	
	private String getJwtFromRequest(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		
		return null;
	}
	
	

}
