package com.borrowercopilot.service;

import com.borrowercopilot.dto.AssessmentRequest;
import com.borrowercopilot.dto.AssessmentResponse;
import com.borrowercopilot.dto.AssessmentResponse.MoneyRange;
import com.borrowercopilot.model.Enums.IncomeStability;
import com.borrowercopilot.model.Enums.IncomeType;
import com.borrowercopilot.model.Enums.LoanType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class LoanAmountService {
    private final MathService math;
    private final AffordabilityService affordability;
    private final RateService rateService;

    public LoanAmountService(MathService math, AffordabilityService affordability, RateService rateService) {
        this.math = math;
        this.affordability = affordability;
        this.rateService = rateService;
    }

    public MoneyRange safeRange(AssessmentRequest r) {
        BigDecimal maxEmi = affordability.maxNewEmi(r);
        if (maxEmi.compareTo(new BigDecimal("2000")) < 0)
            return new MoneyRange(BigDecimal.ZERO, BigDecimal.ZERO);

        AssessmentResponse.RateRange rate = rateService.fairRate(r);
        int months = termFor(r.loanType());
        BigDecimal upper = loanFromEmi(maxEmi, rate.maxPercent(), months);
        upper = applyProductCap(r, upper);
        upper = math.roundHundred(upper.max(BigDecimal.ZERO));

        boolean keyUnknown = r.creditScore() == null
                || r.incomeStability() == null
                || r.incomeStability() == IncomeStability.UNKNOWN
                || r.rent() == null
                || r.emergencySavingsMonths() == null
                || r.essentialExpenses() == null
                || r.existingEmi() == null;
        BigDecimal lowerFactor = keyUnknown ? new BigDecimal("0.70") : new BigDecimal("0.85");
        BigDecimal lower = math.roundHundred(upper.multiply(lowerFactor));
        return new MoneyRange(lower, upper.max(lower));
    }

    public MoneyRange lenderRange(AssessmentRequest r) {
        BigDecimal income = lenderQualifyingIncome(r);
        BigDecimal multiplier = switch (r.loanType()) {
            case PERSONAL -> new BigDecimal("18");
            case HOME -> new BigDecimal("60");
            case LAP -> new BigDecimal("48");
            case GOLD -> new BigDecimal("12");
            case TWO_WHEELER -> new BigDecimal("24");
            case BUSINESS -> new BigDecimal("24");
        };

        BigDecimal base = income.multiply(multiplier);

        if (r.creditScore() != null && r.creditScore() >= 750) {
            base = base.multiply(new BigDecimal("1.10"));
        } else if (r.creditScore() != null && r.creditScore() < 650) {
            base = base.multiply(new BigDecimal("0.65"));
        } else if (r.creditScore() == null) {
            base = base.multiply(new BigDecimal("0.90"));
        }

        if (r.incomeStability() == IncomeStability.HIGHLY_VARIABLE) {
            base = base.multiply(new BigDecimal("0.85"));
        }
        if (r.age() >= 65) {
            base = base.multiply(new BigDecimal("0.70"));
        } else if (r.age() >= 55) {
            base = base.multiply(new BigDecimal("0.85"));
        }

        if (r.existingEmi() == null) {
            base = base.multiply(new BigDecimal("0.85"));
        } else if (r.existingEmi().signum() > 0 && r.existingEmiMonthsRemaining() != null
                && r.existingEmiMonthsRemaining() <= 12) {
            base = base.multiply(new BigDecimal("1.05"));
        }

        if ((r.loanType() == LoanType.LAP || r.loanType() == LoanType.BUSINESS)
                && r.collateralValue() != null
                && Boolean.TRUE.equals(r.collateralUnencumbered())) {
            BigDecimal collateralIllustration = r.collateralValue().multiply(new BigDecimal("0.60"));
            base = base.max(collateralIllustration);
        }

        BigDecimal high = math.roundHundred(base.max(BigDecimal.ZERO).min(r.amountWanted()));
        BigDecimal low = math.roundHundred(high.multiply(new BigDecimal("0.75")));
        return new MoneyRange(low, high.max(low));
    }

    private BigDecimal lenderQualifyingIncome(AssessmentRequest r) {
        BigDecimal income = affordability.monthlyIncome(r);
        if (r.incomeType() == IncomeType.SELF_EMPLOYED
                && r.documentedAnnualIncome() != null
                && r.documentedAnnualIncome().signum() > 0) {
            BigDecimal documentedMonthly = r.documentedAnnualIncome()
                    .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
            income = income.min(documentedMonthly);
        }
        return income;
    }

    private BigDecimal applyProductCap(AssessmentRequest r, BigDecimal amount) {
        if (r.loanType() == LoanType.TWO_WHEELER) return amount.min(r.amountWanted());
        return amount;
    }

    private BigDecimal loanFromEmi(BigDecimal emi, BigDecimal annualRate, int months) {
        if (emi.signum() <= 0)
            return BigDecimal.ZERO;

        double payment = emi.doubleValue();
        double monthlyRate = annualRate.doubleValue() / 1200.0;
        if (monthlyRate == 0)
            return emi.multiply(BigDecimal.valueOf(months));

        double principal = payment * (1 - Math.pow(1 + monthlyRate, -months)) / monthlyRate;

        return BigDecimal.valueOf(principal).setScale(0, RoundingMode.HALF_UP);
    }

    public int termFor(LoanType type) {
        return switch (type) {
            case PERSONAL -> 60;
            case HOME -> 240;
            case LAP -> 180;
            case GOLD -> 24;
            case TWO_WHEELER -> 48;
            case BUSINESS -> 84;
        };
    }
}
