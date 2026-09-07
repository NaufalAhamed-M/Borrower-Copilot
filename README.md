
# Borrower Copilot — V1.0

Borrower Copilot is a borrower-side, stateless self-assessment for India. It helps a borrower answer four questions before meeting a lender:

1. **Should I borrow at all?** → BORROW / BORROW LESS / DON'T BORROW
2. **How much might a lender sanction?** → indicative lender range
3. **How much can I safely carry?** → borrower-safe range and the number to negotiate around
4. **What rate and EMI should I agree to?** → fair-rate band, EMI ceiling, tenure trade-off, stress case and lender-quote comparison

It also creates a **Negotiation Card** that can be shown to a lender.

## Product boundary

- No login.
- No credit-bureau pull.
- No bank-statement verification.
- No borrower database.
- No borrower data is persisted by the application.
- No approval, eligibility, rate or APR is guaranteed.
- All calculations use borrower-supplied values plus explicit V1 fallback assumptions.
- V1 uses deterministic rules rather than ML so every number can be defended in an interview.

## Stack

- Java 17
- Spring Boot 3.5.5
- Spring Web + Validation
- React 19
- Vite 7
- No MySQL in V1 because the assignment requires a stateless/no-storage experience.

## Run locally

### Prerequisites

- JDK 17+
- Maven 3.9+
- Node.js 20+
- npm 10+

Check:

```bash
java -version
mvn -version
node -v
npm -v
```

### Terminal 1 — backend

```bash
cd backend
mvn clean test
mvn spring-boot:run
```

Backend:

```text
http://localhost:8080
```

Health check:

```text
http://localhost:8080/api/health
```

Root:

```text
http://localhost:8080/
```

### Terminal 2 — frontend

```bash
cd frontend
npm install
npm run dev
```

Open the Vite URL, normally:

```text
http://localhost:5173
```

## Adaptive questions

The UI keeps the same two-step visual design, but questions change based on answers.

### Always relevant

- name (optional)
- loan type
- amount wanted
- purpose
- income type
- minimum/ordinary net monthly income
- existing monthly EMI, if known
- existing EMI remaining tenure when EMI > ₹0
- essential household expenses, optional
- age
- credit score, optional
- income stability
- residential rent, optional
- emergency savings, optional
- credit history, optional
- co-applicant, optional
- upcoming large expense, optional

### Only when useful

- maximum monthly income → variable/uncertain income
- variable income share → variable/uncertain income
- repayment bounces → when credit history = Yes
- high-cost/app debt → when credit history = Yes
- high-cost loan count/outstanding/rate → only when high-cost debt = Yes
- ITR/documented annual income → self-employed
- expected productive income → business or two-wheeler borrowing
- collateral value + unencumbered status → LAP or business borrowing
- co-applicant income → only when co-applicant = Yes

The app does **not** assume that rent belongs only to salaried borrowers or that repayment bounces belong only to informal borrowers.

## Optional expense inputs

### Essential household expenses

The field is optional.

- If supplied, the supplied value is used.
- If blank, V1 uses **25% of ordinary monthly income** as an explicit affordability fallback.
- The fallback is shown in the assumptions/reasoning; it is not presented as the borrower's actual expense.

### Residential rent

The field is optional for every income type.

- Enter `0` when the borrower does not pay residential rent.
- Leave blank when the borrower does not know the amount.
- If blank, V1 uses **10% of ordinary monthly income** as an explicit housing-cost fallback.

This distinction prevents an unknown rent value from silently becoming ₹0.

### Emergency savings

- If supplied, the savings months affect resilience.
- If blank, no positive savings benefit is assumed and a small 5% resilience haircut is applied.
- Blank is therefore not interpreted as “0 months”.

## Existing EMI

Existing EMI is a key affordability input.

- `0` means the borrower knows they have no existing EMI.
- A positive EMI requires **remaining tenure in months**.
- Blank means the EMI amount is unknown.
- When EMI is unknown, V1 reserves **15% of ordinary monthly income** as a conservative existing-debt proxy and lowers confidence.
- A known existing EMI is counted in today's affordability until its stated remaining tenure ends.

The app never assumes a future EMI expiry has already happened.

## High-cost/app debt

The app separates:

