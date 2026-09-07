package com.borrowercopilot.service;

import com.borrowercopilot.dto.AssessmentRequest;
import com.borrowercopilot.dto.AssessmentResponse.RateRange;
import com.borrowercopilot.model.Enums.IncomeStability;
import com.borrowercopilot.model.Enums.IncomeType;
import com.borrowercopilot.model.Enums.LoanType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class RateService {
    public RateRange fairRate(AssessmentRequest r) {
        double low = 10.00;
        double high = 16.50;

        switch (r.loanType()) {
            case PERSONAL -> { low = 10.00; high = 16.50; }
            case HOME -> { low = 7.25; high = 10.50; }
            case LAP -> { low = 10.00; high = 13.00; }
            case GOLD -> { low = 8.75; high = 15.00; }
            case TWO_WHEELER -> { low = 9.50; high = 18.00; }
            case BUSINESS -> { low = 10.50; high = 18.00; }
        }

        if (r.creditScore() == null) {
            high += 2.00;
        } else if (r.creditScore() >= 780) {
            low -= 1.00;
            high -= 1.00;
        } else if (r.creditScore() >= 750) {
            low -= 0.50;
            high -= 0.50;
        } else if (r.creditScore() < 650) {
            low += 2.00;
            high += 3.00;
        } else if (r.creditScore() < 700) {
            low += 1.00;
            high += 1.50;
        }

        if (r.incomeStability() == IncomeStability.STABLE) {
            low -= 0.25;
            high -= 0.25;
        } else if (r.incomeStability() == IncomeStability.HIGHLY_VARIABLE) {
            low += 1.00;
            high += 1.50;
        } else if (r.incomeStability() == null || r.incomeStability() == IncomeStability.UNKNOWN) {
            high += 0.75;
        }

        if (r.incomeType() == IncomeType.INFORMAL_GIG) {
            low += 1.50;
            high += 2.00;
        }

        if (r.variableIncomeShare() != null) {
            if (r.variableIncomeShare().compareTo(new BigDecimal("50")) > 0) {
                low += 0.75;
                high += 1.25;
            } else if (r.variableIncomeShare().compareTo(new BigDecimal("30")) > 0) {
                low += 0.50;
                high += 0.75;
            }
        }

        BigDecimal existingEmi = r.existingEmi();
        if (existingEmi != null && existingEmi.compareTo(r.monthlyIncomeMin().multiply(new BigDecimal("0.20"))) > 0) {
            low += 0.50;
            high += 1.00;
        } else if (existingEmi == null) {
            high += 0.75;
        }

        if (Boolean.TRUE.equals(r.hasHighCostDebt()) && r.highCostDebtOutstanding() != null && r.highCostDebtOutstanding().signum() > 0) {
            low += 1.00;
            high += 2.00;
        }
        if (Boolean.TRUE.equals(r.hasHighCostDebt()) && r.highCostLoanCount() != null && r.highCostLoanCount() >= 3) {
            low += 0.50;
            high += 0.75;
        }
        if (Boolean.TRUE.equals(r.hasHighCostDebt()) && r.highCostDebtRatePercent() != null && r.highCostDebtRatePercent().compareTo(new BigDecimal("25")) > 0) {
            low += 0.50;
            high += 1.00;
        }

        if (Boolean.TRUE.equals(r.hasCreditHistory()) && r.recentBounceCount() != null && r.recentBounceCount() > 0) {
            low += 1.00;
            high += 2.00;
        }

        if (r.existingEmi() != null && r.existingEmi().signum() > 0
                && r.existingEmiMonthsRemaining() != null
                && r.existingEmiMonthsRemaining() <= 12) {
            low -= 0.25;
            high -= 0.25;
        }

        low = Math.max(6.50, low);
        high = Math.max(low + 0.50, high);

        return new RateRange(
                BigDecimal.valueOf(low).setScale(2),
                BigDecimal.valueOf(high).setScale(2));
    }
}
