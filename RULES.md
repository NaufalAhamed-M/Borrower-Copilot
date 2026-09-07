# Borrower Copilot — RULES.md

**Version:** 1.0 — borrower-first, deterministic V1

## 1. Product boundary

| What | Value | Why | Source |
|---|---|---|---|
| Product type | Stateless borrower self-assessment | The assignment asks for borrower-side decision support, not lender underwriting | Assignment |
| Login | None | No identity layer is needed | Assignment |
| Bureau pull | None | All inputs come from the borrower | Assignment |
| Persistence | None | The assignment explicitly says no personal data stored | Assignment |
| Approval guarantee | Never provided | Borrower estimates cannot reproduce lender underwriting | My judgement |
| Currency | INR | Product is India-specific | Assignment |
| Decision engine | Deterministic rules, no ML | Every number must be traceable and defendable | My judgement |

## 2. Core outputs

| What | Value / rule | Why | Source |
|---|---|---|---|
| O1 verdict | BORROW / BORROW LESS / DON'T BORROW | All three outcomes are required; DON'T BORROW must be reachable | Assignment |
| O2 lender range | Indicative lender-capacity range | Separates likely lender capacity from borrower safety | Assignment / my judgement |
| O2 safe range | Principal supported by safe new-EMI capacity at the upper fair-rate bound | Prevents optimistic pricing from inflating safety | My judgement |
| O2 negotiation number | Conservative lower end of safe range, capped by requested amount unless DON'T BORROW | Gives the borrower one actionable number | My judgement |
| O3 fair rate | Always a band | Self-reported information cannot justify a single precise price | Assignment |
| O4 max new EMI | Safe total debt-payment capacity minus existing EMI burden | Directly answers what monthly outflow the borrower can negotiate around | Assignment / my judgement |
| Stress case | 20% income drop | Simple resilience test | My judgement |
| Negotiation Card | Requested amount, safe amount, fair rate, max EMI, reasons and lender-facing ask | One-screen branch negotiation aid | Assignment |

## 3. Required and optional inputs

The app asks the core questions needed to produce all outputs. A borrower may leave optional values unknown; unknown values widen uncertainty or trigger explicit conservative fallbacks.

| Input | Required? | When | Output affected | Source |
|---|---|---|---|---|
| Name | No | Every borrower | Display only | My judgement |
| Loan type | Yes | Every borrower | Lender range, fair rate, tenure, route | Assignment |
| Amount wanted | Yes | Every borrower | Verdict, lender range, safe range | Assignment |
| Purpose | Yes | Every borrower | Decision context / card | Assignment |
| Income type | Yes | Every borrower | Affordability, lender range, rate | Assignment |
| Minimum/ordinary monthly income | Yes | Every borrower | All affordability outputs | Assignment |
| Maximum monthly income | No | Variable/uncertain income | Ordinary income used for calculations | My judgement |
| Existing EMI | No if unknown | Every borrower | Affordability, verdict, stress | Assignment / unknown-is-not-zero principle |
| Existing EMI remaining tenure | Conditional | Only when existing EMI > ₹0 | Current affordability context and lender/rate adjustment | My judgement |
| Essential expenses excluding rent | No | Every borrower | Safe EMI, safe amount | User requirement / my judgement |
| Age | Yes | Every borrower | Indicative lender range | Assignment / my judgement |
| Credit score | No | Every borrower | Fair rate, lender range, confidence | Assignment |
| Income stability | Yes | Every borrower | Affordability, fair rate, lender range | Assignment / my judgement |
| Residential rent | No | Every borrower | Safe EMI, safe amount | User requirement / my judgement |
| Emergency savings months | No | Every borrower | Safe EMI, confidence | User requirement / my judgement |
| Variable income share | No | Variable/uncertain income | Safe EMI, fair rate | Assignment / my judgement |
| Credit history | No | Every borrower | Enables repayment-history questions | My judgement |
| Recent bounces/missed payments | No | Credit history = Yes | Safe EMI, fair rate, verdict | Assignment / my judgement |
| High-cost/app debt flag | No | Credit history = Yes | Enables debt-detail questions | My judgement |
| High-cost loan count | No | High-cost debt = Yes | Safe EMI and fair rate | Assignment / my judgement |
| High-cost debt outstanding | No | High-cost debt = Yes | Safe EMI and fair rate | Assignment / my judgement |
| High-cost debt rate | No | High-cost debt = Yes | Fair rate | Assignment / my judgement |
| ITR/documented annual income | No | Self-employed | Indicative lender range, confidence | Assignment |
| Expected productive income | No | Business/two-wheeler | Qualifying affordability income | Assignment |
| Collateral value | No | LAP/business | Indicative lender range, route | Assignment |
| Collateral unencumbered | No | LAP/business | Indicative lender range, route | Assignment |
| Upcoming large expense | No | Every borrower | Safe EMI | Assignment |
| Co-applicant | No | Every borrower | Qualifying income, confidence | Assignment |
| Co-applicant income | Conditional | Co-applicant = Yes | Qualifying income | My judgement |

