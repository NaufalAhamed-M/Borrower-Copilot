package com.borrowercopilot;

import com.borrowercopilot.dto.AssessmentRequest;
import com.borrowercopilot.dto.AssessmentResponse;
import com.borrowercopilot.model.Enums.*;
import com.borrowercopilot.service.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AssessmentServiceTest {
    private AssessmentService service() {
        MathService math = new MathService();
        AffordabilityService affordability = new AffordabilityService(math);
        RateService rate = new RateService();
        LoanAmountService amount = new LoanAmountService(math, affordability, rate);
        DecisionService decision = new DecisionService(affordability, math, rate, amount);
        StressService stress = new StressService(affordability);
        ConfidenceService confidence = new ConfidenceService();
        ProductRoutingService routing = new ProductRoutingService();
        return new AssessmentService(affordability, amount, rate, decision, stress, confidence, routing, math);
    }

    @Test
    void priyaCanReachBorrow() {
        AssessmentResponse out = service().assess(new Input()
                .name("Priya").loan(LoanType.PERSONAL).amount("800000").purpose("wedding")
                .incomeType(IncomeType.SALARIED).income("110000", null).existing("14000", 24)
                .expenses("17000").age(29).score(780).stability(IncomeStability.STABLE)
                .rent("28000").savings("4").credit(true).highCost(false).bounces(0).build());
        assertEquals(Decision.BORROW, out.decision());
        assertEquals(24, out.existingEmiMonthsRemaining());
        assertTrue(out.safeAmount().max().signum() > 0);
    }

    @Test
    void raviRoutesToSecuredBusinessRoute() {
        AssessmentResponse out = service().assess(new Input()
                .name("Ravi").loan(LoanType.BUSINESS).amount("1500000").purpose("stock and delivery vehicle")
                .incomeType(IncomeType.SELF_EMPLOYED).income("40000", "80000").existing("0", null)
                .expenses("30000").age(42).score(null).stability(IncomeStability.VARIABLE)
                .rent("0").savings("3").credit(false).highCost(false).bounces(null)
                .documented("420000").expected("10000").collateral("4500000", true)
                .coApplicant("18000").build());
        assertTrue(out.productRecommendation().product().contains("SECURED"));
        assertTrue(out.lenderSanction().max().compareTo(bd("1500000")) <= 0);
    }

    @Test
    void anitaCanReachDontBorrowWithoutInventingExistingEmi() {
        AssessmentResponse out = service().assess(new Input()
                .name("Anita").loan(LoanType.TWO_WHEELER).amount("150000").purpose("electric scooter")
                .incomeType(IncomeType.INFORMAL_GIG).income("26000", "30000").existing(null, null)
                .expenses("12000").age(35).score(null).stability(IncomeStability.HIGHLY_VARIABLE)
                .rent(null).savings(null).credit(true).highCost(true).highCostDetails(3, "35000", "30").bounces(1)
                .expected("5000").build());
        assertEquals(Decision.DONT_BORROW, out.decision());
        assertEquals(BigDecimal.ZERO, out.recommendedAmount());
        assertEquals(BigDecimal.ZERO, out.safeAmount().max());
    }

    @Test
    void rentReducesAffordabilityForSelfEmployed() {
        AssessmentRequest noRent = new Input().incomeType(IncomeType.SELF_EMPLOYED).rent(null).build();
        AssessmentRequest withRent = new Input().incomeType(IncomeType.SELF_EMPLOYED).rent("20000").build();
        assertTrue(service().assess(withRent).maximumSafeEmi().compareTo(service().assess(noRent).maximumSafeEmi()) < 0);
    }

    @Test
    void rentReducesAffordabilityForInformalBorrower() {
        AssessmentRequest noRent = new Input().incomeType(IncomeType.INFORMAL_GIG).rent(null).build();
        AssessmentRequest withRent = new Input().incomeType(IncomeType.INFORMAL_GIG).rent("10000").build();
        assertTrue(service().assess(withRent).maximumSafeEmi().compareTo(service().assess(noRent).maximumSafeEmi()) < 0);
    }

    @Test
    void bounceChangesRateForSalariedBorrowerToo() {
        AssessmentRequest clean = new Input().credit(true).highCost(false).bounces(0).build();
        AssessmentRequest bounced = new Input().credit(true).highCost(false).bounces(1).build();
        assertTrue(service().assess(bounced).fairRate().maxPercent().compareTo(service().assess(clean).fairRate().maxPercent()) > 0);
    }

    @Test
    void unknownCreditScoreWidensFairRate() {
        AssessmentRequest known = new Input().score(720).build();
        AssessmentRequest unknown = new Input().score(null).build();
        assertTrue(service().assess(unknown).fairRate().maxPercent().compareTo(service().assess(known).fairRate().maxPercent()) > 0);
    }

    @Test
    void optionalExpensesAndRentUseFallbacks() {
        AssessmentResponse out = service().assess(new Input().expenses(null).rent(null).savings(null).build());
        assertTrue(out.essentialExpensesEstimated());
        assertTrue(out.rentEstimated());
        assertTrue(out.essentialExpensesUsed().signum() > 0);
        assertTrue(out.rentUsed().signum() > 0);
    }

    @Test
    void existingEmiRequiresRemainingTenure() {
        AssessmentRequest r = new Input().existing("10000", null).build();
        assertThrows(IllegalArgumentException.class, () -> service().assess(r));
    }

    @Test
    void zeroEmiCannotHavePositiveRemainingTenure() {
        AssessmentRequest r = new Input().existing("0", 12).build();
        assertThrows(IllegalArgumentException.class, () -> service().assess(r));
    }

    @Test
    void highCostLoanDetailsAreNotAcceptedWhenUserSaysNo() {
        AssessmentRequest r = new Input().credit(true).highCost(false).highCostDetails(2, "10000", "30").build();
        assertThrows(IllegalArgumentException.class, () -> service().assess(r));
    }

    @Test
    void collateralIsAllowedForSelfEmployedBusinessBorrower() {
        AssessmentResponse out = service().assess(new Input().loan(LoanType.BUSINESS)
                .incomeType(IncomeType.SELF_EMPLOYED).collateral("4500000", true).documented("420000").build());
        assertTrue(out.productRecommendation().product().contains("SECURED"));
    }

    private static BigDecimal bd(String value) { return value == null ? null : new BigDecimal(value); }

    private static class Input {
        String name = "Test";
        LoanType loanType = LoanType.PERSONAL;
        String amount = "500000";
        String purpose = "education";
        IncomeType incomeType = IncomeType.SALARIED;
        String minIncome = "100000";
        String maxIncome = null;
        String existingEmi = "0";
        Integer existingTenure = null;
        String expenses = "20000";
        int age = 30;
        Integer score = 720;
        IncomeStability stability = IncomeStability.STABLE;
        String rent = "10000";
        String savings = "6";
        String variableShare = null;
        Boolean credit = true;
        Boolean highCost = false;
        Integer highCostCount = 0;
        String highCostOutstanding = "0";
        String highCostRate = null;
        Integer bounces = 0;
        String collateral = null;
        Boolean unencumbered = null;
        String documented = null;
        String expected = null;
        String upcoming = null;
        Boolean coApplicant = false;
        String coApplicantIncome = null;

        Input name(String v){name=v;return this;} Input loan(LoanType v){loanType=v;return this;}
        Input amount(String v){amount=v;return this;} Input purpose(String v){purpose=v;return this;}
        Input incomeType(IncomeType v){incomeType=v;return this;} Input income(String a,String b){minIncome=a;maxIncome=b;return this;}
        Input existing(String a,Integer b){existingEmi=a;existingTenure=b;return this;} Input expenses(String v){expenses=v;return this;}
        Input age(int v){age=v;return this;} Input score(Integer v){score=v;return this;} Input stability(IncomeStability v){stability=v;return this;}
        Input rent(String v){rent=v;return this;} Input savings(String v){savings=v;return this;} Input credit(Boolean v){credit=v;return this;}
        Input highCost(Boolean v){highCost=v;return this;} Input highCostDetails(Integer c,String o,String r){highCostCount=c;highCostOutstanding=o;highCostRate=r;return this;}
        Input bounces(Integer v){bounces=v;return this;} Input collateral(String v,Boolean u){collateral=v;unencumbered=u;return this;}
        Input documented(String v){documented=v;return this;} Input expected(String v){expected=v;return this;} Input coApplicant(String v){coApplicant=true;coApplicantIncome=v;return this;}

        AssessmentRequest build(){
            return new AssessmentRequest(name,loanType,bd(amount),purpose,incomeType,bd(minIncome),bd(maxIncome),bd(existingEmi),existingTenure,
                    bd(expenses),age,score,stability,bd(rent),bd(savings),bd(variableShare),credit,highCost,highCostCount,bd(highCostOutstanding),bd(highCostRate),
                    bounces,bd(collateral),unencumbered,bd(documented),bd(expected),bd(upcoming),coApplicant,bd(coApplicantIncome));
        }
    }
}
