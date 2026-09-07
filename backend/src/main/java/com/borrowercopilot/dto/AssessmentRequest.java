package com.borrowercopilot.dto;

import com.borrowercopilot.model.Enums.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record AssessmentRequest(
        @Size(max = 100)
        String name,
        @NotNull
        LoanType loanType,
        @NotNull @DecimalMin("1000")
        BigDecimal amountWanted,
        @NotBlank
        String purpose,
        @NotNull
        IncomeType incomeType,
        @NotNull @DecimalMin("0.01")
        BigDecimal monthlyIncomeMin,
        @DecimalMin("0")
        BigDecimal monthlyIncomeMax,
        @DecimalMin("0")
        BigDecimal existingEmi,
        @Min(1) @Max(360)
        Integer existingEmiMonthsRemaining,
        @DecimalMin("0")
        BigDecimal essentialExpenses,
        @NotNull @Min(18) @Max(80)
        Integer age,
        @DecimalMin("300") @DecimalMax("900")
        Integer creditScore,
        IncomeStability incomeStability,
        @DecimalMin("0")
        BigDecimal rent,
        @DecimalMin("0")
        BigDecimal emergencySavingsMonths,
        @DecimalMin("0") @DecimalMax("100")
        BigDecimal variableIncomeShare,
        Boolean hasCreditHistory,
        Boolean hasHighCostDebt,
        @Min(0) @Max(100)
        Integer highCostLoanCount,
        @DecimalMin("0")
        BigDecimal highCostDebtOutstanding,
        @DecimalMin("0") @DecimalMax("100")
        BigDecimal highCostDebtRatePercent,
        @Min(0) @Max(100)
        Integer recentBounceCount,
        @DecimalMin("0")
        BigDecimal collateralValue,
        Boolean collateralUnencumbered,
        @DecimalMin("0")
        BigDecimal documentedAnnualIncome,
        @DecimalMin("0")
        BigDecimal expectedMonthlyIncomeIncrease,
        @DecimalMin("0")
        BigDecimal upcomingLargeExpense,
        Boolean coApplicant,
        @DecimalMin("0") BigDecimal coApplicantMonthlyIncome
) {}
