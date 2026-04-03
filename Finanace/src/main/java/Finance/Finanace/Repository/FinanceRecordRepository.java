package Finance.Finanace.Repository;

import Finance.Finanace.DTO.Response.FinancialRecordResponse;
import Finance.Finanace.Models.Enums.TransactionType;
import Finance.Finanace.Models.FinancialRecord;
import lombok.Builder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FinanceRecordRepository extends JpaRepository<FinancialRecord,Long> {
    @Query("""
    SELECT
        COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), 0),
        COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0)
    FROM FinancialRecord t
""")
    Object[] getIncomeAndExpense();

    @Query("""
    SELECT t.category, COALESCE(SUM(t.amount), 0)
    FROM FinancialRecord t
    GROUP BY t.category
""")
    List<Object[]> getCategoryWiseSum();

    @Query("""
    SELECT new Finance.Finanace.DTO.Response.FinancialRecordResponse(
        t.id,
        t.amount,
        t.type,
        t.category,
        t.date,
        t.description,
        u.username,
        t.createdAt,
        t.updatedAt
    )
    FROM FinancialRecord t
    LEFT JOIN t.createdBy u
    ORDER BY t.date DESC, t.createdAt DESC
""")
    List<FinancialRecordResponse> findTop10Transactions(Pageable pageable);

    @Query("""
    SELECT
        EXTRACT(MONTH FROM t.date),
        t.type,
        COALESCE(SUM(t.amount), 0)
    FROM FinancialRecord t
    GROUP BY EXTRACT(MONTH FROM t.date), t.type
    ORDER BY EXTRACT(MONTH FROM t.date)
""")
    List<Object[]> getMonthlyTotals();

    @Query("""
    SELECT new Finance.Finanace.DTO.Response.FinancialRecordResponse(
        f.id,
        f.amount,
        f.type,
        f.category,
        f.date,
        f.description,
        f.createdBy.username,
        f.createdAt,
        f.updatedAt
    )
    FROM FinancialRecord f
    WHERE (:category IS NULL OR f.category = :category)
      AND (:type IS NULL OR f.type = :type)
      AND (:startDate IS NULL OR f.date >= :startDate)
      AND (:endDate IS NULL OR f.date <= :endDate)
""")
    Page<FinancialRecordResponse> findRecords(
            @Param("category") String category,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

}


