package borderless_pay_agent;

public class DashboardBody {

    static final String HTML = """
<body>
<div class="hero">
  <div class="blob b1"></div><div class="blob b2"></div><div class="blob b3"></div>
  <div class="wrap">
    <div class="nav"><div class="logo"><span class="mark">B</span> Borderless Pay Agent</div><span class="pill">&#128274; Sandbox demo &middot; no real money</span></div>
    <h1>Send money abroad,<br><span>with rules you control.</span></h1>
    <p class="sub">Tell the agent what you want to pay in plain words. It checks your spending rules, finds the cheapest route, and explains every decision.</p>
    <div class="steps">
      <div class="st"><b>1</b> Ask in plain words</div>
      <div class="st"><b>2</b> Rules check</div>
      <div class="st"><b>3</b> Cheapest route</div>
      <div class="st"><b>4</b> Clear explanation</div>
    </div>
  </div>
</div>

<div class="wrap">
  <div class="panel">
    <label>WHAT DO YOU WANT TO PAY?</label>
    <div class="row">
      <input id="msg" value="Pay 9000 rupees tuition fee to my university abroad" onkeydown="if(event.key==='Enter')go()">
      <button id="btn" class="go" onclick="go()">Check payment</button>
    </div>
    <div class="chips">
      <button onclick="chip(this.textContent)">Send 1500 rupees to my friend in Germany</button>
      <button onclick="chip(this.textContent)">Pay 4000 rupees to my brother in Dubai</button>
      <button onclick="chip(this.textContent)">Pay 9000 rupees tuition fee to my university abroad</button>
    </div>
  </div>

  <div id="out"></div>

  <div class="trust">
    <div class="tc"><em>&#129302;</em><b>AI explains, never pays</b><span>Gemini reads your sentence and writes the reasoning in simple words. It cannot move money.</span></div>
    <div class="tc"><em>&#128737;</em><b>Rules decide</b><span>Your limits are plain code that can be read and tested. Over the limit? Nothing is paid until you choose.</span></div>
    <div class="tc"><em>&#128177;</em><b>Cheapest route</b><span>Routes are compared by fee and exchange-rate markup, using a live rate. Fees shown are assumptions.</span></div>
  </div>
  <div class="foot">Demo limits: auto-approve up to &#8377;2,000 &middot; monthly limit &#8377;20,000 &middot; already spent &#8377;15,000<br>
  Built for the PayPal AI Hackathon &middot; Java backend &middot; Google Gemini &middot; PayPal Sandbox</div>
</div>

<div class="ov" id="ov" onclick="if(event.target===this)closeM()"><div class="md" id="md"></div></div>
""";
}