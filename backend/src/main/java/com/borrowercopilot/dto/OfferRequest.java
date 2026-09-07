package com.borrowercopilot.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OfferRequest(
        @NotNull @DecimalMin("1000")
        BigDecimal loanAmount,
        @NotNull @DecimalMin("0")
        BigDecimal downPayment,
        @NotNull @DecimalMin("0.01") @jakarta.validation.constraints.DecimalMax("100")
        BigDecimal quotedRatePercent,
        @NotNull @Min(1) @Max(360)
        Integer tenureMonths,
        @NotNull @DecimalMin("0")
        BigDecimal upfrontFees,
        @DecimalMin("0")
        BigDecimal quotedEmi,
        @NotNull
        BigDecimal safeEmiCeiling,
        @NotNull
        BigDecimal fairRateMin,
        @NotNull
        BigDecimal fairRateMax,
        @NotNull
        BigDecimal existingEmi,
        @NotNull @DecimalMin("0.01")
        BigDecimal monthlyIncome
) {}
