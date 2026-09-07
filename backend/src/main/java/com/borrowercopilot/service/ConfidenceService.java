package com.borrowercopilot.service;

import com.borrowercopilot.dto.AssessmentRequest;
import com.borrowercopilot.model.Enums.*;
import org.springframework.stereotype.Service;

@Service
public class ConfidenceService {
    public Result score(AssessmentRequest r) {
        int known = 0;
        int material = 0;

        material++; if (r.monthlyIncomeMin() != null) known++;
        material++; if (r.existingEmi() != null) known++;
        material++; if (r.existingEmi() == null || r.existingEmi().signum() == 0 || r.existingEmiMonthsRemaining() != null) known++;
        material++; if (r.essentialExpenses() != null) known++;
        material++; if (r.rent() != null) known++;
        material++; if (r.creditScore() != null) known++;
        material++; if (r.incomeStability() != null && r.incomeStability() != IncomeStability.UNKNOWN) known++;
        material++; if (r.emergencySavingsMonths() != null) known++;
        if (r.incomeStability() != IncomeStability.STABLE) {
            material++; if (r.variableIncomeShare() != null) known++;
        }
        material++; if (r.hasCreditHistory() != null) known++;

        if (Boolean.TRUE.equals(r.hasCreditHistory())) {
            material++; if (r.recentBounceCount() != null) known++;
            material++; if (r.hasHighCostDebt() != null) known++;
            if (Boolean.TRUE.equals(r.hasHighCostDebt())) {
                material++; if (r.highCostLoanCount() != null) known++;
                material++; if (r.highCostDebtOutstanding() != null) known++;
                material++; if (r.highCostDebtRatePercent() != null) known++;
            }
        }

        if (r.incomeType() == IncomeType.SELF_EMPLOYED) {
            material++; if (r.documentedAnnualIncome() != null) known++;
        }
        if (r.loanType() == LoanType.BUSINESS || r.loanType() == LoanType.TWO_WHEELER) {
            material++; if (r.expectedMonthlyIncomeIncrease() != null) known++;
        }
        if (r.loanType() == LoanType.LAP || r.loanType() == LoanType.BUSINESS) {
            material++; if (r.collateralValue() != null) known++;
            material++; if (r.collateralUnencumbered() != null) known++;
        }
        if (Boolean.TRUE.equals(r.coApplicant())) {
            material++; if (r.coApplicantMonthlyIncome() != null) known++;
        }

        double ratio = material == 0 ? 0 : (double) known / material;
        boolean importantUnknown = r.creditScore() == null || r.rent() == null || r.emergencySavingsMonths() == null
                || r.essentialExpenses() == null || r.existingEmi() == null
                || r.incomeStability() == null || r.incomeStability() == IncomeStability.UNKNOWN
                || (r.incomeStability() != IncomeStability.STABLE && r.variableIncomeShare() == null);
        if (ratio >= 0.82 && !importantUnknown) return new Result(Confidence.HIGH, "Most material affordability, resilience and pricing inputs are known.");
        if (ratio >= 0.55) return new Result(Confidence.MEDIUM, "Core income/debt information is known, but some pricing or resilience inputs are missing.");
        return new Result(Confidence.LOW, "Several material inputs are unknown, so the displayed ranges are deliberately wider.");
    }

    public record Result(Confidence confidence, String reason) {}
}
