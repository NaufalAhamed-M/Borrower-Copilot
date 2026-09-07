package com.borrowercopilot.service;

import com.borrowercopilot.dto.AssessmentRequest;
import com.borrowercopilot.dto.AssessmentResponse.StressResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class StressService {
    private final AffordabilityService affordability;

    public StressService(AffordabilityService affordability) {
        this.affordability = affordability;
    }

    public StressResult run(AssessmentRequest r, BigDecimal proposedEmi) {
        BigDecimal ordinaryIncome = affordability.monthlyIncome(r);
        BigDecimal stressedIncome = ordinaryIncome.multiply(new BigDecimal("0.80"));
        BigDecimal existingEmi = affordability.existingEmiForAffordability(r);
        BigDecimal totalEmi = existingEmi.add(proposedEmi);
        BigDecimal stressedFoir = totalEmi
                .divide(stressedIncome, 8, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        String outcome = stressedFoir.compareTo(new BigDecimal("50")) > 0
                ? "Stretched: a 20% income drop would push total EMI above 50% of income."
                : stressedFoir.compareTo(new BigDecimal("40")) > 0
                ? "Caution: a 20% income drop would make repayment meaningfully tighter."
                : "The 20% income-drop scenario remains within the conservative stress boundary.";

        return new StressResult("Income falls by 20%", stressedIncome.setScale(2, RoundingMode.HALF_UP), proposedEmi, stressedFoir, outcome);
    }
}
