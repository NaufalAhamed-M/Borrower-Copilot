package com.borrowercopilot.dto;

import com.borrowercopilot.model.Enums.*;

import java.math.BigDecimal;
import java.util.List;

public record AssessmentResponse(
        String name,
        Decision decision,
        String decisionReason,
        MoneyRange lenderSanction,
        MoneyRange safeAmount,
        BigDecimal recommendedAmount,
        RateRange fairRate,
        BigDecimal maximumSafeEmi,
        BigDecimal monthlyIncome,
        BigDecimal affordabilityIncome,
        BigDecimal essentialExpensesUsed,
        boolean essentialExpensesEstimated,
        BigDecimal rentUsed,
        boolean rentEstimated,
        BigDecimal existingEmi,
        BigDecimal existingEmiUsedForCalculation,
        boolean existingEmiEstimated,
        Integer existingEmiMonthsRemaining,
        List<TenureOption> tenureOptions,
        StressResult stress,
        ProductRecommendation productRecommendation,
        Confidence confidence,
        String confidenceReason,
        AprEstimate illustrativeApr,
        List<String> assumptions,
        NegotiationCard negotiationCard
) {
    public record MoneyRange(BigDecimal min, BigDecimal max) {}
    public record RateRange(BigDecimal minPercent, BigDecimal maxPercent) {}
    public record TenureOption(int months, BigDecimal emi, BigDecimal totalInterest) {}
    public record StressResult(String scenario, BigDecimal stressedIncome, BigDecimal stressedEmi, BigDecimal stressedFoirPercent, String outcome) {}
    public record ProductRecommendation(String product, String reason) {}
    public record AprEstimate(BigDecimal fairRateMinApr, BigDecimal fairRateMaxApr, String explanation) {}
    public record NegotiationCard(String headline, String loan, BigDecimal requested,
                                  MoneyRange lenderRange, MoneyRange safeRange,
                                  BigDecimal recommendedAmount, RateRange fairRate,
                                  BigDecimal maxEmi, String ask, List<String> reasons,
                                  Confidence confidence) {}
}
