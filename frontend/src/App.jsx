import { useMemo, useState } from 'react'
import { assess, checkOffer } from './api'

const initial = {
  name:'', loanType:'PERSONAL', amountWanted:'', purpose:'', incomeType:'SALARIED',
  monthlyIncomeMin:'', monthlyIncomeMax:'', existingEmi:'', existingEmiMonthsRemaining:'', essentialExpenses:'',
  age:'', creditScore:'', incomeStability:'UNKNOWN', rent:'', emergencySavingsMonths:'',
  variableIncomeShare:'', hasCreditHistory:null, hasHighCostDebt:null, highCostLoanCount:'',
  highCostDebtOutstanding:'', highCostDebtRatePercent:'', recentBounceCount:'',
  collateralValue:'', collateralUnencumbered:null, documentedAnnualIncome:'',
  expectedMonthlyIncomeIncrease:'', upcomingLargeExpense:'', coApplicant:false, coApplicantMonthlyIncome:''
}

const money = n => new Intl.NumberFormat('en-IN',{style:'currency',currency:'INR',maximumFractionDigits:0}).format(Number(n||0))
const pct = n => `${Number(n||0).toFixed(2)}%`

function Field({label, children, hint}) {
  return <label className="field">
      <span>{label}</span>{children}{hint && <small>{hint}</small>}
      </label>
}

