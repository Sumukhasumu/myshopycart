
package com.myshopycart.authservice.config;

import com.myshopycart.authservice.entity.User;
import com.myshopycart.authservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DistributorUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DistributorUserInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        String username = "distributor2";
        String email = "distributor2@example.com";
        String password = "Distributor@123";

        User distributor = userRepository.findByUsername(username)
                .orElseGet(User::new);

        boolean isNewUser = distributor.getId() == null;

        distributor.setUsername(username);
        distributor.setEmail(email);
        distributor.setRole("DISTRIBUTOR");

        // Set the password only for a newly created account.
        // Preserve the existing password on subsequent startups.
        if (isNewUser) {
            distributor.setPassword(passwordEncoder.encode(password));
        }

        userRepository.save(distributor);

        System.out.println(
                "MYSHOPYCART DISTRIBUTOR user initialized: "
                        + username);
    }
}
