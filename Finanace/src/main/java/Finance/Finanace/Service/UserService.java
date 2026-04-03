package Finance.Finanace.Service;

import Finance.Finanace.DTO.Request.CreateUserRequest;
import Finance.Finanace.DTO.Request.UpdateUserRequest;
import Finance.Finanace.DTO.Response.UserResponse;
import Finance.Finanace.Exceptions.DuplicateResourceException;
import Finance.Finanace.Exceptions.InvalidOperationException;
import Finance.Finanace.Exceptions.UserNotFoundException;
import Finance.Finanace.Mapper.UserMapper;
import Finance.Finanace.Models.Enums.Role;
import Finance.Finanace.Models.Enums.UserStatus;
import Finance.Finanace.Models.User;
import Finance.Finanace.Repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

   private final UserRepo userRepo;
   private final PasswordEncoder passwordEncoder;
   private final UserMapper userMapper;


    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepo.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(UserStatus.ACTIVE)
                .build();

        User saved = userRepo.save(user);
        log.info("User created: {} with role {}", saved.getUsername(), saved.getRole());
        return userMapper.toResponse(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public List<UserResponse> getAllUsers() {
        return userMapper.toResponseList(userRepo.findAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse getUserById(Long id) {
        return userMapper.toResponse(
                userRepo.findById(id).orElseThrow(() -> UserNotFoundException.withId(id))
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> UserNotFoundException.withId(id));

        if (request.getEmail() != null) {
            if (userRepo.existsByEmail(request.getEmail()) &&
                    !user.getEmail().equals(request.getEmail())) {
                throw new DuplicateResourceException("Email already in use: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        User updated = userRepo.save(user);
        log.info("User updated: {} — role={}, status={}", updated.getUsername(), updated.getRole(), updated.getStatus());
        return userMapper.toResponse(updated);

    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> UserNotFoundException.withId(id));

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new InvalidOperationException("User is already inactive");
        }

        user.setStatus(UserStatus.INACTIVE);
        userRepo.save(user);
        log.info("User deactivated: {}", user.getUsername());
    }
}
