package de.minebugdevelopment.watch2minebug.security;

import de.minebugdevelopment.watch2minebug.utils.CustomUser;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {}

    public static CustomUser getCurrentUser() {
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof CustomUser result) return result;
        throw new SecurityException();
    }

    public static boolean checkAdmin() {
        return getCurrentUser().getAuthorities().stream().anyMatch(x -> x.getAuthority().equals("ADMIN"));
    }

    public static void checkAuthority(String authority) throws SecurityException {
        CustomUser user = getCurrentUser();
        if (user.getAuthorities().stream().noneMatch( x -> x.getAuthority().equals("*") || x.getAuthority().equals(authority) ))
            throw new SecurityException();
    }

}
