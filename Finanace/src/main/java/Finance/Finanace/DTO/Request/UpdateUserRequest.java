package Finance.Finanace.DTO.Request;

import Finance.Finanace.Models.Enums.Role;
import Finance.Finanace.Models.Enums.TransactionType;
import Finance.Finanace.Models.Enums.UserStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {
    @Email(message = "Must be a valid email address")
    private String email;

    private Role role;

    private UserStatus status;
}
