package com.example.documentservice.service;

import com.example.documentservice.entity.User;
import com.example.documentservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

@Component
public class TestUserDataInitializer implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UserRepository userRepository;

    public TestUserDataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        String testUsername = "testuser";
        String testPassword = "password"; // ToDo: Hash the password before saving in production

        if (userRepository.findByUsername(testUsername).isEmpty()) {
            User testUser = new User();
            testUser.setUsername(testUsername);
            testUser.setPassword(testPassword);
            userRepository.save(testUser);
            LOG.info("Test user '{}' created successfully.", testUsername);
        } else {
            LOG.info("Test user '{}' already exists.", testUsername);
        }
    }
}
