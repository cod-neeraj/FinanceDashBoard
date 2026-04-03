package Finance.Finanace.DTO.Request;

import Finance.Finanace.Models.Enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecordFilterRequest {
    private TransactionType type;

    private String category;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate to;

    private int page = 0;
    private int size = 20;
}
