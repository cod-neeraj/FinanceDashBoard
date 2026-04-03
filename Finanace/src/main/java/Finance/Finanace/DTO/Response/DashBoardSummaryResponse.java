package Finance.Finanace.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashBoardSummaryResponse {

    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;
    private Map<String, BigDecimal> categoryTotals;
    private List<FinancialRecordResponse> recentTransactions;
    private Map<String, MonthlySummary> monthlySummary;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MonthlySummary implements Serializable {
        private BigDecimal income;
        private BigDecimal expenses;
        private BigDecimal net;
    }
}
