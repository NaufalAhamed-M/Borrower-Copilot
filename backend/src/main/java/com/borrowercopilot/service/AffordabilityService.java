package com.borrowercopilot.service;

import com.borrowercopilot.dto.AssessmentRequest;
import com.borrowercopilot.model.Enums.IncomeStability;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class AffordabilityService {
    private static final BigDecimal EXPENSE_GUARD = new BigDecimal("0.60");
    private static final BigDecimal CO_APPLICANT_FACTOR = new BigDecimal("0.50");
    private static final BigDecimal FUTURE_INCOME_FACTOR = new BigDecimal("0.50");
    private static final BigDecimal DEFAULT_EXPENSE_RATIO = new BigDecimal("0.25");
    private static final BigDecimal DEFAULT_RENT_RATIO = new BigDecimal("0.10");
    private static final BigDecimal UNKNOWN_EXISTING_EMI_RESERVE = new BigDecimal("0.15");

    private final MathService math;

    public AffordabilityService(MathService math) {
        this.math = math;
    }

    public BigDecimal monthlyIncome(AssessmentRequest r) {
        BigDecimal min = r.monthlyIncomeMin();
        BigDecimal max = r.monthlyIncomeMax();
        if (max == null) return min;
        return math.avg(min, max);
    }

    public BigDecimal qualifyingIncome(AssessmentRequest r) {
        BigDecimal income = monthlyIncome(r);
        if (Boolean.TRUE.equals(r.coApplicant()) && r.coApplicantMonthlyIncome() != null) {
            income = income.add(r.coApplicantMonthlyIncome().multiply(CO_APPLICANT_FACTOR));
        }
        if (isProductiveLoan(r) && r.expectedMonthlyIncomeIncrease() != null) {
            income = income.add(r.expectedMonthlyIncomeIncrease().multiply(FUTURE_INCOME_FACTOR));
        }
        return income.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal essentialExpensesUsed(AssessmentRequest r) {
        BigDecimal ordinaryIncome = monthlyIncome(r);
        BigDecimal value = r.essentialExpenses() != null ? r.essentialExpenses() : ordinaryIncome.multiply(DEFAULT_EXPENSE_RATIO);
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public boolean essentialExpensesWereEstimated(AssessmentRequest r) {
        return r.essentialExpenses() == null;
    }

    public BigDecimal rentUsed(AssessmentRequest r) {
        BigDecimal ordinaryIncome = monthlyIncome(r);
        BigDecimal value = r.rent() != null ? r.rent() : ordinaryIncome.multiply(DEFAULT_RENT_RATIO);
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public boolean rentWasEstimated(AssessmentRequest r) {
        return r.rent() == null;
    }

    public BigDecimal safeTotalDebtPayment(AssessmentRequest r) {

        BigDecimal income = qualifyingIncome(r);

        BigDecimal baseRatio = switch (r.incomeType()) {
            case SALARIED -> new BigDecimal("0.40");
            case SELF_EMPLOYED -> new BigDecimal("0.35");
            case INFORMAL_GIG -> new BigDecimal("0.30");
        };

        if (r.incomeStability() == null || r.incomeStability() == IncomeStability.UNKNOWN) {
            baseRatio = baseRatio.subtract(new BigDecimal("0.05"));
        } else if (r.incomeStability() == IncomeStability.HIGHLY_VARIABLE) {
            baseRatio = baseRatio.subtract(new BigDecimal("0.05"));
        }

        BigDecimal result = income.multiply(baseRatio);

        if (r.emergencySavingsMonths() == null) {
            result = result.multiply(new BigDecimal("0.95"));
        } else if (r.emergencySavingsMonths().compareTo(BigDecimal.ONE) < 0) {
            result = result.multiply(new BigDecimal("0.90"));

        } else if (r.emergencySavingsMonths().compareTo(new BigDecimal("3")) < 0) {
            result = result.multiply(new BigDecimal("0.95"));

        } else if (r.emergencySavingsMonths().compareTo(new BigDecimal("6")) >= 0) {
            result = result.multiply(new BigDecimal("1.05"));
        }

        if (Boolean.TRUE.equals(r.hasCreditHistory()) && r.recentBounceCount() != null && r.recentBounceCount() > 0) {
            result = result.multiply(new BigDecimal("0.85"));
        }

        if (Boolean.TRUE.equals(r.hasHighCostDebt()) && r.highCostDebtOutstanding() != null && r.highCostDebtOutstanding().signum() > 0) {
            result = result.multiply(new BigDecimal("0.85"));
        }

        if (Boolean.TRUE.equals(r.hasHighCostDebt()) && r.highCostLoanCount() != null && r.highCostLoanCount() >= 3) {
            result = result.multiply(new BigDecimal("0.95"));
        }

        if (r.variableIncomeShare() != null) {
            if (r.variableIncomeShare().compareTo(new BigDecimal("50")) > 0) {
                result = result.multiply(new BigDecimal("0.90"));
            } else if (r.variableIncomeShare().compareTo(new BigDecimal("30")) > 0) {
                result = result.multiply(new BigDecimal("0.95"));
            }
        }

        if (r.upcomingLargeExpense() != null && r.upcomingLargeExpense().compareTo(monthlyIncome(r).multiply(BigDecimal.valueOf(3))) > 0) {
            result = result.multiply(new BigDecimal("0.90"));
        }
        return result.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal existingEmiForAffordability(AssessmentRequest r) {
        if (r.existingEmi() != null) return r.existingEmi();
        return monthlyIncome(r).multiply(UNKNOWN_EXISTING_EMI_RESERVE).setScale(2, RoundingMode.HALF_UP);
    }

    public boolean existingEmiWasEstimated(AssessmentRequest r) {
        return r.existingEmi() == null;
    }

    public BigDecimal maxNewEmi(AssessmentRequest r) {
        return safeTotalDebtPayment(r)
                .subtract(existingEmiForAffordability(r))
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal currentFoir(AssessmentRequest r) {
        return existingEmiForAffordability(r)
                .divide(monthlyIncome(r), 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isProductiveLoan(AssessmentRequest r) {
        return r.loanType() == com.borrowercopilot.model.Enums.LoanType.BUSINESS
                || r.loanType() == com.borrowercopilot.model.Enums.LoanType.TWO_WHEELER;
    }
}
