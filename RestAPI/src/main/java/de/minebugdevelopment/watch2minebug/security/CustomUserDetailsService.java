package de.minebugdevelopment.watch2minebug.security;

import de.minebugdevelopment.watch2minebug.entity.UserEntity;
import de.minebugdevelopment.watch2minebug.repository.UserRepository;
import de.minebugdevelopment.watch2minebug.utils.CustomUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@Transactional
public class CustomUserDetailsService implements UserDetailsService {

    private UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Dieser Benutzer konnte nicht gefunden werden."));

        return new CustomUser(
                user.getUsername(),
                user.getPassword(),
                getAuthorities(user),
                user.getUuid()
        ); 
    }
    
    private Collection<GrantedAuthority> getAuthorities(UserEntity user) {
        List<String> permissions = user.getUserGroups().stream()
                .flatMap(group -> group.getPermissions().stream())
                .toList();

        Collection<GrantedAuthority> authorities = new ArrayList<>(permissions.size());
        permissions.forEach(p -> authorities.add(new SimpleGrantedAuthority(p.toLowerCase())));

        return authorities;
	}
}