export default function App(){
  const [form,setForm] = useState(initial)
  const [result,setResult] = useState(null)
  const [loading,setLoading] = useState(false)
  const [error,setError] = useState('')
  const [step,setStep] = useState(1)

  const set=(key,value)=>setForm(f=>({...f,[key]:value}))
  const hasCredit = form.hasCreditHistory === true
  const hasExistingEmi = Number(form.existingEmi || 0) > 0
  const hasHighCostDebt = form.hasHighCostDebt === true
  const variableIncome = ['VARIABLE','HIGHLY_VARIABLE','UNKNOWN'].includes(form.incomeStability)

  function normalizePayload(){
    const payload = {...form}
    for(const key of Object.keys(payload)) if(payload[key] === '') payload[key] = null

    ;[
      'amountWanted','monthlyIncomeMin','monthlyIncomeMax','existingEmi','existingEmiMonthsRemaining',
      'essentialExpenses','rent','emergencySavingsMonths','variableIncomeShare','highCostLoanCount',
      'highCostDebtOutstanding','highCostDebtRatePercent','collateralValue','documentedAnnualIncome',
      'expectedMonthlyIncomeIncrease','upcomingLargeExpense','coApplicantMonthlyIncome'
    ].forEach(key=>{
      if(payload[key] != null) payload[key] = Number(payload[key])
    })
    ;['age','creditScore','recentBounceCount'].forEach(key=>{
      if(payload[key] != null) payload[key] = Number(payload[key])
    })

    if(payload.existingEmi == null || payload.existingEmi === 0) payload.existingEmiMonthsRemaining = null
    if(!hasCredit){
      payload.recentBounceCount = null
      payload.hasHighCostDebt = null
      payload.highCostLoanCount = null
      payload.highCostDebtOutstanding = null
      payload.highCostDebtRatePercent = null
    }
    if(!hasHighCostDebt){
      payload.highCostLoanCount = null
      payload.highCostDebtOutstanding = null
      payload.highCostDebtRatePercent = null
    }
    if(!variableIncome) payload.monthlyIncomeMax = null
    if(!variableIncome) payload.variableIncomeShare = null
    if(payload.loanType !== 'LAP' && payload.loanType !== 'BUSINESS'){
      payload.collateralValue = null
      payload.collateralUnencumbered = null
    }
    if(payload.incomeType !== 'SELF_EMPLOYED') payload.documentedAnnualIncome = null
    if(!['BUSINESS','TWO_WHEELER'].includes(payload.loanType)) payload.expectedMonthlyIncomeIncrease = null
    if(!payload.coApplicant){ payload.coApplicantMonthlyIncome = null }
    return payload
  }

  async function submit(e){
    e.preventDefault(); setError('')
    if(hasExistingEmi && !form.existingEmiMonthsRemaining){
      setError('Please enter the remaining tenure for your existing EMI.')
      return
    }
    if(hasHighCostDebt && form.highCostLoanCount !== '' && Number(form.highCostLoanCount) < 1){
      setError('Enter at least 1 high-cost/app loan, or choose No.')
      return
    }
    try{
      setLoading(true)
      setResult(await assess(normalizePayload()))
      setStep(3)
    }catch(err){setError(err.message)}
    finally{setLoading(false)}
  }

  if(result) return <Results data={result} reset={()=>{setResult(null);setForm(initial);setStep(1)}} />

  function goToDetails(){
    setError('')
    const required = [
      ['amountWanted','Enter the amount you want to borrow.'],
      ['purpose','Enter the purpose of the loan.'],
      ['monthlyIncomeMin','Enter your minimum net monthly income.'],
      ['age','Enter your age.']
    ]
    for(const [key,message] of required){
      if(form[key] === '' || form[key] == null){ setError(message); return }
    }
    if(form.monthlyIncomeMax !== '' && Number(form.monthlyIncomeMax) < Number(form.monthlyIncomeMin)){
      setError('Maximum monthly income cannot be below minimum monthly income.')
      return
    }
    if(hasExistingEmi && !form.existingEmiMonthsRemaining){
      setError('Please enter the remaining tenure for your existing EMI.')
      return
    }
    if(hasExistingEmi && Number(form.existingEmiMonthsRemaining) > 360){
      setError('Existing EMI remaining tenure cannot exceed 360 months.')
      return
    }
    setStep(2)
  }

  return <main className="app">
    <header>
      <div className="brand">Borrower Copilot</div>
      <div className="tag">Know your number before the lender gives you theirs.</div>
    </header>

    <section className="hero">
      <div><p className="eyebrow">INDIA · BORROWER-FIRST</p><h1>Make the borrowing decision before you enter the branch.</h1>
      <p>Self-assess your safe amount, likely lender range, fair-rate band and EMI ceiling. Nothing is saved.</p></div>
    </section>

    <form onSubmit={submit} className="card form">
      <div className="progress"><span className={step>=1?'active':''}>1 Profile</span><i></i><span className={step>=2?'active':''}>2 Details</span></div>

      {step===1 && <div className="grid">
        <Field label="Your name (optional)" hint="Used only in this session; it is not stored or used in calculations."><input value={form.name} onChange={e=>set('name',e.target.value)} placeholder="e.g. Priya" /></Field>
        <Field label="Loan type"><select value={form.loanType} onChange={e=>set('loanType',e.target.value)}>
          <option value="PERSONAL">Personal loan</option><option value="HOME">Home loan</option><option value="LAP">Loan against property</option>
          <option value="GOLD">Gold loan</option><option value="TWO_WHEELER">Two-wheeler loan</option><option value="BUSINESS">Business loan</option>
        </select></Field>
        <Field label="Amount wanted"><input required type="number" min="1000" value={form.amountWanted} onChange={e=>set('amountWanted',e.target.value)} placeholder="₹ 8,00,000"/></Field>
        <Field label="Purpose"><input required value={form.purpose} onChange={e=>set('purpose',e.target.value)} placeholder="e.g. wedding, stock, scooter"/></Field>
        <Field label="Income type"><select value={form.incomeType} onChange={e=>set('incomeType',e.target.value)}>
          <option value="SALARIED">Salaried</option><option value="SELF_EMPLOYED">Self-employed</option><option value="INFORMAL_GIG">Informal / gig</option>
        </select></Field>
        <Field label="Net monthly income (minimum)"><input required type="number" min="0.01" value={form.monthlyIncomeMin} onChange={e=>set('monthlyIncomeMin',e.target.value)} placeholder="₹"/></Field>
        {variableIncome && <Field label="Net monthly income (maximum)" hint="Optional. Leave blank if you do not know the upper end."><input type="number" min="0" value={form.monthlyIncomeMax} onChange={e=>set('monthlyIncomeMax',e.target.value)} placeholder="Optional"/></Field>}
        <Field label="Existing monthly EMIs" hint="Enter 0 if you have none. Leave blank only if the amount is unknown."><input type="number" min="0" value={form.existingEmi} onChange={e=>{set('existingEmi',e.target.value); if(Number(e.target.value||0)===0) set('existingEmiMonthsRemaining','')}} placeholder="₹"/></Field>
        {hasExistingEmi && <Field label="Existing EMI tenure remaining (months)" hint="Required when existing EMI is above ₹0; today's affordability keeps this EMI until it ends."><input required type="number" min="1" max="360" value={form.existingEmiMonthsRemaining} onChange={e=>set('existingEmiMonthsRemaining',e.target.value)} placeholder="e.g. 24"/></Field>}
        <Field label="Essential household expenses (optional)" hint="Excluding rent. If blank, V1 estimates 25% of ordinary income rather than using ₹0."><input type="number" min="0" value={form.essentialExpenses} onChange={e=>set('essentialExpenses',e.target.value)} placeholder="Optional"/></Field>
        <Field label="Age"><input required type="number" min="18" max="80" value={form.age} onChange={e=>set('age',e.target.value)}/></Field>
        <Field label="Credit score" hint="Leave blank if unknown. Unknown is not treated as a bad score."><input type="number" min="300" max="900" value={form.creditScore} onChange={e=>set('creditScore',e.target.value)} placeholder="I don't know"/></Field>
        <Field label="Income stability"><select value={form.incomeStability} onChange={e=>{set('incomeStability',e.target.value); if(e.target.value==='STABLE'){set('monthlyIncomeMax','');set('variableIncomeShare','')}}}>
          <option value="STABLE">Stable</option><option value="VARIABLE">Variable</option><option value="HIGHLY_VARIABLE">Highly variable</option><option value="UNKNOWN">I don't know</option>
        </select></Field>
        <button type="button" className="primary full" onClick={goToDetails}>Continue →</button>
      </div>}

      {step===2 && <div className="grid">
        <Field label="Monthly residential rent (optional)" hint="Asked for every borrower. Enter 0 if you do not pay residential rent; leave blank if unknown."><input type="number" min="0" value={form.rent} onChange={e=>set('rent',e.target.value)} placeholder="Optional"/></Field>
        <Field label="Emergency savings (months, optional)" hint="Leave blank if unknown. Unknown is not treated as 0 months."><input type="number" min="0" value={form.emergencySavingsMonths} onChange={e=>set('emergencySavingsMonths',e.target.value)} placeholder="e.g. 4"/></Field>
        {variableIncome && <Field label="Variable income share (%)" hint="Approximate share of monthly income that can change."><input type="number" min="0" max="100" value={form.variableIncomeShare} onChange={e=>set('variableIncomeShare',e.target.value)} placeholder="Optional"/></Field>}
        <Field label="Credit history"><select value={form.hasCreditHistory===null?'':String(form.hasCreditHistory)} onChange={e=>{
          const v=e.target.value===''?null:e.target.value==='true'; set('hasCreditHistory',v);
          if(v!==true){set('recentBounceCount','');set('hasHighCostDebt',null);set('highCostLoanCount','');set('highCostDebtOutstanding','');set('highCostDebtRatePercent','')}
        }}>
          <option value="">I don't know</option><option value="true">Yes</option><option value="false">No formal/app credit</option>
        </select></Field>

        {hasCredit && <Field label="Payment bounces in last 6 months" hint="Relevant to any borrower with credit history, not just informal/gig workers. Leave blank if unknown."><input type="number" min="0" max="100" value={form.recentBounceCount} onChange={e=>set('recentBounceCount',e.target.value)} placeholder="Optional"/></Field>}

        {hasCredit && <Field label="Any high-cost/app loans?" hint="High-cost means materially expensive debt, such as app loans above ordinary mainstream pricing."><select value={form.hasHighCostDebt===null?'':String(form.hasHighCostDebt)} onChange={e=>{
          const v=e.target.value===''?null:e.target.value==='true'; set('hasHighCostDebt',v);
          if(v!==true){set('highCostLoanCount','');set('highCostDebtOutstanding','');set('highCostDebtRatePercent','')}
        }}>
          <option value="">I don't know</option><option value="true">Yes</option><option value="false">No</option>
        </select></Field>}

        {hasCredit && hasHighCostDebt && <>
          <Field label="Active high-cost/app loan count"><input type="number" min="1" max="100" value={form.highCostLoanCount} onChange={e=>set('highCostLoanCount',e.target.value)} placeholder="e.g. 3"/></Field>
          <Field label="Total high-cost/app debt outstanding (₹)"><input type="number" min="0" value={form.highCostDebtOutstanding} onChange={e=>set('highCostDebtOutstanding',e.target.value)} placeholder="e.g. 35000"/></Field>
          <Field label="Approximate high-cost debt rate (%)"><input type="number" min="0" max="100" step="0.01" value={form.highCostDebtRatePercent} onChange={e=>set('highCostDebtRatePercent',e.target.value)} placeholder="e.g. 30"/></Field>
        </>}

        {form.incomeType==='SELF_EMPLOYED' && <Field label="Annual documented/ITR income" hint="Used for indicative lender capacity; it does not override your cash-flow affordability."><input type="number" min="0" value={form.documentedAnnualIncome} onChange={e=>set('documentedAnnualIncome',e.target.value)} placeholder="₹"/></Field>}
        {(form.loanType==='BUSINESS'||form.loanType==='TWO_WHEELER') && <Field label="Expected monthly income increase" hint="Only 50% is counted for affordability; expected income is not treated as guaranteed."><input type="number" min="0" value={form.expectedMonthlyIncomeIncrease} onChange={e=>set('expectedMonthlyIncomeIncrease',e.target.value)} placeholder="Optional"/></Field>}
        <Field label="Upcoming large expense" hint="Enter the amount if known; leave blank if unknown."><input type="number" min="0" value={form.upcomingLargeExpense} onChange={e=>set('upcomingLargeExpense',e.target.value)} placeholder="Optional"/></Field>
        <Field label="Co-applicant?" hint="Optional. If yes, their income can conservatively increase your capacity."><select value={String(form.coApplicant)} onChange={e=>{const v=e.target.value==='true'; set('coApplicant',v); if(!v) set('coApplicantMonthlyIncome','')}}><option value="false">No</option><option value="true">Yes</option></select></Field>
        {form.coApplicant && <Field label="Co-applicant net monthly income"><input type="number" min="0" value={form.coApplicantMonthlyIncome} onChange={e=>set('coApplicantMonthlyIncome',e.target.value)} placeholder="₹"/></Field>}
        {(form.loanType==='LAP'||form.loanType==='BUSINESS') && <>
          <Field label="Collateral value"><input type="number" min="0" value={form.collateralValue} onChange={e=>set('collateralValue',e.target.value)} placeholder="Optional"/></Field>
          <Field label="Collateral unencumbered?" hint="Applicable to both self-employed and other borrowers using LAP/business borrowing."><select value={form.collateralUnencumbered===null?'':String(form.collateralUnencumbered)} onChange={e=>set('collateralUnencumbered',e.target.value===''?null:e.target.value==='true')}><option value="">I don't know</option><option value="false">No</option><option value="true">Yes</option></select></Field>
        </>}
        <div className="notice full">No login. No bureau pull. No borrower information is persisted by this V1 application. Unknown values remain unknown; explicit fallbacks are documented.</div>
        {error && <div className="error full">{error}</div>}
        <button type="button" className="secondary" onClick={()=>setStep(1)}>← Back</button>
        <button disabled={loading} className="primary">{loading?'Calculating…':'Get my borrower numbers →'}</button>
      </div>}
    </form>

    <footer>Indicative self-assessment only. It is not a lender approval or financial guarantee.</footer>
  </main>
}