## 4. Adaptive-path rules

| Question | When shown | What changes | Why | Source |
|---|---|---|---|---|
| Maximum income | Variable/highly variable/unknown stability | Ordinary income average | Captures a realistic income range | My judgement |
| Variable income share | Variable/highly variable/unknown stability | Safe EMI and fair rate | Volatility matters independently of employment label | My judgement |
| Existing EMI tenure | Existing EMI > ₹0 | Existing-debt treatment | A current EMI does not disappear until it ends | My judgement |
| Rent | All borrower types | Safe EMI / safe amount | Housing cost is independent of occupation | User requirement |
| Credit history | All borrower types | Enables repayment-history branch | Credit history is a borrower attribute | My judgement |
| Bounces | Credit history = Yes | Safe EMI / fair rate / verdict | Missed payments are material repayment signals | My judgement |
| High-cost debt flag | Credit history = Yes | Enables debt branch | Avoids asking debt-detail questions when irrelevant | My judgement |
| High-cost loan details | High-cost debt = Yes | Safe EMI / fair rate | Number, outstanding and rate each add information | My judgement |
| ITR | Self-employed | Lender range | Documentation can constrain indicative capacity | Assignment / my judgement |
| Productive income | Business/two-wheeler | Affordability | Loan may generate income, but only a discounted amount is counted | Assignment / my judgement |
| Collateral | LAP/business | Lender range / product route | Secured borrowing differs from unsecured borrowing | Assignment |
| Co-applicant income | Co-applicant = Yes | Affordability | Additional income can increase capacity, but is discounted | My judgement |

### Adaptive invariants

- Rent is never salaried-only.
- Repayment bounces are never informal-only.
- Collateral is available to self-employed borrowers when the selected product is LAP/business.
- Unknown is never silently converted to zero or to a bad credit score.
- If credit history = No, repayment-bounce and high-cost debt fields are ignored/cleared.
- If high-cost debt = No, its detail fields are ignored/cleared.
- Existing EMI > ₹0 requires remaining tenure.
- Existing EMI = ₹0 requires blank remaining tenure.
- Existing EMI blank is allowed and uses an explicit reserve rather than zero.

## 5. Income calculations

| Rule | Value | Why | Source |
|---|---:|---|---|
| Ordinary monthly income | Minimum income if maximum is absent | Conservative when income is fixed/unknown | My judgement |
| Ordinary monthly income with range | `(min + max) / 2` | Neutral midpoint for a self-reported range | My judgement |
| Co-applicant income contribution | 50% of supplied income | Conservative discount for self-reported additional income | My judgement |
| Productive expected-income contribution | 50% of supplied expected increase | Avoids treating forecast income as guaranteed | My judgement |
| Self-employed documented monthly income | `ITR annual / 12` | Comparable monthly documentation | My judgement |
| Self-employed lender income | Lower of ordinary income and documented monthly income when ITR is supplied | Avoids treating unverified cash income as fully documented | My judgement |

## 6. Optional expense fallbacks

