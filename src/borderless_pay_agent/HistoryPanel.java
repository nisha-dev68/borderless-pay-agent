package borderless_pay_agent;

public class HistoryPanel {

    static final String HTML = """
<style>
.hs{display:flex;gap:8px;flex-wrap:wrap;margin-bottom:12px}
.hchip{background:#f1f5f9;border-radius:999px;padding:6px 12px;font-size:13px;color:#475569}
.hchip b{color:#0f172a}
.hbar{display:flex;gap:10px;margin-bottom:12px;flex-wrap:wrap}
.hbar input{flex:1;min-width:200px;padding:10px 12px;border:1.5px solid #e6ebf4;border-radius:12px;font:inherit;outline:none}
.hbar input:focus{border-color:#3b5bff}
.hbar button{padding:10px 16px;border:1.5px solid #e6ebf4;border-radius:12px;background:#fff;font:inherit;font-weight:600;cursor:pointer}
.hbar button:hover{border-color:#3b5bff;color:#3b5bff}
</style>
<div class="card rise">
  <h3>&#128202; Payment history <small>sort, filter, search and export &middot; powered by AG Grid</small></h3>
  <div class="hs" id="hstats"></div>
  <div class="hbar">
    <input id="hq" placeholder="Search recipient, decision, route..." oninput="hSearch(this.value)">
    <button onclick="hExport()">Export CSV</button>
  </div>
  <div id="hgrid"></div>
</div>
<script src="https://cdn.jsdelivr.net/npm/ag-grid-community/dist/ag-grid-community.min.js"></script>
<script>
(function(){
  var RS=String.fromCharCode(8377), EU=String.fromCharCode(8364), api=null;
  var STY={
    AUTO_APPROVED:['#dcfce7','#166534','Auto-approved'],
    NEEDS_APPROVAL:['#fef3c7','#92400e','Needs approval'],
    OVER_LIMIT:['#fee2e2','#991b1b','On hold'],
    PAID:['#dbeafe','#1e40af','Paid(sandbox)']
  };
  function money(v,sym){return sym+Number(v).toLocaleString('en-IN',{maximumFractionDigits:2});}
  function decision(p){
    var m=STY[p.value]||['#e5e7eb','#374151',p.value];
    return '<span style="background:'+m[0]+';color:'+m[1]+';padding:3px 10px;border-radius:999px;font-size:12px;font-weight:700">'+m[2]+'</span>';
  }
  var cols=[
    {field:'time',headerName:'Time',minWidth:130},
    {field:'recipient',headerName:'Recipient',flex:1.3,minWidth:150},
    {field:'amount',headerName:'Amount',filter:'agNumberColumnFilter',valueFormatter:function(p){return money(p.value,RS);}},
    {field:'status',headerName:'Decision',cellRenderer:decision,minWidth:140},
    {field:'bestRoute',headerName:'Cheapest route',flex:2,minWidth:230},
    {field:'feeInr',headerName:'Fee',filter:'agNumberColumnFilter',valueFormatter:function(p){return money(p.value,RS);}},
    {field:'friendGetsEur',headerName:'Friend gets',filter:'agNumberColumnFilter',valueFormatter:function(p){return money(p.value,EU);}},
    {field:'source',headerName:'Source'}
  ];
  function stats(rows){
    var a=0,h=0,n=0;
    rows.forEach(function(r){
      if(r.status==='AUTO_APPROVED')a++;
      else if(r.status==='OVER_LIMIT')h++;
      else n++;
    });
    document.getElementById('hstats').innerHTML=
      '<span class="hchip">Checks <b>'+rows.length+'</b></span>'+
      '<span class="hchip">Auto-approved <b>'+a+'</b></span>'+
      '<span class="hchip">Needs approval <b>'+n+'</b></span>'+
      '<span class="hchip">On hold <b>'+h+'</b></span>';
  }
  function load(){
    if(typeof agGrid==='undefined'){
      document.getElementById('hgrid').innerHTML='<p>The table could not load. Check your internet connection.</p>';
      return;
    }
    fetch('/api/history').then(function(r){return r.json();}).then(function(rows){
      stats(rows);
      if(!api){
        api=agGrid.createGrid(document.getElementById('hgrid'),{
          columnDefs:cols,
          rowData:rows,
          defaultColDef:{sortable:true,filter:true,resizable:true,minWidth:90},
          pagination:true,
          paginationPageSize:5,
          paginationPageSizeSelector:[5,10,25],
          domLayout:'autoHeight',
          animateRows:true
        });
      }else{
        api.setGridOption('rowData',rows);
      }
    });
  }
  window.hSearch=function(v){if(api)api.setGridOption('quickFilterText',v);};
  window.hExport=function(){if(api)api.exportDataAsCsv({fileName:'payment-history.csv'});};
  window.addEventListener('load',function(){
    var orig=window.go;
    window.go=async function(){await orig();setTimeout(load,300);};
    load();
  });
})();
</script>
""";
}