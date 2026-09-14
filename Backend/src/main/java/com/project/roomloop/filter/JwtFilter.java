package com.project.roomloop.filter;

import com.project.roomloop.entity.User;
import com.project.roomloop.helperMethod.HelperForRoomListing;
import com.project.roomloop.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j

public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final HelperForRoomListing helperForRoomListing;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        log.info("incoming requst from : {}", request.getRequestURL());

        final String requestTokenHeader = request.getHeader("Authorization");

        if (requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }

        String token = requestTokenHeader.substring(7);
        log.info("token is = "+token);


        Long userId;

        try {
            userId = jwtService.getUserIdFromToken(token);
        }
        catch (JwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Some Problem withs the _ Invalid JWT token");
            return;
        }

        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            helperForRoomListing.checkUser(userId);      // this is extra remove this after responce token added
            log.info("returnting to the checkUser filter layer ");
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                    new UsernamePasswordAuthenticationToken(userId, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        }


        filterChain.doFilter(request,response);
        return;




    }
}
