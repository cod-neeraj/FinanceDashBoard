package Finance.Finanace.DTO.Response;

import Finance.Finanace.Models.Enums.Role;
import Finance.Finanace.Models.Enums.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
