package com.raczkowski.springintro.filter;

import com.raczkowski.springintro.service.UserService;
import com.raczkowski.springintro.util.JwtUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class AuthTokenFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER = "Bearer ";

    private UserService userService;

    private JwtUtils jwtUtils;

    public AuthTokenFilter(UserService userService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Optional<String> maybeToken = parseJwt(request);

        maybeToken.ifPresent(token -> {
            String username = jwtUtils.getUserNameFromJwtToken(token);

            UserDetails userDetails = userService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        });

        filterChain.doFilter(request, response);
    }

    private Optional<String> parseJwt(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);

        if (header.contains(AUTHORIZATION_HEADER) && header.startsWith(BEARER)) {
            return of(header.substring(7));
        }

        return empty();
    }
}
