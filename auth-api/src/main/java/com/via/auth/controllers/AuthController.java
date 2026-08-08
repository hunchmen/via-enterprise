package com.via.auth.controllers;

import com.via.auth.dto.ChangePasswordRequest;
import com.via.auth.dto.LoginRequest;
import com.via.auth.dto.SessionResponse;
import com.via.auth.service.PasswordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.CompositeLogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfLogoutHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Session-based authentication endpoints")
@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
    private final PasswordService passwordService;
    private final CsrfTokenRepository csrfTokenRepository;
    private final LogoutHandler logoutHandler;

    public AuthController(
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            SessionAuthenticationStrategy sessionAuthenticationStrategy,
            PasswordService passwordService,
            CsrfTokenRepository csrfTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
        this.passwordService = passwordService;
        this.csrfTokenRepository = csrfTokenRepository;
        this.logoutHandler = new CompositeLogoutHandler(
                new CsrfLogoutHandler(csrfTokenRepository), new SecurityContextLogoutHandler());
    }

    @Operation(
            summary = "Log in",
            description =
                    "Authenticates an enabled user by email and BCrypt-protected password, rotates the session ID, and stores authentication in the HTTP session")
    @ApiResponse(
            responseCode = "200",
            description = "Login successful; the response sets JSESSIONID and XSRF-TOKEN cookies")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "401", description = "Invalid email or password")
    @PostMapping("/login")
    public SessionResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        Authentication authenticationRequest =
                UsernamePasswordAuthenticationToken.unauthenticated(email, request.password());
        Authentication authentication = authenticationManager.authenticate(authenticationRequest);

        sessionAuthenticationStrategy.onAuthentication(authentication, httpRequest, httpResponse);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        securityContextRepository.saveContext(securityContext, httpRequest, httpResponse);
        rotateCsrfToken(httpRequest, httpResponse);

        return toResponse(authentication);
    }

    @Operation(summary = "Get the current session", description = "Returns the currently authenticated user")
    @SecurityRequirement(name = "sessionCookie")
    @ApiResponse(responseCode = "200", description = "Current session returned")
    @ApiResponse(responseCode = "401", description = "No authenticated session")
    @GetMapping("/session")
    public SessionResponse currentSession(Authentication authentication) {
        return toResponse(authentication);
    }

    @Operation(
            summary = "Change password",
            description =
                    "Verifies the current password, stores the new BCrypt hash, and invalidates the current session")
    @SecurityRequirement(name = "sessionCookie")
    @ApiResponse(responseCode = "204", description = "Password changed and session invalidated")
    @ApiResponse(
            responseCode = "400",
            description = "Incorrect current password or new password does not meet the strength rules")
    @ApiResponse(responseCode = "401", description = "No authenticated session")
    @ApiResponse(responseCode = "403", description = "Missing or invalid CSRF token")
    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        passwordService.changePassword(authentication.getName(), request.currentPassword(), request.newPassword());
        logoutHandler.logout(httpRequest, httpResponse, authentication);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Log out", description = "Invalidates the HTTP session and clears its security context")
    @SecurityRequirement(name = "sessionCookie")
    @ApiResponse(responseCode = "204", description = "Logout successful")
    @ApiResponse(responseCode = "401", description = "No authenticated session")
    @ApiResponse(responseCode = "403", description = "Missing or invalid CSRF token")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        logoutHandler.logout(request, response, authentication);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private void rotateCsrfToken(HttpServletRequest request, HttpServletResponse response) {
        CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
        csrfTokenRepository.saveToken(csrfToken, request, response);
    }

    private SessionResponse toResponse(Authentication authentication) {
        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .sorted()
                .toList();
        return new SessionResponse(authentication.getName(), authorities);
    }
}
