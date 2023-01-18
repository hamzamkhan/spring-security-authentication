package com.hamzamustafakhan.authenticationapi.config;

import com.hamzamustafakhan.authenticationapi.service.UserAuthenticationService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class RequestFilter extends OncePerRequestFilter {

    private final TokenUtil tokenUtil;

    private final UserAuthenticationService authenticationService;

    @Autowired
    @Lazy
    RequestFilter(TokenUtil tokenUtil, UserAuthenticationService userAuthenticationService){
        this.tokenUtil = tokenUtil;
        this.authenticationService = userAuthenticationService;
    }



    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = null, email = null;
        if(request.getHeader("Authorization") != null && request.getHeader("Authorization").startsWith("Bearer ")){
            token = request.getHeader("Authorization").substring(7);
            try{
                email = tokenUtil.getEmailFromToken(token);
            } catch(IllegalArgumentException e){
                logger.info("Token can't be retrieved");
                request.setAttribute("exception", e);
            } catch(ExpiredJwtException ex){
                logger.info("Token expired, refreshing token");
                allowTokenRefresh();
                token = tokenUtil.refreshToken(ex.getClaims());
                email = tokenUtil.getEmailFromToken(token);
            }
        } else {
            logger.warn("Token is not a bearer one");
        }

        if(email != null && SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userDetails = authenticationService.loadUserByUsername(email);
            if(tokenUtil.checkValidity(userDetails, token)){
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With, remember-me");
        filterChain.doFilter(request, response);
    }

    private void allowTokenRefresh(){
        SecurityContextHolder.getContext().setAuthentication(null);
    }
}
