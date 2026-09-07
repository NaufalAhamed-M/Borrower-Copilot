package com.borrowercopilot.service;

import com.borrowercopilot.dto.AssessmentRequest;
import com.borrowercopilot.dto.AssessmentResponse.MoneyRange;
import com.borrowercopilot.model.Enums.Decision;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DecisionService {
    private static final BigDecimal SEVERE_FOIR = new BigDecimal("50");
    private static final BigDecimal MIN_NEW_EMI = new BigDecimal("2000");
    private final AffordabilityService affordability;
    private final MathService math;
    private final RateService rateService;
    private final LoanAmountService loanAmountService;

    public DecisionService(AffordabilityService affordability, MathService math,
                           RateService rateService, LoanAmountService loanAmountService) {
        this.affordability = affordability;
        this.math = math;
        this.rateService = rateService;
        this.loanAmountService = loanAmountService;
    }

    public Result decide(AssessmentRequest r, MoneyRange safe) {
        BigDecimal maxEmi = affordability.maxNewEmi(r);
        BigDecimal existingFoir = affordability.currentFoir(r);

        boolean recentBounce = Boolean.TRUE.equals(r.hasCreditHistory()) && r.recentBounceCount() != null && r.recentBounceCount() > 0;
        boolean highCostDebt = Boolean.TRUE.equals(r.hasHighCostDebt()) && r.highCostDebtOutstanding() != null && r.highCostDebtOutstanding().signum() > 0;
        boolean manyHighCostLoans = r.highCostLoanCount() != null && r.highCostLoanCount() >= 3;

        BigDecimal requestedEmi = math.emi(r.amountWanted(),
                rateService.fairRate(r).maxPercent(),
                loanAmountService.termFor(r.loanType()));

        if (existingFoir.compareTo(SEVERE_FOIR) > 0) {
            return new Result(Decision.DONT_BORROW,
                    "Your current debt burden is already above 50% of ordinary income, so adding another EMI is not resilient.");
        }
        if (maxEmi.compareTo(MIN_NEW_EMI) < 0) {
            return new Result(Decision.DONT_BORROW,
                    "After essential costs, housing, existing obligations and resilience adjustments, less than ₹2,000 remains as a safe new-EMI ceiling.");
        }
        if (recentBounce && highCostDebt) {
            return new Result(Decision.DONT_BORROW,
                    "A recent repayment problem combined with outstanding high-cost debt indicates too much current repayment stress for another loan.");
        }
        if (recentBounce && manyHighCostLoans && r.existingEmi() == null) {
            return new Result(Decision.DONT_BORROW,
                    "Multiple high-cost loans and a recent repayment problem are present, while the existing EMI burden is unknown; the app takes the safer position and recommends pausing new borrowing.");
        }
        if (r.amountWanted().compareTo(safe.max()) > 0 || requestedEmi.compareTo(maxEmi) > 0) {
            if (r.amountWanted().compareTo(safe.max()) > 0) {
                return new Result(Decision.BORROW_LESS,
                        "The amount requested is above the borrower-safe range even if a lender may sanction more.");
            }
            return new Result(Decision.BORROW_LESS,
                    "The requested amount has an indicative EMI above your borrower-safe monthly ceiling at the conservative fair-rate assumption.");
        }

        return new Result(Decision.BORROW,
                "Your requested amount fits within the current borrower-safe range and indicative repayment ceiling based on the information provided.");
    }

    public record Result(Decision decision, String reason) {}
}
