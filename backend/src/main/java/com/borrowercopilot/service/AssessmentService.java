package com.borrowercopilot.service;

import com.borrowercopilot.dto.AssessmentRequest;
import com.borrowercopilot.dto.AssessmentResponse;
import com.borrowercopilot.dto.AssessmentResponse.*;
import com.borrowercopilot.model.Enums.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class AssessmentService {
    private final AffordabilityService affordability;
    private final LoanAmountService loanAmount;
    private final RateService rateService;
    private final DecisionService decisionService;
    private final StressService stressService;
    private final ConfidenceService confidenceService;
    private final ProductRoutingService productRouting;
    private final MathService math;

    public AssessmentService(AffordabilityService affordability, LoanAmountService loanAmount,
                             RateService rateService, DecisionService decisionService,
                             StressService stressService, ConfidenceService confidenceService,
                             ProductRoutingService productRouting, MathService math) {
        this.affordability = affordability;
        this.loanAmount = loanAmount;
        this.rateService = rateService;
        this.decisionService = decisionService;
        this.stressService = stressService;
        this.confidenceService = confidenceService;
        this.productRouting = productRouting;
        this.math = math;
    }

    public AssessmentResponse assess(AssessmentRequest r) {
        validate(r);
        RateRange rate = rateService.fairRate(r);
        BigDecimal maxEmi = affordability.maxNewEmi(r);
        MoneyRange lender = loanAmount.lenderRange(r);
        MoneyRange safe = loanAmount.safeRange(r);
        DecisionService.Result decision = decisionService.decide(r, safe);

        BigDecimal recommended = decision.decision() == Decision.DONT_BORROW ? BigDecimal.ZERO
                : math.roundHundred(math.min(r.amountWanted(), safe.min()));

        BigDecimal displayPrincipal = recommended.signum() > 0 ? recommended : r.amountWanted();
        List<TenureOption> options = new ArrayList<>();
        for (int months : tenureMonths(r.loanType())) {
            BigDecimal emi = math.emi(displayPrincipal, rate.maxPercent(), months);
            options.add(new TenureOption(months, emi, math.totalInterest(displayPrincipal, rate.maxPercent(), months)));
        }

        BigDecimal stressEmi = options.isEmpty() ? BigDecimal.ZERO : options.get(0).emi();
        StressResult stress = stressService.run(r, stressEmi);
        ConfidenceService.Result confidence = confidenceService.score(r);
        ProductRecommendation product = productRouting.route(r);

        BigDecimal aprMin = BigDecimal.ZERO;
        BigDecimal aprMax = BigDecimal.ZERO;
        if (recommended.signum() > 0) {
            int aprMonths = tenureMonths(r.loanType())[0];
            BigDecimal feeMin = recommended.multiply(new BigDecimal("0.005"));
            BigDecimal feeMax = recommended.multiply(new BigDecimal("0.020"));
            aprMin = math.effectiveApr(recommended, rate.minPercent(), aprMonths, feeMin);
            aprMax = math.effectiveApr(recommended, rate.maxPercent(), aprMonths, feeMax);
        }

        List<String> assumptions = buildAssumptions(r);
        List<String> reasons = buildReasons(r, safe, maxEmi, product, options);

        String headline = switch (decision.decision()) {
            case BORROW -> "BORROW — within your safe range";
            case BORROW_LESS -> "BORROW LESS — lender capacity may exceed safe capacity";
            case DONT_BORROW -> "DON'T BORROW — pause and reduce repayment pressure";
        };

        NegotiationCard card = new NegotiationCard(
                headline,
                r.loanType().name().replace('_', ' '),
                r.amountWanted(),
                lender,
                safe,
                recommended,
                rate,
                maxEmi,
                buildNegotiationAsk(decision.decision(), rate, maxEmi),
                reasons,
                confidence.confidence());

        return new AssessmentResponse(
                r.name(),
                decision.decision(),
                decision.reason(),
                lender,
                safe,
                recommended,
                rate,
                maxEmi,
                affordability.monthlyIncome(r),
                affordability.qualifyingIncome(r),
                affordability.essentialExpensesUsed(r),
                affordability.essentialExpensesWereEstimated(r),
                affordability.rentUsed(r),
                affordability.rentWasEstimated(r),
                r.existingEmi(),
                affordability.existingEmiForAffordability(r),
                affordability.existingEmiWasEstimated(r),
                r.existingEmiMonthsRemaining(),
                options,
                stress,
                product,
                confidence.confidence(),
                confidence.reason(),
                new AprEstimate(aprMin, aprMax,
                        "Illustrative only: based on the recommended principal, fair-rate band, first displayed tenure and assumed mandatory fees of 0.5%–2.0%. Use the lender's official KFS/APR for the contractual figure."),
                assumptions,
                card);
    }

    private String buildNegotiationAsk(Decision decision, RateRange rate, BigDecimal maxEmi) {
        if (decision == Decision.DONT_BORROW) {
            return "Do not add a new obligation yet. Reduce existing repayment pressure and reassess when the new EMI can stay within the borrower-safe ceiling of " + money(maxEmi) + ".";
        }
        if (decision == Decision.BORROW_LESS) {
            return "Ask the lender to reduce the amount so the EMI stays at or below " + money(maxEmi) + " and keep the rate within the fair band of " + rateText(rate) + ".";
        }
        return "Ask the lender to keep the rate within the fair band where possible, keep the EMI at or below " + money(maxEmi) + " and disclose the complete KFS/APR and every mandatory fee before you agree.";
    }

    private List<String> buildAssumptions(AssessmentRequest r) {
        List<String> a = new ArrayList<>();
        a.add("FOIR-style thresholds are borrower-side product judgements, not claims that RBI mandates these exact percentages.");
        a.add("Likely lender range is an indicative capacity estimate, not an approval prediction.");
        a.add("Fair rate is a market-informed estimate; actual pricing depends on lender underwriting, product and current offer.");
        a.add("Illustrative APR uses assumed mandatory upfront fees of 0.5%–2.0%; replace them with the lender's actual KFS charges.");
        a.add("Residential rent is asked for every borrower. If rent is left blank, V1 uses a 10% of ordinary monthly income housing-cost fallback; entering ₹0 means no residential rent is being paid.");
        a.add("Essential household expenses exclude rent. If left blank, V1 uses a 25% of ordinary monthly income fallback rather than treating unknown expenses as ₹0.");
        a.add("Unknown values remain unknown; fallbacks are explicit conservative product assumptions, not claims about the borrower's actual finances.");
        a.add("The borrower-safe range is calculated independently of the lender range and is constrained by repayment capacity.");
        a.add("The conservative end of the safe range is the recommended number to negotiate around.");
        a.add("Existing EMI is counted in today's cash flow until its stated remaining tenure ends. If existing EMI is unknown, V1 reserves 15% of ordinary income as a conservative debt-payment proxy and widens the safe range.");
        a.add("Only 50% of self-reported co-applicant income and 50% of expected productive-loan income are counted for borrower affordability.");
        a.add("Age affects indicative lender capacity only from age 55 onward; this is a product judgement, not a lender rule.");

        if (r.creditScore() == null) a.add("Credit score was unknown, so the fair-rate band is wider and confidence is lower.");
        if (r.incomeStability() == null || r.incomeStability() == IncomeStability.UNKNOWN)
            a.add("Income stability was unknown, so affordability uses a conservative stability adjustment.");
        if (r.rent() == null) a.add("Residential rent was unknown; a 10% of ordinary-income fallback was used rather than silently treating rent as ₹0.");
        if (r.emergencySavingsMonths() == null) a.add("Emergency savings were unknown; no positive savings benefit was assumed and a small resilience haircut was applied.");
        if (r.hasCreditHistory() == null) a.add("Credit history was unknown; repayment-bounce and high-cost-debt status were not invented.");
        if (Boolean.TRUE.equals(r.hasCreditHistory()) && r.recentBounceCount() == null)
            a.add("Repayment-bounce history was unknown; it was not treated as zero.");
        if (Boolean.TRUE.equals(r.hasCreditHistory()) && Boolean.TRUE.equals(r.hasHighCostDebt()) && r.highCostLoanCount() == null)
            a.add("The number of high-cost/app loans was unknown; it was not treated as zero.");
        if (Boolean.TRUE.equals(r.hasCreditHistory()) && Boolean.TRUE.equals(r.hasHighCostDebt()) && r.highCostDebtOutstanding() == null)
            a.add("High-cost debt balance was unknown; no debt balance was invented.");
        if (Boolean.TRUE.equals(r.hasCreditHistory()) && Boolean.TRUE.equals(r.hasHighCostDebt()) && r.highCostDebtRatePercent() == null)
            a.add("High-cost debt rate was unknown; no rate was invented.");
        if (r.existingEmi() == null)
            a.add("Existing EMI was unknown; V1 used a 15% of ordinary-income reserve for affordability instead of assuming ₹0.");
        else if (r.existingEmi().signum() > 0)
            a.add("Existing EMI of ₹" + r.existingEmi().toPlainString() + " has " + r.existingEmiMonthsRemaining() + " month(s) remaining and is included in current affordability.");
        if (r.essentialExpenses() == null)
            a.add("Essential household expenses were estimated using the documented 25% ordinary-income fallback.");
        return a;
    }

    private List<String> buildReasons(AssessmentRequest r, MoneyRange safe, BigDecimal maxEmi,
                                      ProductRecommendation product, List<TenureOption> options) {
        List<String> reasons = new ArrayList<>();
        if (r.essentialExpenses() == null) {
            reasons.add("Essential household expenses were not provided, so V1 used 25% of ordinary monthly income instead of treating the unknown as ₹0.");
        } else {
            reasons.add("Safe EMI uses the essential household expenses you supplied.");
        }
        if (r.rent() == null) {
            reasons.add("Residential rent was not provided, so V1 used a 10% of ordinary monthly income housing-cost fallback.");
        } else {
            reasons.add("Residential rent of ₹" + r.rent().toPlainString() + " is included as a household cash outflow.");
        }
        reasons.add("Safe EMI also accounts for existing debt and the resilience adjustments you supplied.");
        reasons.add("Fair rate reflects product type plus credit score, income stability, variable income and repayment/debt signals.");
        reasons.add("The safe amount is converted from the maximum new EMI using the upper fair-rate bound, so it does not depend on an optimistic rate.");
        if (r.existingEmi() != null && r.existingEmi().signum() > 0) {
            reasons.add("Your existing EMI is ₹" + r.existingEmi().toPlainString() + " with " + r.existingEmiMonthsRemaining() + " month(s) remaining; it reduces today's new-EMI room.");
        } else if (r.existingEmi() == null) {
            reasons.add("Existing EMI was not known, so the app reserved 15% of ordinary income rather than assuming no existing debt.");
        }
        if (Boolean.TRUE.equals(r.hasHighCostDebt()) && r.highCostLoanCount() != null && r.highCostLoanCount() >= 3) {
            reasons.add(r.highCostLoanCount() + " high-cost/app loans were reported, so affordability is reduced and the fair-rate band is widened.");
        }
        if (Boolean.TRUE.equals(r.hasHighCostDebt()) && r.highCostDebtOutstanding() != null && r.highCostDebtOutstanding().signum() > 0) {
            reasons.add("High-cost debt outstanding of ₹" + r.highCostDebtOutstanding().toPlainString() + " is treated as a repayment-pressure signal.");
        }
        if (r.recentBounceCount() != null && r.recentBounceCount() > 0) {
            reasons.add("Recent repayment bounces/missed payments increase both repayment-stress and pricing risk.");
        }
        if (product.product().toLowerCase().contains("secured")) {
            reasons.add("Your reported unencumbered collateral may make a secured route worth comparing with unsecured borrowing.");
        }
        if (r.amountWanted().compareTo(safe.max()) > 0) {
            reasons.add("The requested amount exceeds the borrower-safe maximum, so negotiate around the safe range rather than the lender range.");
        }
        if (!options.isEmpty() && options.get(options.size() - 1).emi().compareTo(maxEmi) > 0) {
            reasons.add("Even the longest displayed tenure is above the comfortable EMI ceiling for the displayed principal; a smaller amount is safer.");
        }
        return reasons;
    }

    private void validate(AssessmentRequest r) {
        if (r.monthlyIncomeMax() != null && r.monthlyIncomeMax().compareTo(r.monthlyIncomeMin()) < 0)
            throw new IllegalArgumentException("Maximum monthly income cannot be below minimum monthly income.");
        if (r.rent() != null && r.rent().compareTo(affordability.monthlyIncome(r)) > 0)
            throw new IllegalArgumentException("Monthly rent cannot exceed the stated monthly income.");
        if (r.essentialExpenses() != null && r.essentialExpenses().add(r.rent() == null ? BigDecimal.ZERO : r.rent()).compareTo(affordability.monthlyIncome(r)) > 0)
            throw new IllegalArgumentException("Essential expenses plus rent cannot exceed the stated monthly income.");
        if (r.existingEmi() != null && r.existingEmi().signum() > 0 && r.existingEmiMonthsRemaining() == null)
            throw new IllegalArgumentException("Please enter the remaining tenure in months for your existing EMI.");
        if (r.existingEmi() == null && r.existingEmiMonthsRemaining() != null)
            throw new IllegalArgumentException("Existing EMI tenure cannot be supplied when the existing EMI amount is unknown.");
        if (r.existingEmi() != null && r.existingEmi().signum() == 0 && r.existingEmiMonthsRemaining() != null)
            throw new IllegalArgumentException("Existing EMI tenure must be blank when existing EMI is ₹0.");
        if (r.hasCreditHistory() != null && !r.hasCreditHistory()
                && ((r.recentBounceCount() != null && r.recentBounceCount() > 0)
                || (r.highCostLoanCount() != null && r.highCostLoanCount() > 0)
                || (r.highCostDebtOutstanding() != null && r.highCostDebtOutstanding().signum() > 0)
                || (r.highCostDebtRatePercent() != null && r.highCostDebtRatePercent().signum() > 0)))
            throw new IllegalArgumentException("Repayment-history and high-cost-debt details cannot be supplied when the borrower says they have no credit history.");
        if (Boolean.FALSE.equals(r.hasHighCostDebt())
                && ((r.highCostLoanCount() != null && r.highCostLoanCount() > 0)
                || (r.highCostDebtOutstanding() != null && r.highCostDebtOutstanding().signum() > 0)
                || (r.highCostDebtRatePercent() != null && r.highCostDebtRatePercent().signum() > 0)))
            throw new IllegalArgumentException("High-cost debt details cannot be supplied when the borrower says there is no high-cost/app debt.");
        if (Boolean.TRUE.equals(r.hasHighCostDebt()) && r.highCostLoanCount() != null && r.highCostLoanCount() == 0)
            throw new IllegalArgumentException("Enter at least 1 high-cost/app loan, or choose No if there is no high-cost debt.");
        if (Boolean.TRUE.equals(r.hasHighCostDebt()) && Boolean.FALSE.equals(r.hasCreditHistory()))
            throw new IllegalArgumentException("High-cost/app debt requires credit history to be Yes or unknown.");
        if (r.highCostLoanCount() != null && r.highCostLoanCount() > 0 && r.hasCreditHistory() != null && !r.hasCreditHistory())
            throw new IllegalArgumentException("High-cost loans imply existing credit history; select Yes or leave the history unknown.");
        if (Boolean.TRUE.equals(r.coApplicant()) && r.coApplicantMonthlyIncome() == null)
            throw new IllegalArgumentException("Enter the co-applicant's monthly income when a co-applicant is selected.");
        if (Boolean.FALSE.equals(r.coApplicant()) && r.coApplicantMonthlyIncome() != null)
            throw new IllegalArgumentException("Co-applicant income cannot be supplied when no co-applicant is selected.");
        if (r.incomeType() != IncomeType.SELF_EMPLOYED && r.documentedAnnualIncome() != null)
            throw new IllegalArgumentException("Documented annual income is only applicable to self-employed borrowers in V1.");
        if (!(r.loanType() == LoanType.BUSINESS || r.loanType() == LoanType.TWO_WHEELER)
                && r.expectedMonthlyIncomeIncrease() != null)
            throw new IllegalArgumentException("Expected productive income is only applicable to business or two-wheeler borrowing in V1.");
        if (!(r.loanType() == LoanType.LAP || r.loanType() == LoanType.BUSINESS)
                && (r.collateralValue() != null || r.collateralUnencumbered() != null))
            throw new IllegalArgumentException("Collateral fields are only applicable to LAP or business borrowing in V1.");
    }

    private String money(BigDecimal value) {
        return "₹" + value.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private String rateText(RateRange rate) {
        return rate.minPercent().toPlainString() + "%–" + rate.maxPercent().toPlainString() + "%";
    }

    private int[] tenureMonths(LoanType type) {
        return switch (type) {
            case PERSONAL -> new int[]{36, 48, 60};
            case HOME -> new int[]{120, 180, 240};
            case LAP -> new int[]{60, 120, 180};
            case GOLD -> new int[]{12, 18, 24};
            case TWO_WHEELER -> new int[]{24, 36, 48};
            case BUSINESS -> new int[]{36, 60, 84};
        };
    }
}
