package com.via.ems;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.via.ems.repository.EmployeeRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:emsdb;MODE=MySQL;DB_CLOSE_DELAY=-1",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.datasource.username=sa",
            "spring.datasource.password=",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "spring.jpa.show-sql=false",
            "spring.session.jdbc.initialize-schema=always",
            "debug=false"
        })
@AutoConfigureMockMvc
class EmsApiApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    @SuppressWarnings("rawtypes")
    private SessionRepository sessionRepository;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
    }

    @Test
    void employeeEndpointRejectsRequestWithoutSharedSession() throws Exception {
        mockMvc.perform(get("/v1/employees"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("401"));
    }

    @Test
    void employeeEndpointAcceptsSecurityContextFromJdbcSession() throws Exception {
        Cookie sessionCookie = createAuthenticatedSession();

        mockMvc.perform(get("/v1/employees").cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void employeeMutationUsesSharedSessionAndCookieCsrf() throws Exception {
        Cookie sessionCookie = createAuthenticatedSession();
        Cookie csrfCookie = new Cookie("XSRF-TOKEN", "shared-csrf-token");

        mockMvc.perform(post("/v1/employees")
                        .cookie(sessionCookie, csrfCookie)
                        .header("X-XSRF-TOKEN", csrfCookie.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Ada",
                                  "lastName": "Lovelace",
                                  "email": "ada@example.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ada@example.com"));
    }

    @Test
    void employeeMutationRejectsMissingCookieCsrfHeader() throws Exception {
        Cookie sessionCookie = createAuthenticatedSession();

        mockMvc.perform(post("/v1/employees")
                        .cookie(sessionCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Ada",
                                  "lastName": "Lovelace",
                                  "email": "ada@example.com"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @SuppressWarnings("unchecked")
    private Cookie createAuthenticatedSession() {
        var user = User.withUsername("user@example.com")
                .password("")
                .authorities("ROLE_USER")
                .build();
        var authentication = UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities());
        var securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);

        Session session = sessionRepository.createSession();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
        sessionRepository.save(session);

        return new Cookie("JSESSIONID", session.getId());
    }
}
