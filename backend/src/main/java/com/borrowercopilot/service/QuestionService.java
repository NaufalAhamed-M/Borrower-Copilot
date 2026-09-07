package com.borrowercopilot.service;

import com.borrowercopilot.model.Enums.IncomeStability;
import com.borrowercopilot.model.Enums.IncomeType;
import com.borrowercopilot.model.Enums.LoanType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionService {
    public List<Question> questions(LoanType loanType, IncomeType incomeType) {
        List<Question> q = new ArrayList<>();
        q.add(new Question("name", "What is your name?", false, "all"));
        q.add(new Question("loanType", "What type of loan are you considering?", true, "all"));
        q.add(new Question("amountWanted", "How much do you want to borrow?", true, "all"));
        q.add(new Question("purpose", "What will the loan be used for?", true, "all"));
        q.add(new Question("incomeType", "How do you earn most of your income?", true, "all"));
        q.add(new Question("monthlyIncomeMin", "What is your usual/minimum net monthly income?", true, "all"));
        q.add(new Question("monthlyIncomeMax", "What is your highest typical monthly income?", false, "variable-income"));
        q.add(new Question("existingEmi", "How much do you already pay in EMIs each month? Leave blank if unknown.", false, "all"));
        q.add(new Question("existingEmiMonthsRemaining", "How many months remain on your existing EMI?", false, "if-existing-emi"));
        q.add(new Question("essentialExpenses", "How much do essential household expenses cost each month, excluding rent?", false, "all"));
        q.add(new Question("age", "What is your age?", true, "all"));
        q.add(new Question("creditScore", "What is your credit score? You can choose I don't know.", false, "all"));
        q.add(new Question("incomeStability", "How stable is your income?", true, "all"));
        q.add(new Question("rent", "Do you pay monthly residential rent? If yes, how much?", false, "all"));
        q.add(new Question("emergencySavingsMonths", "How many months of essential expenses can your savings cover?", false, "all"));
        q.add(new Question("variableIncomeShare", "Approximately what share of income is variable?", false, "if-variable"));
        q.add(new Question("hasCreditHistory", "Do you currently have or have you previously had formal/app-based credit?", false, "credit-history"));
        q.add(new Question("recentBounceCount", "How many loan/credit payment bounces or missed payments happened in the last 6 months?", false, "if-credit-history"));
        q.add(new Question("hasHighCostDebt", "Do you currently have any high-cost/app loans?", false, "if-credit-history"));
        q.add(new Question("highCostLoanCount", "How many active high-cost/app loans do you have?", false, "if-high-cost-debt"));
        q.add(new Question("highCostDebtOutstanding", "What is the total outstanding balance on those high-cost/app loans?", false, "if-high-cost-debt"));
        q.add(new Question("highCostDebtRatePercent", "What is the approximate interest rate on those high-cost/app loans?", false, "if-high-cost-debt"));
        q.add(new Question("upcomingLargeExpense", "Any major expense expected in the next 12 months?", false, "all"));
        q.add(new Question("coApplicant", "Will someone co-borrow with you?", false, "all"));
        q.add(new Question("coApplicantMonthlyIncome", "What is the co-applicant's net monthly income?", false, "if-co-applicant"));

        if (incomeType == IncomeType.SELF_EMPLOYED) {
            q.add(new Question("documentedAnnualIncome", "What annual income is shown in your ITR/records?", false, "self-employed"));
        }
        if (loanType == LoanType.BUSINESS || loanType == LoanType.TWO_WHEELER) {
            q.add(new Question("expectedMonthlyIncomeIncrease", "How much additional monthly income do you realistically expect the loan to create?", false, "productive-loan"));
        }
        if (loanType == LoanType.LAP || loanType == LoanType.BUSINESS) {
            q.add(new Question("collateralValue", "What is the approximate value of available collateral?", false, "secured/business"));
            q.add(new Question("collateralUnencumbered", "Is the collateral unencumbered?", false, "secured/business"));
        }
        return q;
    }

    public record Question(String key, String text, boolean required, String path) {}
}