function Results({data,reset}){
  const [quoteAmount,setQuoteAmount]=useState(String(data.recommendedAmount || data.safeAmount.max || ''))
  const [downPayment,setDownPayment]=useState('0')
  const [quoteRate,setQuoteRate]=useState('')
  const [quoteFee,setQuoteFee]=useState('')
  const [quoteTenure,setQuoteTenure]=useState(String(data.tenureOptions?.[data.tenureOptions.length-1]?.months || 60))
  const [quoteEmi,setQuoteEmi]=useState('')
  const [offer,setOffer]=useState(null)
  const [offerLoading,setOfferLoading]=useState(false)
  const [offerError,setOfferError]=useState('')

  const downPaymentRelevant = ['HOME','TWO WHEELER'].includes(data.negotiationCard.loan)
  const selectedTenure = useMemo(()=>Number(quoteTenure),[quoteTenure])

  async function evaluateOffer(){
    setOfferError('')
    if(!quoteAmount || !quoteRate || !quoteTenure || quoteFee === ''){
      setOfferError('Enter the lender amount, interest rate, tenure and mandatory upfront fees first.')
      return
    }
    const financed = Number(quoteAmount) - Number(downPayment || 0)
    if(financed <= 0){
      setOfferError('Down payment must be less than the purchase/loan amount.')
      return
    }
    try{
      setOfferLoading(true)
      const out = await checkOffer({
        loanAmount:Number(quoteAmount), downPayment:Number(downPayment || 0), quotedRatePercent:Number(quoteRate),
        tenureMonths:selectedTenure, upfrontFees:Number(quoteFee), quotedEmi:quoteEmi === '' ? 0 : Number(quoteEmi),
        safeEmiCeiling:Number(data.maximumSafeEmi), fairRateMin:Number(data.fairRate.minPercent), fairRateMax:Number(data.fairRate.maxPercent),
        existingEmi:Number(data.existingEmiUsedForCalculation || 0), monthlyIncome:Number(data.affordabilityIncome || data.monthlyIncome || 0)
      })
      setOffer(out)
    }catch(err){setOfferError(err.message)}
    finally{setOfferLoading(false)}
  }

  const offerVerdict = offer?.verdict
  const negotiationHeadline = offerVerdict ? offerVerdict : data.negotiationCard.headline
  const negotiationAsk = offer?.negotiationAsk || data.negotiationCard.ask
  const existingEmiLabel = data.existingEmi == null ? 'Estimated existing + proposed EMI ratio' : 'Existing + proposed EMI ratio'
  const expenseText = data.essentialExpensesEstimated ? `using the V1 fallback of ${money(data.essentialExpensesUsed)}` : `using your ${money(data.essentialExpensesUsed)} stated expenses`
  const rentText = data.rentEstimated ? `a ${money(data.rentUsed)} rent fallback` : `your ${money(data.rentUsed)} stated rent`

  return <main className="app results">
    <header><div className="brand">Borrower Copilot</div><button className="link" onClick={reset}>Start over</button></header>
    <section className={`decision ${data.decision==='DONT_BORROW'?'danger':data.decision==='BORROW_LESS'?'warn':'good'}`}>
      <p className="eyebrow">YOUR VERDICT</p><h1>{data.decision==='DONT_BORROW'?'Don’t borrow':data.decision==='BORROW_LESS'?'Borrow less':'Borrow'}</h1>
      <p>{data.decisionReason}</p>
    </section>

    <div className="cards">
      <ResultCard title="Likely lender range" value={`${money(data.lenderSanction.min)} – ${money(data.lenderSanction.max)}`} why={`Indicative capacity from your ${money(data.affordabilityIncome)} monthly qualifying income and selected product; it is not an approval prediction.`} />
      <ResultCard title="Borrower-safe amount" value={`${money(data.safeAmount.min)} – ${money(data.safeAmount.max)}`} highlight why={`Converted from your ${money(data.maximumSafeEmi)} new-EMI ceiling at the upper fair-rate bound; use the conservative end for your decision.`} />
      <ResultCard title="Fair rate for your profile" value={`${pct(data.fairRate.minPercent)} – ${pct(data.fairRate.maxPercent)}`} why={`Illustrative APR with V1 assumed mandatory fees: ${pct(data.illustrativeApr.fairRateMinApr)} – ${pct(data.illustrativeApr.fairRateMaxApr)}; compare the lender's official KFS/APR for the actual all-in cost.`} />
      <ResultCard title="Maximum comfortable new EMI" value={money(data.maximumSafeEmi)} why={`After the affordability ratio, ${expenseText}, ${rentText}, existing debt and resilience adjustments.`} />
    </div>

    <section className="card">
      <h2>Tenure trade-off</h2>
      <div className="table">{data.tenureOptions.map(t=><div className="row" key={t.months}><b>{t.months/12} years</b><span>EMI {money(t.emi)}</span><span>Interest {money(t.totalInterest)}</span></div>)}</div>
    </section>

    <section className="card stress"><h2>Stress case</h2><p>{data.stress.scenario}: <b>{money(data.stress.stressedIncome)}</b> income.</p><p>{existingEmiLabel}: <b>{pct(data.stress.stressedFoirPercent)}</b></p><strong>{data.stress.outcome}</strong></section>

    <section className="card"><h2>Fair-rate confidence</h2><div className="confidence">{data.confidence}</div><p>{data.confidenceReason}</p>
      <p className="muted">Every range is wider when important information is missing. Unknown credit score is not treated as a low score.</p>
    </section>

    <section className="card"><h2>Product route</h2><h3>{data.productRecommendation.product}</h3><p>{data.productRecommendation.reason}</p></section>

    <section className="card"><h2>Compare a lender quote</h2><div className="grid">
      <Field label={downPaymentRelevant ? "Purchase / asset amount (₹)" : "Lender principal (₹)"}>
        <input type="number" min="1000" value={quoteAmount} onChange={e=>setQuoteAmount(e.target.value)} placeholder="e.g. 800000"/>
      </Field>
      {downPaymentRelevant && <Field label="Down payment (₹)" hint="A higher down payment lowers the financed amount and EMI, but uses more cash upfront.">
        <input type="number" min="0" value={downPayment} onChange={e=>setDownPayment(e.target.value)} placeholder="0"/>
      </Field>}
      <Field label="Quoted interest rate (%)"><input type="number" step="0.01" min="0.01" value={quoteRate} onChange={e=>setQuoteRate(e.target.value)} placeholder="e.g. 14"/></Field>
      <Field label="Tenure">
        <select value={quoteTenure} onChange={e=>setQuoteTenure(e.target.value)}>{data.tenureOptions.map(t=><option key={t.months} value={t.months}>{t.months/12} years</option>)}</select>
      </Field>
      <Field label="Quoted EMI (₹)" hint="Optional. Leave blank and Borrower Copilot calculates it."><input type="number" min="0" value={quoteEmi} onChange={e=>setQuoteEmi(e.target.value)} placeholder="Optional"/></Field>
      <Field label="Upfront mandatory fees (₹)" hint="Enter the lender's disclosed mandatory fees. Use 0 only when the lender confirms there are none."><input required type="number" min="0" value={quoteFee} onChange={e=>setQuoteFee(e.target.value)} placeholder="e.g. 13000"/></Field>
      <button type="button" className="primary full" disabled={offerLoading} onClick={evaluateOffer}>{offerLoading?'Checking offer…':'Check this lender offer →'}</button>
      {offerError && <div className="error full">{offerError}</div>}
      {offer && <div className="apr full">
        <b>{offer.verdict}</b>
        <p>{offer.reason}</p>
        <small>Financed amount: {money(offer.financedAmount)} · EMI: {money(offer.lenderEmi)} · Calculated EMI: {money(offer.calculatedEmi)} · Total interest: {money(offer.totalInterest)} · Illustrative APR: {pct(offer.illustrativeApr)}</small>
        <small>Quoted EMI variance vs calculation: {Number(offer.emiDifferencePercent).toFixed(2)}% · Rate premium over fair upper bound: {Number(offer.ratePremiumPoints).toFixed(2)} percentage points · Total EMI burden: {pct(offer.totalFoirPercent)}</small>
        <small>{offer.stressOutcome}</small>
        <small>Use the lender's official KFS/APR as the contractual source.</small>
      </div>}
    </div></section>

    <section className="negotiation">
      <div className="neg-head"><span>NEGOTIATION CARD</span><b>{data.confidence} CONFIDENCE</b></div>
      <h2>{negotiationHeadline === 'DON\'T AGREE YET' || negotiationHeadline === 'NEGOTIATE' || negotiationHeadline === 'NEGOTIATE HARD' ? negotiationHeadline : data.negotiationCard.headline}</h2>
      <div className="neg-grid">
        <div><small>REQUESTED</small><b>{money(data.negotiationCard.requested)}</b></div>
        <div><small>SAFE AMOUNT</small><b>{money(data.negotiationCard.recommendedAmount)}</b></div>
        <div><small>FAIR RATE</small><b>{pct(data.negotiationCard.fairRate.minPercent)} – {pct(data.negotiationCard.fairRate.maxPercent)}</b></div>
        <div><small>MAX EMI</small><b>{money(data.negotiationCard.maxEmi)}</b></div>
      </div>
      {offer && <p className="ask"><b>Lender quote: {pct(offer.quotedRatePercent)} · EMI {money(offer.lenderEmi)}</b><br/>{offer.ratePremiumPoints > 0 ? `Rate is ${Number(offer.ratePremiumPoints).toFixed(2)} percentage points above the fair upper bound. ` : `Quoted rate is not above the fair upper bound. `}Financed amount: {money(offer.financedAmount)}.</p>}
      <p className="ask">“{negotiationAsk}”</p>
      <ul>{data.negotiationCard.reasons.map((r,i)=><li key={i}>{r}</li>)}</ul>
      <small>Take this as a negotiation aid, not an approval or guaranteed price.</small>
    </section>

    <section className="card"><h2>What we assumed</h2><ul>{data.assumptions.map((a,i)=><li key={i}>{a}</li>)}</ul></section>
    <footer>Borrower Copilot V1 · Stateless · No bureau pull · No borrower data storage</footer>
  </main>
}

function ResultCard({title,value,why,highlight}){
  return <article className={`result-card ${highlight?'highlight':''}`}><small>{title}</small><strong>{value}</strong><p>{why}</p></article>
}
