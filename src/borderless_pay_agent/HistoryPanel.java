package borderless_pay_agent;

public class HistoryPanel {

    static final String HTML = """
<style>
.hs{display:flex;gap:8px;flex-wrap:wrap;margin-bottom:14px}
.hchip{background:#f1f5f9;border-radius:999px;padding:6px 12px;font-size:13px;color:#475569}
.hchip b{color:#0f172a}
.hst{display:flex;height:14px;border-radius:999px;overflow:hidden;background:#e8edf6}
.hst i{display:block;height:100%}
.hlg{display:flex;gap:14px;flex-wrap:wrap;font-size:12px;color:#64748b;margin:8px 0 14px}
.hlg i{display:inline-block;width:9px;height:9px;border-radius:50%;margin-right:5px}
.hpills{display:flex;gap:8px;flex-wrap:wrap;margin-bottom:12px}
.hp{font:inherit;font-size:13px;padding:7px 13px;border-radius:999px;border:1.5px solid #e6ebf4;background:#fff;color:#475569;cursor:pointer}
.hp:hover{border-color:#3b5bff;color:#3b5bff}
.hp.on{background:#3b5bff;border-color:#3b5bff;color:#fff}
.hbar{display:flex;gap:10px;margin-bottom:12px;flex-wrap:wrap}
.hbar input{flex:1;min-width:200px;padding:10px 12px;border:1.5px solid #e6ebf4;border-radius:12px;font:inherit;outline:none}
.hbar input:focus{border-color:#3b5bff}
.hbar button{padding:10px 14px;border:1.5px solid #e6ebf4;border-radius:12px;background:#fff;font:inherit;font-weight:600;cursor:pointer}
.hbar button:hover{border-color:#3b5bff;color:#3b5bff}
.ag-row.row-hold{background-color:#fff5f5 !important}
.ag-row.row-paid{background-color:#f0f7ff !important}
.ag-row.row-sched{background-color:#f7f3ff !important}
.ag-row.ag-row-pinned{font-weight:700}
</style>
<div class="card rise">
  <h3>&#128202; Payment history <small>filter, search, select and export &middot; powered by AG Grid</small></h3>
  <div class="hs" id="hstats"></div>
  <div id="hbars"></div>
  <div class="hpills" id="hpills"></div>
  <div class="hbar">
    <input id="hq" placeholder="Search recipient, decision, route..." oninput="hSearch(this.value)">
    <button onclick="hExport(false)">Export all</button>
    <button onclick="hExport(true)">Export selected</button>
    <button onclick="hReset()">Reset</button>
  </div>
  <div id="hgrid"></div>
</div>
<script src="https://cdn.jsdelivr.net/npm/ag-grid-community/dist/ag-grid-community.min.js"></script>
<script>
(function(){
  var RS=String.fromCharCode(8377), EU=String.fromCharCode(8364), api=null, cur='ALL';
  var ORDER=['ALL','AUTO_APPROVED','NEEDS_APPROVAL','OVER_LIMIT','PAID','SCHEDULED'];
  var LABEL={ALL:'All',AUTO_APPROVED:'Auto-approved',NEEDS_APPROVAL:'Needs approval',OVER_LIMIT:'On hold',PAID:'Paid',SCHEDULED:'Scheduled'};
  var COLOR={AUTO_APPROVED:'#16a34a',NEEDS_APPROVAL:'#f59e0b',OVER_LIMIT:'#ef4444',PAID:'#2563eb',SCHEDULED:'#7c3aed'};
  var STY={
    AUTO_APPROVED:['#dcfce7','#166534'],
    NEEDS_APPROVAL:['#fef3c7','#92400e'],
    OVER_LIMIT:['#fee2e2','#991b1b'],
    PAID:['#dbeafe','#1e40af'],
    SCHEDULED:['#ede9fe','#5b21b6']
  };
  function money(v,sym){return sym+Number(v).toLocaleString('en-IN',{maximumFractionDigits:2});}
  function decision(p){
    if(!p.value)return '';
    var m=STY[p.value]||['#e5e7eb','#374151'];
    return '<span style="background:'+m[0]+';color:'+m[1]+';padding:3px 10px;border-radius:999px;font-size:12px;font-weight:700">'+(LABEL[p.value]||p.value)+'</span>';
  }
  var cols=[
    {field:'time',headerName:'Time',minWidth:120},
    {field:'recipient',headerName:'Recipient',flex:1.3,minWidth:160,pinned:'left',headerCheckboxSelection:false},
    {field:'amount',headerName:'Amount',filter:'agNumberColumnFilter',valueFormatter:function(p){return p.value==null?'':money(p.value,RS);}},
    {field:'status',headerName:'Decision',cellRenderer:decision,minWidth:140},
    {field:'bestRoute',headerName:'Cheapest route',flex:2,minWidth:240},
    {field:'feeInr',headerName:'Fee',filter:'agNumberColumnFilter',valueFormatter:function(p){return p.value==null?'':money(p.value,RS);}},
    {headerName:'Fee %',filter:'agNumberColumnFilter',
      valueGetter:function(p){return p.data&&p.data.amount?p.data.feeInr/p.data.amount*100:null;},
      valueFormatter:function(p){return p.value==null?'':p.value.toFixed(1)+'%';},
      cellStyle:function(p){return p.value>5?{color:'#b45309',fontWeight:'700'}:null;}},
    {field:'friendGetsEur',headerName:'Friend gets',filter:'agNumberColumnFilter',valueFormatter:function(p){return p.value==null?'':money(p.value,EU);}},
    {field:'due',headerName:'Due date',minWidth:110},
    {field:'source',headerName:'Source'}
  ];
  function stats(rows){
    var cnt={},sum={},tot=0;
    rows.forEach(function(r){cnt[r.status]=(cnt[r.status]||0)+1;sum[r.status]=(sum[r.status]||0)+r.amount;tot+=r.amount;});
    document.getElementById('hstats').innerHTML=
      '<span class="hchip">Checks <b>'+rows.length+'</b></span>'+
      '<span class="hchip">Total amount <b>'+money(tot,RS)+'</b></span>'+
      '<span class="hchip">Paid <b>'+money(sum.PAID||0,RS)+'</b></span>'+
      '<span class="hchip">Scheduled <b>'+money(sum.SCHEDULED||0,RS)+'</b></span>';
    var bar='<div class="hst">',lg='<div class="hlg">';
    ORDER.slice(1).forEach(function(k){
      if(!sum[k])return;
      bar+='<i style="width:'+(sum[k]/tot*100)+'%;background:'+COLOR[k]+'"></i>';
      lg+='<span><i style="background:'+COLOR[k]+'"></i>'+LABEL[k]+' '+money(sum[k],RS)+'</span>';
    });
    document.getElementById('hbars').innerHTML='<div class="note" style="font-size:13px;color:#64748b;margin-bottom:6px">Where the money goes, by decision</div>'+bar+'</div>'+lg+'</div>';
    var ph='';
    ORDER.forEach(function(k){
      var n=k==='ALL'?rows.length:(cnt[k]||0);
      ph+='<button class="hp'+(k===cur?' on':'')+'" data-k="'+k+'">'+LABEL[k]+' ('+n+')</button>';
    });
    document.getElementById('hpills').innerHTML=ph;
  }
  function totals(){
    if(!api)return;
    var n=0,a=0,f=0,g=0;
    api.forEachNodeAfterFilter(function(node){
      n++;a+=node.data.amount;f+=node.data.feeInr;g+=node.data.friendGetsEur;
    });
    api.setGridOption('pinnedBottomRowData',[{recipient:'Total ('+n+' rows)',amount:a,feeInr:f,friendGetsEur:g}]);
  }
  function setFilter(k){
    cur=k;
    document.querySelectorAll('#hpills .hp').forEach(function(b){
      b.classList.toggle('on',b.getAttribute('data-k')===k);
    });
    if(api)api.onFilterChanged();
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
          rowSelection:{mode:'multiRow'},
          pagination:true,
          paginationPageSize:5,
          paginationPageSizeSelector:[5,10,25],
          domLayout:'autoHeight',
          animateRows:true,
          isExternalFilterPresent:function(){return cur!=='ALL';},
          doesExternalFilterPass:function(node){return node.data.status===cur;},
          rowClassRules:{
            'row-hold':function(p){return p.data&&p.data.status==='OVER_LIMIT';},
            'row-paid':function(p){return p.data&&p.data.status==='PAID';},
            'row-sched':function(p){return p.data&&p.data.status==='SCHEDULED';}
          },
          onFilterChanged:totals,
          onRowDataUpdated:totals
        });
        totals();
      }else{
        api.setGridOption('rowData',rows);
      }
    });
  }
  window.hSearch=function(v){if(api)api.setGridOption('quickFilterText',v);};
  window.hExport=function(sel){
    if(!api)return;
    if(sel&&api.getSelectedRows().length===0){alert('Tick one or more rows first.');return;}
    api.exportDataAsCsv({fileName:'payment-history.csv',onlySelected:sel});
  };
  window.hReset=function(){
    document.getElementById('hq').value='';
    if(api){api.setGridOption('quickFilterText','');api.setFilterModel(null);api.deselectAll();}
    setFilter('ALL');
  };
  window.hRefresh=load;
  document.getElementById('hpills').addEventListener('click',function(e){
    var b=e.target.closest('button');
    if(b)setFilter(b.getAttribute('data-k'));
  });
  window.addEventListener('load',function(){
    var orig=window.go;
    window.go=async function(){await orig();setTimeout(load,300);};
    load();
  });
})();
</script>
""";
}