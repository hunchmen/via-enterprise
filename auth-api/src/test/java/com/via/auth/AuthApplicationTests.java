package com.via.auth;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.via.auth.model.UserAccount;
import com.via.auth.repository.UserAccountRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:authdb;MODE=MySQL;DB_CLOSE_DELAY=-1",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.datasource.username=sa",
            "spring.datasource.password=",
            "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
            "spring.jpa.hibernate.ddl-auto=validate",
            "debug=false"
        })
@AutoConfigureMockMvc
class AuthApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userAccountRepository.deleteAll();
        userAccountRepository.save(
                new UserAccount("user@example.com", passwordEncoder.encode("correct-password"), true));
    }

    @Test
    void loginCreatesAuthenticatedSession() throws Exception {
        var loginResult = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "USER@example.com",
                                  "password": "correct-password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.authorities[0]").value("ROLE_USER"))
                .andExpect(cookie().exists("JSESSIONID"))
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andReturn();
        Cookie sessionCookie = loginResult.getResponse().getCookie("JSESSIONID");
        Cookie csrfCookie = loginResult.getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(sessionCookie);
        assertNotNull(csrfCookie);

        mockMvc.perform(get("/v1/auth/session").cookie(sessionCookie, csrfCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void loginRejectsInvalidPassword() throws Exception {
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("401"))
                .andExpect(jsonPath("$.errorMessage").value("Invalid email or password"));
    }

    @Test
    void logoutInvalidatesAuthenticatedSession() throws Exception {
        AuthenticatedClient client = login();

        mockMvc.perform(post("/v1/auth/logout")
                        .cookie(client.sessionCookie(), client.csrfCookie())
                        .header("X-XSRF-TOKEN", client.csrfCookie().getValue()))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("XSRF-TOKEN", 0));

        mockMvc.perform(get("/v1/auth/session")).andExpect(status().isUnauthorized());
    }

    @Test
    void sessionRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/v1/auth/session"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("401"));
    }

    @Test
    void changePasswordUpdatesHashAndInvalidatesSession() throws Exception {
        AuthenticatedClient client = login();

        mockMvc.perform(put("/v1/auth/password")
                        .cookie(client.sessionCookie(), client.csrfCookie())
                        .header("X-XSRF-TOKEN", client.csrfCookie().getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "correct-password",
                                  "newPassword": "NewPassword1!"
                                }
                                """))
                .andExpect(status().isNoContent());

        loginWithPassword("correct-password").andExpect(status().isUnauthorized());
        loginWithPassword("NewPassword1!").andExpect(status().isOk());
    }

    @Test
    void changePasswordRejectsWeakNewPassword() throws Exception {
        AuthenticatedClient client = login();

        mockMvc.perform(put("/v1/auth/password")
                        .cookie(client.sessionCookie(), client.csrfCookie())
                        .header("X-XSRF-TOKEN", client.csrfCookie().getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "correct-password",
                                  "newPassword": "weakpass1!"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("400"));
    }

    @Test
    void changePasswordRejectsIncorrectCurrentPassword() throws Exception {
        AuthenticatedClient client = login();

        mockMvc.perform(put("/v1/auth/password")
                        .cookie(client.sessionCookie(), client.csrfCookie())
                        .header("X-XSRF-TOKEN", client.csrfCookie().getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "wrong-password",
                                  "newPassword": "NewPassword1!"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Current password is incorrect"));
    }

    @Test
    void authenticatedStateChangeStillRequiresCookieCsrfHeader() throws Exception {
        AuthenticatedClient client = login();

        mockMvc.perform(put("/v1/auth/password")
                        .cookie(client.sessionCookie(), client.csrfCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "correct-password",
                                  "newPassword": "NewPassword1!"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    private AuthenticatedClient login() throws Exception {
        var result = loginWithPassword("correct-password")
                .andExpect(status().isOk())
                .andExpect(cookie().exists("JSESSIONID"))
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andReturn();
        Cookie sessionCookie = result.getResponse().getCookie("JSESSIONID");
        Cookie csrfCookie = result.getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(sessionCookie);
        assertNotNull(csrfCookie);
        return new AuthenticatedClient(sessionCookie, csrfCookie);
    }

    private ResultActions loginWithPassword(String password) throws Exception {
        return mockMvc.perform(
                post("/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                                {
                                  "email": "user@example.com",
                                  "password": "%s"
                                }
                                """.formatted(password)));
    }

    private record AuthenticatedClient(Cookie sessionCookie, Cookie csrfCookie) {}
}
