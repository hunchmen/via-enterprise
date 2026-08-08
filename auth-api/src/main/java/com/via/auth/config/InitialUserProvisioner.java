package com.via.auth.config;

import com.via.auth.model.UserAccount;
import com.via.auth.repository.UserAccountRepository;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class InitialUserProvisioner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitialUserProvisioner.class);

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final String bootstrapEmail;
    private final String bootstrapPassword;

    public InitialUserProvisioner(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            @Value("${auth.bootstrap.email:}") String bootstrapEmail,
            @Value("${auth.bootstrap.password:}") String bootstrapPassword) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapEmail = bootstrapEmail;
        this.bootstrapPassword = bootstrapPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean hasEmail = StringUtils.hasText(bootstrapEmail);
        boolean hasPassword = StringUtils.hasText(bootstrapPassword);

        if (!hasEmail && !hasPassword) {
            return;
        }
        if (!hasEmail || !hasPassword) {
            throw new IllegalStateException(
                    "AUTH_BOOTSTRAP_EMAIL and AUTH_BOOTSTRAP_PASSWORD must be provided together");
        }

        String normalizedEmail = bootstrapEmail.trim().toLowerCase(Locale.ROOT);
        if (userAccountRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            LOGGER.info("Bootstrap user {} already exists; leaving it unchanged", normalizedEmail);
            return;
        }

        UserAccount user = new UserAccount(normalizedEmail, passwordEncoder.encode(bootstrapPassword), true);
        userAccountRepository.save(user);
        LOGGER.info("Created bootstrap authentication user {}", normalizedEmail);
    }
}
