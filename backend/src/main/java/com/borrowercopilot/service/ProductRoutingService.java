package com.borrowercopilot.service;

import com.borrowercopilot.dto.AssessmentRequest;
import com.borrowercopilot.dto.AssessmentResponse.ProductRecommendation;
import com.borrowercopilot.model.Enums.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ProductRoutingService {
    public ProductRecommendation route(AssessmentRequest r) {
        if ((r.loanType() == LoanType.BUSINESS || r.loanType() == LoanType.LAP) && r.collateralValue() != null && Boolean.TRUE.equals(r.collateralUnencumbered())) {
            return new ProductRecommendation("SECURED BUSINESS FINANCING / LAP (CONSIDER)",
                    "You reported unencumbered collateral; compare a secured route against unsecured borrowing on APR, fees, tenure and total repayment.");
        }
        return new ProductRecommendation(r.loanType().name().replace('_', ' '),
                "This is the product type selected; compare the lender's KFS, APR, fees and repayment schedule before agreeing.");
    }
}
