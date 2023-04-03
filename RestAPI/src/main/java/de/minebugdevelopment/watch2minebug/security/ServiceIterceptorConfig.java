package de.minebugdevelopment.watch2minebug.security;

import de.minebugdevelopment.watch2minebug.utils.Authorized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class ServiceIterceptorConfig extends WebMvcConfigurerAdapter {

    @Autowired
    ProductServiceInterceptor productServiceInterceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(productServiceInterceptor);
    }
}


@Component
class ProductServiceInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle (HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            if (handler instanceof HandlerMethod handlerMethod) {
                Authorized authorized = handlerMethod.getMethodAnnotation(Authorized.class);
                if (authorized == null)
                    authorized = handlerMethod.getMethod().getDeclaringClass().getAnnotation(Authorized.class);
                if (authorized == null) return true;

                if (!SecurityUtils.getCurrentUser().hasAuthority(authorized.permission()))
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            }
        } catch (ResponseStatusException ex) {
            throw ex; // Rethrow this exception
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return true;
    }
}