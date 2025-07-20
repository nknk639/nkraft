package com.nkraft.budget.service;

import com.nkraft.budget.dto.CategoryReportDTO;
import com.nkraft.budget.dto.ReportDTO;
import com.nkraft.budget.entity.Category;
import com.nkraft.budget.entity.Transaction;
import com.nkraft.budget.entity.TransactionStatus;
import com.nkraft.budget.repository.TransactionRepository;
import com.nkraft.user.entity.NkraftUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public ReportDTO generateReport(NkraftUser user, YearMonth targetMonth) {
        List<Transaction> transactions = transactionRepository.findMainAccountExpendituresForMonth(user, targetMonth.getYear(), targetMonth.getMonthValue());

        // カテゴリ別集計
        Map<String, List<Transaction>> groupedByCategory = transactions.stream()
                .collect(Collectors.groupingBy(t -> t.getCategory() != null ? t.getCategory().getCategoryName() : "未分類"));

        List<CategoryReportDTO> categoryReports = groupedByCategory.entrySet().stream()
                .map(entry -> {
                    String categoryName = entry.getKey();
                    List<Transaction> categoryTransactions = entry.getValue();

                    BigDecimal planned = categoryTransactions.stream()
                            .map(Transaction::getPlannedAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal actual = categoryTransactions.stream()
                            .filter(t -> t.getTransactionStatus() == TransactionStatus.COMPLETED)
                            .map(Transaction::getActualAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new CategoryReportDTO(categoryName, planned, actual);
                })
                .filter(dto -> dto.getTotalPlanned().compareTo(BigDecimal.ZERO) > 0 || dto.getTotalActual().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(CategoryReportDTO::getCategoryName))
                .collect(Collectors.toList());

        // 全体集計
        BigDecimal totalPlanned = categoryReports.stream().map(CategoryReportDTO::getTotalPlanned).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalActual = categoryReports.stream().map(CategoryReportDTO::getTotalActual).reduce(BigDecimal.ZERO, BigDecimal::add);

        // 最古の取引月を取得
        Optional<LocalDate> oldestDateOpt = transactionRepository.findOldestExpenditureDate(user);
        boolean isOldestMonth = oldestDateOpt.map(oldestDate -> !targetMonth.isAfter(YearMonth.from(oldestDate))).orElse(true);

        // 取引明細は日付の降順でソート
        transactions.sort(Comparator.comparing(Transaction::getTransactionDate).reversed());

        return ReportDTO.builder()
                .targetMonth(targetMonth)
                .prevMonth(targetMonth.minusMonths(1))
                .nextMonth(targetMonth.plusMonths(1))
                .isOldestMonth(isOldestMonth)
                .totalPlanned(totalPlanned)
                .totalActual(totalActual)
                .totalDifference(totalPlanned.subtract(totalActual))
                .categoryReports(categoryReports)
                .transactions(transactions)
                .build();
    }
}