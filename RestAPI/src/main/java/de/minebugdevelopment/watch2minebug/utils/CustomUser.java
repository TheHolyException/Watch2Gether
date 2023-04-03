package de.minebugdevelopment.watch2minebug.utils;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.UUID;

public class CustomUser extends User {

    @Getter
    private final UUID userId;

    public CustomUser(String username, String password, Collection<? extends GrantedAuthority> authorities, UUID userId) {
        super(username, password, true, true, true, true, authorities);
        this.userId = userId;
    }

    public boolean hasAuthority(String authority) {
        return this.getAuthorities().stream().anyMatch(x -> x.getAuthority().equals(authority) || x.getAuthority().equals("*"));
    }

    public void checkAuthority(String authority) throws SecurityException {
        if (this.getAuthorities().stream().noneMatch( x -> x.getAuthority().equals("*") || x.getAuthority().equals(authority) ))
            throw new SecurityException();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CustomUser user) {
            return user.userId == this.userId;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (this.userId).hashCode();
    }
}
