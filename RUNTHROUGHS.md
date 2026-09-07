# Borrower Copilot — Three Run-throughs

These are representative acceptance runs. They are not hardcoded into the application. Values below are produced by the deterministic V1 rules.

## 1. Priya

### Inputs asked / answered

- Name: Priya
- Loan type: Personal
- Amount wanted: ₹8,00,000
- Purpose: Wedding
- Income type: Salaried
- Net monthly income: ₹1,10,000
- Existing EMI: ₹14,000
- Existing EMI remaining: 24 months
- Essential expenses excluding rent: ₹17,000
- Age: 29
- Credit score: 780
- Income stability: Stable
- Residential rent: ₹28,000
- Emergency savings: 4 months
- Variable income share: Not asked because income was marked stable
- Credit history: Yes
- Payment bounces in last 6 months: 0
- High-cost/app loans: No
- Upcoming large expense: Unknown
- Co-applicant: No

### Why this path is adaptive

- Rent is asked even though Priya is salaried because housing cost affects affordability.
- Bounce history is asked because she has credit history, not because she is salaried.
- ITR and collateral questions are skipped because they are not relevant to a salaried personal-loan path.

### Four outputs

- **O1 — BORROW**
- **O2 — Likely lender range:** ₹6,00,000–₹8,00,000
- **O2 — Borrower-safe range:** ₹8,88,300–₹10,45,100
- **O2 — Negotiation amount:** ₹8,00,000 because she requested ₹8,00,000 and that is below the conservative safe amount.
- **O3 — Fair rate:** 8.75%–15.25%
- **O4 — Maximum comfortable new EMI:** ₹25,000/month

### Tenure trade-off for ₹8,00,000

| Tenure | EMI at conservative fair-rate upper bound 15.25% | Approx. total interest |
|---:|---:|---:|
| 3 years | ₹27,830 | ₹2,01,891 |
| 4 years | ₹22,366 | ₹2,73,573 |
| 5 years | ₹19,137 | ₹3,48,225 |

The 3-year EMI is above the ₹25,000 ceiling, while 4 and 5 years are below it. Longer tenure lowers monthly EMI but increases total interest.

### Stress

A 20% income drop gives stressed income of ₹88,000. Using the shortest displayed tenure, total EMI is about ₹41,830 including the existing ₹14,000 EMI, producing stressed FOIR of about **47.53%**.

Result: **Caution** — the income shock makes repayment meaningfully tighter.

### Negotiation Card

```text
BORROW — within your safe range

Requested: ₹8,00,000
Safe amount: ₹8,00,000 practical negotiation amount
Fair rate: 8.75%–15.25%
Maximum new EMI: ₹25,000

Ask the lender to keep the rate within the fair band where possible,
keep the EMI at or below ₹25,000 and disclose the complete KFS/APR
and every mandatory fee before you agree.
```

---

## 2. Ravi

### Inputs asked / answered

- Name: Ravi
- Loan type: Business
- Amount wanted: ₹15,00,000
- Purpose: Second stock line + delivery vehicle
- Income type: Self-employed
- Net monthly income: ₹40,000–₹80,000
- Existing EMI: ₹0
- Essential expenses excluding rent: ₹30,000
- Age: 42
- Credit score: Unknown
- Income stability: Variable
- Residential rent: ₹0
- Emergency savings: 3 months
- Variable income share: Unknown
- Credit history: No formal/app credit
- High-cost/app loans: No
- Upcoming large expense: Unknown
- Co-applicant: Yes; co-applicant income ₹18,000
- ITR/documented annual income: ₹4,20,000/year
- Expected monthly income increase from the business loan: ₹10,000
- Collateral value: ₹45,00,000
- Collateral unencumbered: Yes

### Why this path is adaptive

- ITR/documented income is asked because Ravi is self-employed.
- Collateral is asked because the selected product is business borrowing.
- A secured business/LAP route is highlighted because he reports unencumbered collateral.
- Rent is still asked because owning the shop does not establish that he owns his residence.
- Credit-score-dependent repayment questions are not forced when he reports no formal/app credit.

### Four outputs

- **O1 — BORROW LESS**
- **O2 — Likely lender range:** ₹11,25,000–₹15,00,000
- **O2 — Borrower-safe range:** ₹8,16,400–₹11,66,300
- **O2 — Negotiation amount:** ₹8,16,400
- **O3 — Fair rate:** 10.50%–20.00%
- **O4 — Maximum comfortable new EMI:** ₹25,900/month

The lender range is higher than the conservative safe amount. That difference is intentional: **lender capacity is not borrower-safe capacity**.

### Tenure trade-off for ₹8,16,400

| Tenure | EMI at conservative fair-rate upper bound 20% | Approx. total interest |
|---:|---:|---:|
| 3 years | ₹30,340 | ₹2,75,853 |
| 5 years | ₹21,630 | ₹4,81,376 |
| 7 years | ₹18,129 | ₹7,06,448 |

The 3-year EMI is above the ₹25,900 ceiling. Five and seven years fit the monthly ceiling, but the longer tenure materially increases total interest.

### Stress

