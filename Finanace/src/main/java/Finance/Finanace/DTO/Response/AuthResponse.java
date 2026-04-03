package Finance.Finanace.DTO.Response;

import Finance.Finanace.Models.Enums.Role;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String username;
    private Role role;
    private long expiresIn;
}
