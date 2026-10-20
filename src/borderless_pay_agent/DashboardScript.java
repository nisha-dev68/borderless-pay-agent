package borderless_pay_agent;

public class DashboardScript {

    static final String JS = """
<script>
const LIMIT=20000, SPENT=15000, AUTO=2000;
let DATA=null, CUR='', timer=null;
const $=id=>document.getElementById(id);
const fmt=n=>Number(n).toLocaleString('en-IN',{maximumFractionDigits:2});
const esc=s=>String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
const ST={
  AUTO_APPROVED:{cls:'ok',ic:'&#10003;',label:'Auto-approved',note:'Within your limits, so no action is needed.'},
  NEEDS_APPROVAL:{cls:'warn',ic:'!',label:'Needs your approval',note:'Above your auto-approve limit of &#8377;'+fmt(AUTO)+'.'},
  OVER_LIMIT:{cls:'bad',ic:'&#9888;',label:'On hold: over monthly limit',note:'Nothing is paid until you choose an option.'}
};
function kind(o){
  if(o.startsWith('Approve'))return 'approve';
  if(o.startsWith('Raise'))return 'raise';
  if(o.startsWith('Split'))return 'split';
  if(o.startsWith('Schedule'))return 'schedule';
  if(o.startsWith('Cancel'))return 'cancel';
  return 'pay';
}
const OI={approve:'&#10003;',raise:'&#8679;',split:'&#9986;',schedule:'&#128197;',cancel:'&#10005;',pay:'&#10148;'};
const OT={approve:'Approve this payment',raise:'Raise monthly limit',split:'Split the payment',schedule:'Schedule for next month',cancel:'Cancel',pay:'Pay now'};
const OD={approve:'One-time OK. Your limit stays the same.',raise:'Increase your limit and re-check.',schedule:'Pay the full amount next month.',cancel:'Do not send anything.',pay:'Send it now (sandbox).'};
const LS=['Reading your request','Checking your spending rules','Comparing fees and rates','Writing the explanation'];

function chip(t){$('msg').value=t;go();}

function loading(){
  let h='<div class="card load rise"><b>The agent is working</b><div class="ls">';
  LS.forEach((t,i)=>{h+='<div id="ls'+i+'"><i class="sp"></i>'+t+'</div>';});
  h+='</div></div>';
  $('out').innerHTML=h;
  let i=0;
  const step=()=>{
    for(let k=0;k<LS.length;k++){const e=$('ls'+k);if(!e)return;e.className=k<i?'done':(k===i?'on':'');}
    if(i<LS.length-1)i++;
  };
  step();
  timer=setInterval(step,900);
}

function countUp(el,to){
  const t0=performance.now(), d=900;
  function f(t){const p=Math.min((t-t0)/d,1), e=1-Math.pow(1-p,3);el.textContent=fmt(Math.round(to*e));if(p<1)requestAnimationFrame(f);}
  requestAnimationFrame(f);
}

function gauge(spentPct,reqPct,cls,remaining){
  const C=2*Math.PI*60;
  const col={ok:'#16a34a',warn:'#f59e0b',bad:'#ef4444'}[cls];
  return '<svg viewBox="0 0 150 150"><circle cx="75" cy="75" r="60" fill="none" stroke="#e8edf6" stroke-width="16"/>'
   +'<circle cx="75" cy="75" r="60" fill="none" stroke="#94a3b8" stroke-width="16" stroke-dasharray="'+(C*spentPct/100)+' '+C+'" transform="rotate(-90 75 75)"/>'
   +'<circle cx="75" cy="75" r="60" fill="none" stroke="'+col+'" stroke-width="16" stroke-dasharray="'+(C*reqPct/100)+' '+C+'" stroke-dashoffset="-'+(C*spentPct/100)+'" transform="rotate(-90 75 75)"/>'
   +'<text x="75" y="70" text-anchor="middle" font-size="12" fill="#64748b">Left this month</text>'
   +'<text x="75" y="92" text-anchor="middle" font-size="20" font-weight="800" fill="#0f172a">&#8377;'+fmt(remaining)+'</text></svg>';
}

function render(d){
  DATA=d;
  const s=ST[d.status]||ST.NEEDS_APPROVAL;
  const remaining=Math.max(LIMIT-SPENT,0);
  const over=Math.max(d.amount-remaining,0);
  const spentPct=SPENT/LIMIT*100;
  const reqPct=Math.min(d.amount/LIMIT*100,100-spentPct);
  const best=d.fees[0], worst=d.fees[d.fees.length-1];
  const saving=worst.cost-best.cost;

  let h='<div class="card res '+s.cls+' rise"><div>';
  h+='<span class="badge '+s.cls+'">'+s.ic+' '+s.label+'</span>';
  h+='<div class="amt">&#8377;<span id="amt">0</span></div>';
  h+='<div class="to">to '+esc(d.recipient)+'</div>';
  h+='<div class="to" style="margin-top:4px">'+s.note+'</div>';
  h+='<div class="ai"><b>&#10024; AI EXPLANATION</b><p>'+esc(d.explanation)+'</p></div></div>';
  h+='<div class="gauge">'+gauge(spentPct,reqPct,s.cls,remaining);
  h+='<div class="gc">'+(over>0?'<b style="color:var(--bad)">Over by &#8377;'+fmt(over)+'</b>':'<b style="color:var(--ok)">Fits your limit</b>')+'</div>';
  h+='<div class="lg"><span><i style="background:#94a3b8"></i>Spent</span><span><i style="background:'+({ok:'#16a34a',warn:'#f59e0b',bad:'#ef4444'}[s.cls])+'"></i>This payment</span></div></div></div>';

  const options=d.options.length?d.options:['Pay now'];
  h+='<div class="card rise" style="animation-delay:.12s"><h3>Your choices <small>tap one to continue</small></h3><div class="opts">';
  options.forEach((o,i)=>{
    const k=kind(o);
    const sub=k==='split'?o.replace('Split: ',''):OD[k];
    h+='<button class="opt" onclick="openOpt('+i+')"><div class="oi">'+OI[k]+'</div><div><b>'+OT[k]+'</b><span>'+esc(sub)+'</span></div></button>';
  });
  h+='</div></div>';

  h+='<div class="card rise" style="animation-delay:.24s"><h3>Cheapest way to pay <small>assumed fees &middot; cheapest first</small></h3>';
  d.fees.forEach((f,i)=>{
    h+='<div class="fee'+(i===0?' best':'')+'"><div class="fh"><span>'+esc(f.name)+'</span>'+(i===0?'<span class="tag">BEST</span>':'')+'</div>';
    h+='<div class="fb"><i data-w="'+(f.cost/worst.cost*100)+'"></i></div>';
    h+='<div class="fv"><span>Cost <strong>&#8377;'+fmt(f.cost)+'</strong></span><span>Friend gets <strong>&euro;'+fmt(f.gets)+'</strong></span></div></div>';
  });
  h+='<p class="save">&#128161; The best route saves &#8377;'+fmt(saving)+' compared with the costliest route.</p></div>';

  const steps=[
    ['Understood your request','AI read your sentence: &#8377;'+fmt(d.amount)+' to '+esc(d.recipient)+'.'],
    ['Checked your rules','Auto-approve up to &#8377;'+fmt(AUTO)+'. Left this month: &#8377;'+fmt(remaining)+'. Result: '+s.label+'.'],
    ['Compared '+d.fees.length+' payment routes','Cheapest: '+esc(best.name)+'.'],
    ['Explained the decision','AI wrote the reasoning above. It never moves money by itself.']
  ];
  h+='<div class="card rise" style="animation-delay:.36s"><h3>How the agent decided</h3><ul class="tl">';
  steps.forEach((x,i)=>{h+='<li><div class="dot">'+(i+1)+'</div><div><b>'+x[0]+'</b><span>'+x[1]+'</span></div></li>';});
  h+='</ul></div>';
  return h;
}

async function go(){
  const btn=$('btn');
  btn.disabled=true; btn.textContent='Checking...';
  loading();
  try{
    const r=await fetch('/api/pay',{method:'POST',body:$('msg').value});
    const d=await r.json();
    clearInterval(timer);
    if(d.error){$('out').innerHTML='<div class="card" style="color:var(--bad)">Something went wrong: '+esc(d.error)+'</div>';return;}
    $('out').innerHTML=render(d);
    countUp($('amt'),d.amount);
    setTimeout(()=>document.querySelectorAll('.fb i').forEach(e=>{e.style.width=e.dataset.w+'%';}),120);
  }catch(e){
    clearInterval(timer);
    $('out').innerHTML='<div class="card" style="color:var(--bad)">Something went wrong: '+esc(e)+'</div>';
  }finally{
    btn.disabled=false; btn.textContent='Check payment';
  }
}

function openOpt(i){
  const o=DATA.options.length?DATA.options[i]:'Pay now';
  const k=kind(o), remaining=Math.max(LIMIT-SPENT,0), amt=DATA.amount;
  CUR=k;
  let body='';
  if(k==='split'){
    body='<p>Your remaining limit covers part of this payment.</p><div class="tm"><div>Today<strong>&#8377;'+fmt(remaining)+'</strong></div><div>Next month<strong>&#8377;'+fmt(Math.max(amt-remaining,0))+'</strong></div></div>';
  }else if(k==='raise'){
    const start=Math.max(LIMIT,Math.ceil((SPENT+amt)/1000)*1000);
    body='<p>Drag to choose a new monthly limit.</p><input id="rng" type="range" min="'+LIMIT+'" max="60000" step="1000" value="'+start+'" oninput="upd()"><p id="rtxt"></p>';
  }else if(k==='schedule'){
    body='<p>The full &#8377;'+fmt(amt)+' is scheduled for next month, when your limit resets.</p>';
  }else if(k==='approve'){
    body='<p>You approve &#8377;'+fmt(amt)+' to '+esc(DATA.recipient)+' one time. Your monthly limit stays &#8377;'+fmt(LIMIT)+'.</p>';
  }else if(k==='cancel'){
    body='<p>Nothing will be sent.</p>';
  }else{
    body='<p>Send &#8377;'+fmt(amt)+' now.</p>';
  }
  $('md').innerHTML='<h4>'+OT[k]+'</h4>'+body+'<p style="font-size:12px">Sandbox demo: no real money moves.</p><div class="mb"><button onclick="closeM()">Back</button><button class="pri" onclick="confirmM()">Confirm</button></div>';
  $('ov').classList.add('show');
  if(k==='raise')upd();
}

function upd(){
  const v=Number($('rng').value), left=v-SPENT-DATA.amount;
  $('rtxt').innerHTML='New limit <b>&#8377;'+fmt(v)+'</b>. '+(left>=0?'This payment fits, and &#8377;'+fmt(left)+' would be left.':'Still over by &#8377;'+fmt(-left)+'.');
}

async function confirmM(){
  if(CUR==='schedule'||CUR==='cancel'){
    const m=CUR==='schedule'?'Scheduled for next month.':'Cancelled.';
    $('md').innerHTML='<div class="okc">&#10003;</div><h4 style="text-align:center">Done</h4><p style="text-align:center">'+m+' This is a demo, so nothing was sent.</p><div class="mb"><button class="pri" onclick="closeM()">Close</button></div>';
    return;
  }
  const amt=CUR==='split'?Math.max(LIMIT-SPENT,0):DATA.amount;
  $('md').innerHTML='<p style="text-align:center"><b>Creating your PayPal sandbox order...</b></p>';
  try{
    const r=await fetch('/api/order',{method:'POST',body:amt+'|'+DATA.recipient});
    const d=await r.json();
    if(d.error){
      $('md').innerHTML='<h4>Something went wrong</h4><p>'+esc(d.error)+'</p><div class="mb"><button onclick="closeM()">Close</button></div>';
      return;
    }
    window.location.href=d.approveUrl;
  }catch(e){
    $('md').innerHTML='<h4>Something went wrong</h4><p>'+esc(e)+'</p><div class="mb"><button onclick="closeM()">Close</button></div>';
  }
}
function closeM(){$('ov').classList.remove('show');}
</script></body></html>
""";
}