| Input | Supplied value | Blank/unknown fallback | Why | Source |
|---|---|---:|---|---|
| Essential household expenses excluding rent | Use exactly supplied amount | 25% of ordinary monthly income | Unknown spending must not become ₹0 | My judgement |
| Residential rent | Use exactly supplied amount; ₹0 means no rent | 10% of ordinary monthly income | Unknown housing cost must not become ₹0 | My judgement |
| Emergency savings | Use supplied months | No positive savings benefit + 5% resilience haircut | Unknown is not the same as zero | My judgement |

The UI labels essential expenses as **excluding rent** so the two cash-flow items are not double-counted.

## 7. FOIR-style affordability

These percentages are **product judgements**. They are not claims that RBI mandates these exact thresholds.

| Rule | Value | Why | Source |
|---|---:|---|---|
| Salaried base total-debt-payment ratio | 40% | Conservative mainstream borrower ceiling | My judgement |
| Self-employed base ratio | 35% | More conservative documentation/income treatment | My judgement |
| Informal/gig base ratio | 30% | More conservative volatility/documentation treatment | My judgement |
| Unknown stability adjustment | -5 percentage points | Missing stability increases uncertainty | My judgement |
| Highly variable adjustment | -5 percentage points | Less predictable cash flow | My judgement |
| Expense guard | 60% of income after essential expenses + rent | Preserves a cash-flow buffer | My judgement |
| Savings <1 month | -15% | Low resilience | My judgement |
| Savings 1 to <3 months | -5% | Limited resilience | My judgement |
| Savings >=6 months | +5% | Stronger resilience, still subject to other caps | My judgement |
| Savings unknown | -5% | No positive benefit assumed while unknown | My judgement |
| Variable income >30% | -5% | More monthly volatility | My judgement |
| Variable income >50% | -10% | Stronger volatility haircut | My judgement |
| Upcoming expense >3 months ordinary income | -10% | Protects known near-term cash need | My judgement |
| Recent bounce | -15% | Current repayment behaviour is a material stress signal | My judgement |
| High-cost debt outstanding >₹0 | -15% | Existing expensive debt reduces resilient room | My judgement |
| 3+ high-cost/app loans | -5% | Multiple expensive obligations increase complexity/stress | My judgement |
| Existing EMI | Subtracted from total safe debt-payment capacity | Existing obligations come before new EMI | My judgement |
| Existing EMI unknown | Reserve 15% of ordinary income | Unknown is not zero; conservative reserve is explicit | My judgement |
| Minimum meaningful new EMI capacity | ₹2,000 | Below this, V1 treats new borrowing capacity as too small to be useful | My judgement |

Formula:

```text
base safe total debt payment
= min(income × product/income-type ratio,
      (qualifying income − essential expenses used − rent used) × 60%)
```

Then resilience adjustments are applied.

```text
maximum comfortable new EMI
= safe total debt payment − existing EMI used
```

If the result is negative, it is set to ₹0.

## 8. Existing EMI tenure

| Rule | Value | Why | Source |
|---|---|---|---|
| Positive existing EMI | Remaining tenure required, 1–360 months | Current obligation must be time-bounded | My judgement |
| Zero existing EMI | Tenure must be blank | Prevents contradictory input | My judgement |
| Unknown existing EMI | 15% ordinary-income reserve | Unknown is not zero | My judgement |
| Current affordability | Known EMI counted today | Future expiry does not remove today's obligation | My judgement |
| Near-expiry lender illustration | <=12 months → +5% indicative lender capacity | A near-expiring obligation may be viewed differently by some lenders; not a policy claim | My judgement |
| Near-expiry fair-rate illustration | <=12 months → -0.25 percentage point | Small recognition without pretending the EMI is already gone | My judgement |

## 9. Indicative lender-capacity bands

The lender range is intentionally an **illustration**, not a lender approval formula.

| Product | Base multiplier of monthly lender-qualifying income | Source / why |
|---|---:|---|
| Personal | 18× | My judgement informed by mainstream personal-loan sizing |
| Home | 60× | Long-tenure secured-product illustration |
| LAP | 48× | Secured mortgage-backed illustration |
| Gold | 12× | Short-tenure secured-product illustration |
| Two-wheeler | 24× | Vehicle-finance illustration |
| Business | 24× | Small-business finance illustration |

