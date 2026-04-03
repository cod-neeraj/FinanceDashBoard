package Finance.Finanace.Data;

import Finance.Finanace.Models.Enums.Role;
import Finance.Finanace.Models.Enums.UserStatus;
import Finance.Finanace.Models.User;
import Finance.Finanace.Repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataIntializer implements CommandLineRunner {

    private final UserRepo userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (userRepository.count() == 0) {

            User admin = User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            userRepository.save(admin);
        }
    }
}