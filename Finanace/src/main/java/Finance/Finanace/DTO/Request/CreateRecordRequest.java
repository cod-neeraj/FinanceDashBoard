package Finance.Finanace.DTO.Request;

import Finance.Finanace.Models.Enums.TransactionType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateRecordRequest {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0001", message = "Amount must be greater than zero")
    @Digits(integer = 15, fraction = 4, message = "Amount format invalid")
    private BigDecimal amount;

    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category;

    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Transaction date cannot be in the future")
    private LocalDate date;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}
