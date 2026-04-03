package Finance.Finanace.Service;

import Finance.Finanace.DTO.Response.DashBoardSummaryResponse;
import Finance.Finanace.DTO.Response.FinancialRecordResponse;
import Finance.Finanace.Mapper.FinancialRecordMapper;
import Finance.Finanace.Models.Enums.TransactionType;
import Finance.Finanace.Repository.FinanceRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class DashBoardService {

    private final FinanceRecordRepository recordRepository;
    private final FinancialRecordMapper financialRecordMapper;

    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    @Transactional(readOnly = true)
    @Cacheable(value = "dashboard:summary", key = "#root.methodName")
    public DashBoardSummaryResponse getSummary() {
        log.debug("Cache miss — computing dashboard summary from DB");

        Object [] object = recordRepository.getIncomeAndExpense();
        Object[] obj = (Object[]) object[0];
        BigDecimal totalIncome = (BigDecimal) obj[0];
        BigDecimal totalExpense = (BigDecimal) obj[1];
        BigDecimal netBalance = totalIncome.subtract(totalExpense);

        Map<String, BigDecimal> categoryTotals = buildCategoryTotals();

        List<FinancialRecordResponse> recentTransactions = recordRepository.findTop10Transactions(PageRequest.of(0, 10));

        Map<String, DashBoardSummaryResponse.MonthlySummary> monthlySummary = buildMonthlySummary();

        return DashBoardSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpense)
                .netBalance(netBalance)
                .categoryTotals(categoryTotals)
                .recentTransactions(recentTransactions)
                .monthlySummary(monthlySummary)
                .build();
    }

    private Map<String, BigDecimal> buildCategoryTotals() {
        List<Object[]> rows = recordRepository.getCategoryWiseSum();
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String category = (String) row[0];
            BigDecimal total = (BigDecimal) row[1];
            result.put(category, total);
        }
        return result;
    }

    private Map<String, DashBoardSummaryResponse.MonthlySummary> buildMonthlySummary() {
        List<Object[]> rows = recordRepository.getMonthlyTotals();

        Map<String, BigDecimal> monthlyIncome = new LinkedHashMap<>();
        Map<String, BigDecimal> monthlyExpense = new LinkedHashMap<>();

        for (Object[] row : rows) {
            Integer monthNum = ((Number) row[0]).intValue();
            String month = Month.of(monthNum)
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            TransactionType type = (TransactionType) row[1];
            BigDecimal total = new BigDecimal(row[2].toString());

            if (type == TransactionType.INCOME) {
                monthlyIncome.merge(month, total, BigDecimal::add);
            } else {
                monthlyExpense.merge(month, total, BigDecimal::add);
            }
        }

        Map<String, DashBoardSummaryResponse.MonthlySummary> result = new LinkedHashMap<>();
        monthlyIncome.keySet().forEach(m -> result.put(m, null));
        monthlyExpense.keySet().forEach(m -> result.put(m, null));

        result.replaceAll((month, ignored) -> {
            BigDecimal income = monthlyIncome.getOrDefault(month, BigDecimal.ZERO);
            BigDecimal expense = monthlyExpense.getOrDefault(month, BigDecimal.ZERO);
            return DashBoardSummaryResponse.MonthlySummary.builder()
                    .income(income)
                    .expenses(expense)
                    .net(income.subtract(expense))
                    .build();
        });

        return result;
    }
}
