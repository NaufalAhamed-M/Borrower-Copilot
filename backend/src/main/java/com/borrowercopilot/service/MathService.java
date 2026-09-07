package com.borrowercopilot.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class MathService {

    public BigDecimal emi(BigDecimal principal, BigDecimal annualRatePercent, int months) {
        if (principal == null || principal.signum() <= 0 || months <= 0)
            return BigDecimal.ZERO;
        if (annualRatePercent == null || annualRatePercent.signum() == 0) {
            return principal.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        }
        double p = principal.doubleValue();
        double r = annualRatePercent.doubleValue() / 1200.0;
        double n = months;
        double e = p * r * Math.pow(1 + r, n) / (Math.pow(1 + r, n) - 1);
        return BigDecimal.valueOf(e).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal totalInterest(BigDecimal principal, BigDecimal rate, int months) {
        return emi(principal, rate, months).multiply(BigDecimal.valueOf(months))
                .subtract(principal).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal roundHundred(BigDecimal value) {
        if (value == null)
            return BigDecimal.ZERO;
        return value.divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(0);
    }

    public BigDecimal min(BigDecimal a, BigDecimal b) { return a.compareTo(b) <= 0 ? a : b; }
    public BigDecimal max(BigDecimal a, BigDecimal b) { return a.compareTo(b) >= 0 ? a : b; }
    public BigDecimal avg(BigDecimal a, BigDecimal b) { return a.add(b).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP); }

    public BigDecimal effectiveApr(BigDecimal principal, BigDecimal annualRatePercent, int months, BigDecimal upfrontFee) {
        BigDecimal fee = upfrontFee == null ? BigDecimal.ZERO : upfrontFee.max(BigDecimal.ZERO);
        BigDecimal net = principal.subtract(fee);
        if (net.signum() <= 0)
            return annualRatePercent;

        double payment = emi(principal, annualRatePercent, months).doubleValue();
        double target = net.doubleValue();
        double lo = 0.0;
        double hi = 1.0;

        for (int i = 0; i < 100; i++) {
            double mid = (lo + hi) / 2.0;
            double monthly = Math.pow(1.0 + mid, 1.0 / 12.0) - 1.0;
            double pv = 0.0;
            for (int m = 1; m <= months; m++) {
                pv += payment / Math.pow(1.0 + monthly, m);
            }
            if (pv > target)
                lo = mid;
            else hi = mid;
        }

        return BigDecimal.valueOf(((lo + hi) / 2.0) * 100.0)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
