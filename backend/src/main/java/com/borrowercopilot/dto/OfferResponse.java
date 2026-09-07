package com.borrowercopilot.dto;

import java.math.BigDecimal;

public record OfferResponse(
        String verdict,
        String reason,
        BigDecimal purchaseOrLoanAmount,
        BigDecimal downPayment,
        BigDecimal financedAmount,
        BigDecimal calculatedEmi,
        BigDecimal lenderEmi,
        BigDecimal emiDifferencePercent,
        BigDecimal quotedRatePercent,
        BigDecimal fairRateMax,
        BigDecimal ratePremiumPoints,
        BigDecimal totalRepayment,
        BigDecimal totalInterest,
        BigDecimal upfrontFees,
        BigDecimal illustrativeApr,
        BigDecimal totalFoirPercent,
        String stressOutcome,
        String negotiationAsk
) {}