Adjustments:

| Signal | Adjustment | Source |
|---|---:|---|
| Credit >=750 | +10% | My judgement |
| Credit <650 | -35% | My judgement |
| Credit unknown | -20% | Avoid assuming strong credit | My judgement |
| Highly variable income | -15% | Income uncertainty | My judgement |
| Age 55–64 | -15% | Indicative remaining-tenure consideration | My judgement |
| Age >=65 | -30% | Indicative remaining-tenure consideration | My judgement |
| Existing EMI unknown | -15% | Missing debt information | My judgement |
| Existing EMI <=12 months | +5% | Near-expiry illustration | My judgement |

For self-employed borrowers, when ITR/documented annual income is supplied, lender-qualifying monthly income is the lower of ordinary monthly income and ITR/12.

### Secured route

For LAP/business borrowing:

```text
collateral illustration = 60% × reported collateral value
```

When collateral is reported as unencumbered, the lender range may use the greater of the income-based capacity and this collateral illustration, still capped at the amount requested.

This is **not** a lender LTV promise. Actual valuation, LTV, documentation, legal checks and underwriting are lender-specific.

## 10. Fair-rate bands

Starting V1 bands:

| Product | Starting fair-rate band | Source / why |
|---|---:|---|
| Personal | 10.00%–16.50% | Market-informed anchor; ICICI currently publishes 9.99%–16.50% on its personal-loan rate page | Official lender anchor + my judgement |
| Home | 7.25%–10.50% | Secured housing illustration | My judgement |
| LAP | 10.00%–13.00% | Secured borrowing illustration | My judgement informed by published lender ranges |
| Gold | 8.75%–15.00% | Secured short-tenure illustration | My judgement informed by published lender ranges |
| Two-wheeler | 9.50%–18.00% | Product/borrower variation | My judgement |
| Business | 10.50%–18.00% | Small-business variation | My judgement |

Profile adjustments are in **percentage points**:

| Signal | Lower bound | Upper bound | Why | Source |
|---|---:|---:|---|---|
| Credit >=780 | -1.00 pp | -1.00 pp | Strong credit signal | My judgement |
| Credit 750–779 | -0.50 pp | -0.50 pp | Strong credit signal | My judgement |
| Credit 700–749 | 0 | 0 | Neutral V1 treatment | My judgement |
| Credit 650–699 | +1.00 pp | +1.50 pp | Higher uncertainty | My judgement |
| Credit <650 | +2.00 pp | +3.00 pp | Higher risk/uncertainty | My judgement |
| Credit unknown | 0 | +2.00 pp | Unknown is not treated as bad credit; uncertainty widens the band | Assignment / my judgement |
| Stable income | -0.25 pp | -0.25 pp | Predictability | My judgement |
| Highly variable income | +1.00 pp | +1.50 pp | Volatility | My judgement |
| Informal/gig income | +1.50 pp | +2.00 pp | Documentation/volatility uncertainty | My judgement |
| Variable income >30% | +0.50 pp | +0.75 pp | Volatility | My judgement |
| Variable income >50% | +0.75 pp | +1.25 pp | Higher volatility | My judgement |
| Existing EMI >20% of minimum income | +0.50 pp | +1.00 pp | Existing debt burden | My judgement |
| Existing EMI unknown | 0 | +0.75 pp | Missing debt information | My judgement |
| High-cost debt outstanding >₹0 | +1.00 pp | +2.00 pp | Expensive debt signal | My judgement |
| 3+ high-cost/app loans | +0.50 pp | +0.75 pp | Multiple expensive obligations | My judgement |
| High-cost debt rate >25% | +0.50 pp | +1.00 pp | Expensive existing borrowing | My judgement |
| Recent bounce | +1.00 pp | +2.00 pp | Recent repayment problem | My judgement |
| Existing EMI <=12 months | -0.25 pp | -0.25 pp | Small near-expiry recognition | My judgement |

Final lower bound is never below **6.50%** and the upper bound is always at least **0.50 percentage points above the lower bound**.