A 20% income drop on the ₹60,000 ordinary-income midpoint gives stressed income of ₹48,000. The shortest displayed tenure produces stressed FOIR of about **63.21%**.

Result: **Stretched** under the V1 stress test.

### Product route

```text
SECURED BUSINESS FINANCING / LAP (CONSIDER)

Reason: Ravi reports unencumbered collateral of approximately ₹45 lakh.
The app does not guarantee a 60% LTV or approval. He should compare
secured and unsecured offers using the official KFS/APR, fees and
repayment schedule.
```

### Negotiation Card

```text
BORROW LESS — lender capacity may exceed safe capacity

Requested: ₹15,00,000
Safe amount: ₹8,16,400 conservative negotiation number
Fair rate: 10.50%–20.00%
Maximum new EMI: ₹25,900

Ask the lender to reduce the amount so the EMI stays at or below
₹25,900 and compare a secured business/LAP route against the
unsecured offer using the official KFS/APR and complete fee schedule.
```

---

## 3. Anita

### Inputs asked / answered

- Name: Anita
- Loan type: Two-wheeler
- Amount wanted: ₹1,50,000
- Purpose: Electric scooter to increase delivery runs
- Income type: Informal/gig
- Net monthly income: ₹26,000–₹30,000
- Existing EMI: **Unknown**
- Existing EMI remaining: Not asked because the EMI amount is unknown
- Essential expenses excluding rent: ₹12,000
- Age: 35
- Credit score: Unknown
- Income stability: Highly variable
- Residential rent: Unknown
- Emergency savings: Unknown
- Variable income share: Unknown
- Credit history: Yes
- Payment bounces in last 6 months: 1
- High-cost/app loans: Yes
- Active high-cost/app loan count: 3
- Total high-cost/app debt outstanding: ₹35,000
- Approximate high-cost debt rate: 30%+
- Upcoming large expense: Unknown
- Co-applicant: No
- Expected monthly income increase from the scooter: ₹5,000

### Important interpretation

The assignment's statement:

```text
3 app loans, ₹35,000 outstanding at 30%+
```

means:

```text
3 active loans
₹35,000 total outstanding across them
30%+ approximate borrowing rate
```

It does **not** mean ₹35,000 is Anita's monthly EMI.

Because the assignment does not provide her actual existing EMI, the app leaves it unknown instead of inventing a value. V1 reserves 15% of ordinary income as a conservative existing-debt proxy and lowers confidence.

### Why this path is adaptive

- Bounce history is asked because Anita has credit history.
- High-cost debt questions are shown because she reports high-cost/app loans.
- ITR and collateral questions are skipped because this is a two-wheeler/gig path.
- The decision is driven by repayment signals, not simply by the word “informal”.

### Four outputs

- **O1 — DON'T BORROW**
- **O2 — Likely lender range:** ₹1,12,500–₹1,50,000
- **O2 — Borrower-safe range:** ₹0–₹0
- **O2 — Negotiation amount:** ₹0
- **O3 — Fair rate:** 15.00%–30.00%
- **O4 — Maximum comfortable new EMI:** approximately ₹772/month

The ₹0 safe amount is intentional. The calculated new-EMI capacity is below the V1 minimum meaningful new-EMI threshold of ₹2,000.

### Tenure illustration for the requested ₹1,50,000

| Tenure | EMI at conservative fair-rate upper bound 30% | Approx. total interest |
|---:|---:|---:|
| 2 years | ₹8,387 | ₹51,286 |
| 3 years | ₹6,368 | ₹79,238 |
| 4 years | ₹5,401 | ₹1,09,243 |

Even the longest displayed tenure is far above the approximately ₹772 new-EMI ceiling.

### Stress

Ordinary income midpoint is ₹28,000. A 20% income drop gives stressed income of ₹22,400. V1's unknown-existing-EMI reserve is ₹4,200. Using the 2-year requested-loan EMI, stressed FOIR is about **56.19%**.

Result: **Stretched**.

### Negotiation Card

```text
DON'T BORROW — pause and reduce repayment pressure

Requested: ₹1,50,000
Safe amount: ₹0
Fair rate: 15.00%–30.00%
Maximum new EMI: about ₹772

Do not add a new obligation yet. Reduce existing repayment pressure
and reassess when the new EMI can stay within the borrower-safe ceiling.
```

---

## Acceptance checks

- Rent is available to salaried, self-employed and informal/gig borrowers.
- Existing EMI tenure is requested only when a positive EMI is known.
- Unknown existing EMI is not converted to ₹0; the documented reserve is used.
- High-cost debt is not confused with monthly EMI.
- High-cost debt count, outstanding balance and rate are separate inputs.
- Bounces are asked based on credit history, not occupation.
- Self-employed borrowers can enter collateral for business/LAP routes.
- Unknown credit score is not converted into 300 or another bad score.
- Unknown rent and savings are not silently treated as favourable zero values.
- The lender range and safe range are calculated separately.
- Fair rate is always a range.
- “4 points over fair” means percentage points above the fair upper bound.
- Mandatory lender fees affect the illustrative APR calculation.
- Home/two-wheeler down payment reduces financed principal and EMI in the lender-quote checker.
