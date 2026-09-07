package com.borrowercopilot.service;

import com.borrowercopilot.dto.OfferRequest;
import com.borrowercopilot.dto.OfferResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class OfferService {
    private final MathService math;

    public OfferService(MathService math) {
        this.math = math;
    }

    public OfferResponse check(OfferRequest r) {
        if (r.upfrontFees() == null) {
            throw new IllegalArgumentException("Enter the lender's mandatory upfront fees; use ₹0 only when the lender confirms there are none.");
        }
        BigDecimal fees = r.upfrontFees();
        if (fees.compareTo(r.loanAmount()) >= 0) {
            throw new IllegalArgumentException("Mandatory upfront fees must be lower than the quoted amount.");
        }
        if (r.downPayment().compareTo(r.loanAmount()) >= 0) {
            throw new IllegalArgumentException("Down payment must be less than the purchase/loan amount.");
        }

        BigDecimal financed = r.loanAmount().subtract(r.downPayment());
        BigDecimal calculatedEmi = math.emi(financed, r.quotedRatePercent(), r.tenureMonths());

        BigDecimal minimumAmortizingEmi = financed.multiply(r.quotedRatePercent()).divide(new BigDecimal("1200"), 2, RoundingMode.HALF_UP);
        if (r.quotedEmi() != null && r.quotedEmi().signum() > 0
                && r.quotedEmi().compareTo(minimumAmortizingEmi) < 0) {
            throw new IllegalArgumentException("The quoted EMI is below the first-month interest for this offer; check the lender quote.");
        }

        BigDecimal lenderEmi = r.quotedEmi() == null || r.quotedEmi().signum() == 0 ? calculatedEmi : r.quotedEmi();

        BigDecimal emiDifference = lenderEmi.subtract(calculatedEmi)
                .divide(calculatedEmi, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalRepayment = lenderEmi.multiply(BigDecimal.valueOf(r.tenureMonths()))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalInterest = totalRepayment.subtract(financed)
                .max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        BigDecimal apr = math.effectiveApr(financed, r.quotedRatePercent(), r.tenureMonths(), fees);
        BigDecimal totalFoir = r.existingEmi().add(lenderEmi)
                .divide(r.monthlyIncome(), 8, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal premium = r.quotedRatePercent().subtract(r.fairRateMax())
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal stressedIncome = r.monthlyIncome().multiply(new BigDecimal("0.80"));
        BigDecimal stressedFoir = r.existingEmi().add(lenderEmi)
                .divide(stressedIncome, 8, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        boolean emiTooHigh = lenderEmi.compareTo(r.safeEmiCeiling()) > 0;
        boolean rateTooHigh = premium.signum() > 0;

        String verdict;
        String reason;
        String ask;

        if (emiTooHigh) {
            verdict = "DON'T AGREE YET";
            reason = "The lender's proposed EMI is above your borrower-safe monthly ceiling.";
            ask = "Ask the lender to reduce the financed amount or restructure the tenure so the EMI stays at or below your safe ceiling.";
        } else if (rateTooHigh && premium.compareTo(new BigDecimal("2.00")) >= 0) {
            verdict = "NEGOTIATE HARD";
            reason = "The quoted rate is at least 2 percentage points above the top of your estimated fair band.";
            ask = "Ask the lender to explain the pricing premium and move the rate toward the top of your fair band.";
        } else if (rateTooHigh) {
            verdict = "NEGOTIATE";
            reason = "The EMI fits your current ceiling, but the quoted rate is above the top of your estimated fair band.";
            ask = "Ask the lender to move the rate toward the top of your fair band and disclose every mandatory fee.";
        } else if (stressedFoir.compareTo(new BigDecimal("50")) > 0) {
            verdict = "BORROW LESS / STRESS RISK";
            reason = "The offer is affordable today but becomes highly stretched in the 20% income-drop stress case.";
            ask = "Ask for a lower financed amount or higher down payment so the stressed repayment burden remains manageable.";
        } else {
            verdict = "REASONABLE TO CONSIDER";
            reason = "The quoted EMI is within your safe ceiling and the rate is within your estimated fair band.";
            ask = "Ask for the official KFS, APR, complete fee schedule and repayment schedule before agreeing.";
        }

        String stressOutcome = stressedFoir.compareTo(new BigDecimal("50")) > 0
                ? "A 20% income drop would push total EMI above 50% of income."
                : stressedFoir.compareTo(new BigDecimal("40")) > 0
                ? "A 20% income drop would make repayment meaningfully tighter."
                : "The 20% income-drop scenario remains within the conservative stress boundary.";

        return new OfferResponse(
                verdict, reason, r.loanAmount(), r.downPayment(), financed,
                calculatedEmi, lenderEmi, emiDifference,
                r.quotedRatePercent(), r.fairRateMax(), premium.max(BigDecimal.ZERO),
                totalRepayment, totalInterest, fees, apr, totalFoir, stressOutcome, ask);
    }
}