- number of active high-cost/app loans;
- total outstanding balance; and
- approximate interest rate.

For example, Anita's assignment data means:

```text
3 active app loans
₹35,000 total outstanding
30%+ approximate rate
```

It does **not** mean ₹35,000 is her monthly EMI.

Existing EMI remains a separate input. If the assignment does not give Anita's EMI, the app allows it to remain unknown and uses the documented conservative reserve rather than inventing a value.

## Four outputs

### O1 — Borrow / Borrow Less / Don't Borrow

The decision considers safe new-EMI capacity, requested EMI, existing repayment pressure and severe repayment-risk combinations.

`DON'T BORROW` is deliberately reachable.

### O2 — Maximum amount

Two separate ranges are shown:

- **Likely lender range** — an indicative lender-capacity illustration.
- **Borrower-safe amount** — derived from borrower-side repayment capacity.

The lender range is never used as the safety cap.

The Negotiation Card uses the conservative end of the borrower-safe range when borrowing is appropriate.

### O3 — Fair interest rate

The app starts from product-level market-informed bands and applies transparent profile adjustments.

The result is always a **range**, not a single rate.

The lender-quote section calculates:

```text
rate premium = lender quoted rate − fair-rate upper bound
```

So:

```text
16.50% − 12.50% = 4.00 percentage points over fair
```

“4 points over fair” means **4 percentage points**, not 4% of the loan amount.

### O4 — EMI ceiling

The app calculates a maximum comfortable new EMI after applying:

- income;
- income type/stability;
- essential expenses;
- residential rent;
- existing EMI or the explicit unknown-EMI reserve;
- emergency-savings resilience;
- variable-income volatility;
- high-cost debt;
- recent repayment problems; and
- upcoming large expenses.

A tenure table shows EMI and total interest at several product-appropriate tenures.

A 20% income-drop stress case is also shown.

## Lender quote comparison

The borrower can enter a lender quote after seeing the self-assessment:

- lender principal / purchase price;
- down payment for home/two-wheeler;
- quoted interest rate;
- tenure;
- quoted EMI, optional;
- mandatory upfront fees.

The app shows:

- financed amount;
- calculated EMI;
- lender EMI if supplied;
- EMI variance;
- total interest;
- rate premium over the fair upper bound;
- illustrative APR;
- total EMI burden;
- 20% income-drop stress outcome.

The lender's official **KFS/APR and contractual repayment schedule** remain authoritative.

## Regulatory grounding

RBI's April 15, 2024 KFS framework requires regulated entities to provide borrowers a standardized Key Facts Statement containing key loan information, including the all-in cost/APR framework and repayment information.


## Market-rate anchors

The V1 personal-loan starting band is informed by currently published bank information but is deliberately treated as a product judgement rather than a guaranteed offer.

Examples used as anchors while writing V1:

- ICICI Bank personal loans: published range around 9.99%–16.50% p.a. on its current rate page.
- HDFC Bank personal-loan page: published rack rate for salaried customers around 9.99%–24.00%, with recent quarterly IRR/APR statistics also disclosed.
- Other product bands in V1 are documented as judgement-based anchors because rates vary by lender, borrower and product.

Current lender pages change. The application therefore tells the borrower to compare the actual KFS/APR rather than relying on these anchors.

## API

### `POST /api/assessment`

Runs the borrower assessment.

### `POST /api/offer-check`

Checks a lender quote against the borrower's fair-rate band and EMI ceiling.

### `GET /api/questions`

Returns the adaptive question catalogue for the selected loan and income type.

### `GET /api/health`

Returns:

```json
{"status":"UP"}
```

## Architecture

```text
React UI
   ↓
REST API
   ↓
Spring Boot Controller
   ↓
AssessmentService
   ├── AffordabilityService
   ├── LoanAmountService
   ├── RateService
   ├── DecisionService
   ├── StressService
   ├── ConfidenceService
   └── ProductRoutingService
   ↓
Four outputs + Negotiation Card
```

Business rules stay in the backend. The UI only collects input and renders results.

## Files to review

- `RULES.md` — every important rule, threshold, band and fallback.
- `RUNTHROUGHS.md` — Priya, Ravi and Anita acceptance runs.
- `backend/src/test/.../AssessmentServiceTest.java` — regression tests for the core rules.