## 11. Safe amount

1. Calculate maximum comfortable new EMI.
2. Convert that EMI to principal using the **upper fair-rate bound** and the product's conservative V1 term.
3. Do not cap safe capacity using the lender range.
4. If key information is unknown, widen the safe range by using 70% of the calculated upper amount as the lower bound instead of 85%.
5. Round displayed money to the nearest ₹100.
6. If maximum new EMI is below ₹2,000, safe amount is `₹0–₹0`.
7. For two-wheeler borrowing, safe principal is capped at the requested amount.

The Negotiation Card's recommended amount is:

```text
DON'T BORROW -> ₹0
otherwise -> min(requested amount, conservative safe lower bound)
```

## 12. Decision rules

| Priority | Decision | Trigger | Why |
|---:|---|---|---|
| 1 | DON'T BORROW | Existing EMI used >50% of ordinary income | Severe current debt burden |
| 2 | DON'T BORROW | New-EMI capacity <₹2,000 | No meaningful resilient room |
| 3 | DON'T BORROW | Recent bounce + high-cost debt | Combined repayment stress |
| 4 | DON'T BORROW | Recent bounce + 3+ high-cost loans + existing EMI unknown | Multiple expensive obligations plus unknown current EMI burden |
| 5 | BORROW LESS | Requested amount > safe maximum | Requested amount exceeds borrower-safe capacity |
| 6 | BORROW LESS | Requested EMI at conservative fair-rate assumption > safe EMI ceiling | Requested amount is too large for monthly capacity |
| 7 | BORROW | None of the above triggers | Request fits the borrower-side test |

`DON'T BORROW` is intentionally reachable.

## 13. Stress case

The stress case reduces ordinary monthly income by **20%** and keeps the proposed EMI unchanged.

```text
stressed income = ordinary income × 80%
stressed FOIR = (existing EMI used + proposed EMI) / stressed income × 100
```

| Stressed FOIR | Display | Why |
|---:|---|---|
| <=40% | Within conservative stress boundary | Manageable under this simplified test |
| >40% and <=50% | Caution | Repayment becomes meaningfully tighter |
| >50% | Stretched | High burden after income shock |

This is a resilience scenario, not a prediction.

## 14. Lender quote and “4 points over fair”

The lender quote checker accepts:

- lender principal or purchase/asset amount;
- down payment for home/two-wheeler;
- quoted annual interest rate;
- tenure;
- quoted EMI, optional;
- mandatory upfront fees.

For home/two-wheeler:

```text
financed amount = purchase/asset amount − down payment
```

For other products:

```text
financed amount = entered lender principal
```

Rate premium:

```text
premium points = quoted lender rate − fair-rate upper bound
```

Example:

```text
16.50% quoted − 12.50% fair upper bound = 4.00 percentage points over fair
```

The UI explicitly says **percentage points** so “4 points over” cannot be confused with a 4% fee or 4% of principal.

### EMI

The app calculates standard reducing-balance amortizing EMI.

If lender EMI is supplied, the app shows its percentage variance from the calculated EMI.

A quoted EMI below the first month's interest is rejected as inconsistent with a normal amortizing schedule.

### Illustrative APR

The calculator finds an annualized effective rate whose monthly cash-flow present value equals the net amount after the entered upfront mandatory fee.

For the assessment screen, the assumed mandatory fee range is:

```text
0.5%–2.0% of recommended principal
```

For a lender quote, the actual entered mandatory fee is used.

The official lender KFS/APR remains authoritative.

## 15. KFS / RBI grounding

RBI's April 15, 2024 KFS framework applies to retail and MSME term loans from regulated entities and requires a standardized Key Facts Statement containing key loan information and the all-in cost/APR framework.

Reference:

- RBI circular: `DOR.STR.REC.13/13.03.00/2024-25`, April 15, 2024.
- RBI website: https://www.rbi.org.in/

The app therefore instructs the borrower to compare the lender's official KFS/APR, mandatory fees and repayment schedule.

## 16. Market anchors

The following are anchors, not promises:

| Source | Published information used as context | How used |
|---|---|---|
| ICICI Bank | Personal loan 9.99%–16.50% p.a.; processing fee up to 2% + taxes on current rate page | Supports the V1 personal-loan starting band |
| HDFC Bank | Personal-loan rack rate around 9.99%–24.00%; quarterly IRR/APR statistics disclosed | Demonstrates the breadth of actual lender pricing |
| RBI | KFS/APR disclosure framework | Supports all-in-cost comparison principle |

Published rates change. V1 does not present them as guaranteed current offers.

## 17. Product routing

| Condition | Route | Why | Source |
|---|---|---|---|
| Business/LAP + collateral value supplied + unencumbered = Yes | SECURED BUSINESS FINANCING / LAP (CONSIDER) | Secured route may be worth comparing with unsecured borrowing | My judgement |
| Otherwise | Selected product | Preserve borrower-selected product context | Assignment |

For Ravi, reported unencumbered shop/property is therefore a reason to compare a secured business/LAP route.

## 18. What the app does not know

- Credit score may be unknown.
- Income is self-reported.
- Expenses are self-reported.
- Rent is self-reported.
- ITR is not verified.
- Collateral value is not verified.
- Employment/business continuity is not verified.
- Existing debt is self-reported.
- Lender-specific underwriting is unavailable.
- Actual lender fees can differ from V1 assumptions.
- Official contractual APR/KFS is outside the app's control.

## 19. Confidence

Confidence is based on the proportion of material questions actually answered on the selected adaptive path.

| Confidence | Rule | Why |
|---|---|---|
| HIGH | >=82% of material inputs known and no key affordability input is unknown | Most material inputs are available |
| MEDIUM | >=55% known | Core information exists but meaningful uncertainty remains |
| LOW | <55% known | Many material inputs are unknown |

A high-confidence result is blocked when important affordability inputs such as credit score, rent, emergency savings, essential expenses, existing EMI or income stability remain unknown.

## 20. Traceability standard

Every major number must have a one-sentence explanation:

| Output | Explanation |
|---|---|
| Verdict | Requested amount/EMI compared with safe capacity and current repayment stress |
| Lender range | Product multiplier + lender-qualifying income + credit/stability/age/collateral adjustments |
| Safe amount | Safe new-EMI ceiling converted at the upper fair-rate bound |
| Fair rate | Product anchor + profile adjustments in percentage points |
| EMI ceiling | Income + essential expenses + rent + existing debt + resilience adjustments |
| Stress | Same proposed EMI under 20% lower income |
| Confidence | Material inputs known on the adaptive path |
| Product route | Selected product + collateral information where relevant |

## 21. Acceptance borrowers

### Priya

- 29, salaried
- ₹1,10,000/month
- existing car EMI ₹14,000, 24 months remaining
- rent ₹28,000
- credit score 780
- stable income
- wants ₹8,00,000 personal loan for wedding

Expected: BORROW is reachable; rent and existing EMI are counted; strong credit reduces the fair-rate band.

### Ravi

- 42, self-employed
- ₹40,000–₹80,000/month cash income
- ITR ₹4,20,000/year
- unencumbered property around ₹45,00,000
- no formal credit history / no score
- wife earns ₹18,000
- wants ₹15,00,000 for stock + delivery vehicle

Expected: BORROW LESS; documented income constrains lender income; collateral routes him toward a secured comparison; unknown score widens pricing uncertainty.

### Anita

- 35, informal/gig
- ₹26,000–₹30,000/month
- three app loans
- ₹35,000 total outstanding at 30%+
- one recent bounce
- two children; husband unemployed
- wants ₹1,50,000 for an electric scooter

The assignment does **not** provide Anita's existing monthly EMI. The app therefore must not invent one. If she leaves existing EMI unknown, V1 uses the documented 15% income reserve. Combined with high-cost debt, multiple app loans, recent bounce and highly variable income, the decision can reach DON'T BORROW.

## 22. No-ML rationale

V1 deliberately avoids ML. The product is a transparent borrower self-assessment, and deterministic rules make it possible to explain every threshold and every output during a lender-branch conversation and an engineering review.
