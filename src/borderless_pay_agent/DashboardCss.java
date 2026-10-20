package borderless_pay_agent;

public class DashboardCss {

    static final String CSS = """
<!DOCTYPE html>
<html lang="en"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Borderless Pay Agent</title>
<style>
:root{--ink:#0f172a;--mute:#64748b;--line:#e6ebf4;--bg:#f4f7fc;--blue:#3b5bff;--teal:#12c2a9;--ok:#16a34a;--warn:#f59e0b;--bad:#ef4444;--r:20px}
*{box-sizing:border-box}
body{margin:0;font-family:'Segoe UI',Arial,sans-serif;background:var(--bg);color:var(--ink)}
.wrap{max-width:900px;margin:0 auto;padding:0 18px}
.hero{position:relative;overflow:hidden;color:#fff;padding:22px 0 110px;background:#070d24}
.blob{position:absolute;border-radius:50%;filter:blur(70px);opacity:.55;animation:float 12s ease-in-out infinite}
.b1{width:420px;height:420px;background:#3b5bff;top:-140px;left:-80px}
.b2{width:360px;height:360px;background:#12c2a9;top:40px;right:-100px;animation-delay:-4s}
.b3{width:300px;height:300px;background:#a855f7;bottom:-160px;left:35%;animation-delay:-8s}
@keyframes float{50%{transform:translate(40px,30px) scale(1.1)}}
.hero .wrap{position:relative;z-index:1}
.nav{display:flex;justify-content:space-between;align-items:center;gap:10px;flex-wrap:wrap}
.logo{display:flex;align-items:center;gap:10px;font-weight:700}
.mark{width:40px;height:40px;border-radius:12px;display:grid;place-items:center;background:linear-gradient(135deg,var(--blue),var(--teal));font-weight:800;box-shadow:0 6px 18px rgba(59,91,255,.45)}
.pill{background:rgba(255,255,255,.12);border:1px solid rgba(255,255,255,.2);padding:6px 14px;border-radius:999px;font-size:12px;backdrop-filter:blur(6px)}
h1{font-size:clamp(30px,6vw,46px);line-height:1.1;margin:34px 0 14px;font-weight:800;letter-spacing:-1px}
h1 span{background:linear-gradient(90deg,#7dd3fc,#5eead4);-webkit-background-clip:text;background-clip:text;color:transparent}
.sub{max-width:560px;opacity:.85;line-height:1.65;margin:0}
.steps{display:flex;gap:10px;flex-wrap:wrap;margin-top:26px}
.st{display:flex;align-items:center;gap:8px;background:rgba(255,255,255,.09);border:1px solid rgba(255,255,255,.16);padding:8px 14px;border-radius:14px;font-size:13px}
.st b{width:22px;height:22px;border-radius:50%;background:linear-gradient(135deg,var(--blue),var(--teal));display:grid;place-items:center;font-size:12px}
.panel{background:#fff;border-radius:24px;padding:18px;margin-top:-68px;position:relative;z-index:2;box-shadow:0 24px 60px rgba(15,23,42,.16)}
.panel label{font-size:12px;font-weight:700;color:var(--mute);letter-spacing:.8px}
.row{display:flex;gap:10px;flex-wrap:wrap;margin-top:8px}
.row input{flex:1;min-width:220px;padding:16px;font:inherit;font-size:16px;border:1.5px solid var(--line);border-radius:14px;outline:none;transition:.2s}
.row input:focus{border-color:var(--blue);box-shadow:0 0 0 4px rgba(59,91,255,.12)}
.go{padding:16px 26px;font:inherit;font-weight:700;font-size:16px;color:#fff;border:0;border-radius:14px;cursor:pointer;background:linear-gradient(135deg,var(--blue),var(--teal));box-shadow:0 10px 24px rgba(59,91,255,.35);transition:.2s}
.go:hover{transform:translateY(-2px)}
.go:disabled{opacity:.65;cursor:wait;transform:none}
.chips{display:flex;gap:8px;flex-wrap:wrap;margin-top:14px}
.chips button{font:inherit;font-size:13px;border:1px solid var(--line);background:#f8faff;color:#475569;padding:8px 14px;border-radius:999px;cursor:pointer;transition:.2s}
.chips button:hover{border-color:var(--blue);color:var(--blue);background:#eef2ff}
.card{background:#fff;border:1px solid var(--line);border-radius:var(--r);padding:22px;margin-top:18px;box-shadow:0 8px 24px rgba(15,23,42,.05)}
.card h3{margin:0 0 14px;font-size:17px;display:flex;align-items:center;gap:8px}
.card h3 small{font-weight:500;color:var(--mute);font-size:12px}
.rise{animation:rise .55s cubic-bezier(.2,.8,.2,1) both}
@keyframes rise{from{opacity:0;transform:translateY(18px)}to{opacity:1;transform:none}}
.res{display:grid;grid-template-columns:1fr 190px;gap:18px;align-items:center;border-top:5px solid var(--mute)}
.res.ok{border-top-color:var(--ok)}.res.warn{border-top-color:var(--warn)}.res.bad{border-top-color:var(--bad)}
@media(max-width:640px){.res{grid-template-columns:1fr}}
.badge{display:inline-flex;align-items:center;gap:6px;padding:7px 14px;border-radius:999px;font-size:13px;font-weight:700;color:#fff}
.badge.ok{background:var(--ok)}.badge.warn{background:var(--warn)}.badge.bad{background:var(--bad)}
.amt{font-size:44px;font-weight:800;letter-spacing:-1.5px;margin:14px 0 2px}
.to{color:var(--mute);font-size:15px}
.ai{margin-top:16px;padding:14px 16px;border-radius:14px;background:linear-gradient(135deg,#eef2ff,#ecfeff);border:1px solid #dbe4ff}
.ai b{font-size:12px;letter-spacing:.8px;color:var(--blue)}
.ai p{margin:6px 0 0;line-height:1.6;font-size:15px}
.gauge{text-align:center}
.gauge svg{width:170px;height:170px}
.gc{font-size:13px;margin-top:2px}
.lg{display:flex;justify-content:center;gap:12px;font-size:12px;color:var(--mute);margin-top:8px}
.lg i{display:inline-block;width:9px;height:9px;border-radius:50%;margin-right:5px}
.opts{display:grid;grid-template-columns:repeat(auto-fit,minmax(230px,1fr));gap:12px}
.opt{display:flex;gap:12px;text-align:left;padding:14px;border:1.5px solid var(--line);border-radius:16px;background:#fff;cursor:pointer;font:inherit;transition:.2s}
.opt:hover{border-color:var(--blue);transform:translateY(-3px);box-shadow:0 12px 24px rgba(59,91,255,.12)}
.oi{flex:none;width:42px;height:42px;border-radius:12px;background:#eef2ff;color:var(--blue);display:grid;place-items:center;font-size:20px}
.opt b{display:block;font-size:14px;margin-bottom:2px}
.opt span{font-size:12.5px;color:var(--mute);line-height:1.4}
.fee{padding:16px;border:1.5px solid var(--line);border-radius:16px;margin-bottom:12px}
.fee.best{border-color:var(--ok);background:linear-gradient(135deg,#f0fdf4,#ecfdf5)}
.fh{display:flex;justify-content:space-between;align-items:center;gap:8px;font-weight:600;font-size:14px}
.tag{background:var(--ok);color:#fff;font-size:11px;font-weight:800;padding:4px 10px;border-radius:999px}
.fb{height:10px;background:#e8edf6;border-radius:999px;margin:12px 0 10px;overflow:hidden}
.fb i{display:block;height:100%;width:0;border-radius:999px;background:linear-gradient(90deg,var(--blue),var(--teal));transition:width 1s cubic-bezier(.2,.8,.2,1)}
.fv{display:flex;justify-content:space-between;flex-wrap:wrap;gap:6px;font-size:13px;color:var(--mute)}
.fv strong{color:var(--ink);font-size:15px}
.save{background:#ecfdf5;color:#047857;padding:10px 14px;border-radius:12px;font-size:14px;font-weight:600;margin:0}
.tl{margin:0;padding:0;list-style:none}
.tl li{display:flex;gap:14px;padding-bottom:18px;position:relative}
.tl li:not(:last-child):before{content:'';position:absolute;left:15px;top:32px;bottom:2px;width:2px;background:var(--line)}
.dot{flex:none;width:32px;height:32px;border-radius:50%;display:grid;place-items:center;background:linear-gradient(135deg,var(--blue),var(--teal));color:#fff;font-size:14px;font-weight:700}
.tl b{display:block;font-size:14px}
.tl span{font-size:13px;color:var(--mute);line-height:1.5}
.load{text-align:center}
.ls{display:flex;flex-direction:column;gap:12px;max-width:340px;margin:14px auto 4px;text-align:left}
.ls div{display:flex;align-items:center;gap:10px;font-size:14px;color:#94a3b8;transition:.3s}
.ls div.on{color:var(--ink);font-weight:600}
.ls div.done{color:var(--ok)}
.sp{width:18px;height:18px;border-radius:50%;border:3px solid #dbe4ff;flex:none}
.on .sp{border-top-color:var(--blue);animation:spin .8s linear infinite}
.done .sp{background:var(--ok);border-color:var(--ok)}
@keyframes spin{to{transform:rotate(360deg)}}
.trust{display:grid;grid-template-columns:repeat(auto-fit,minmax(240px,1fr));gap:14px;margin-top:26px}
.tc{background:#fff;border:1px solid var(--line);border-radius:18px;padding:18px}
.tc em{font-style:normal;font-size:26px}
.tc b{display:block;margin:8px 0 4px}
.tc span{font-size:13.5px;color:var(--mute);line-height:1.55}
.foot{text-align:center;color:var(--mute);font-size:12px;margin:26px 0 34px;line-height:1.7}
.ov{position:fixed;inset:0;background:rgba(7,13,36,.55);backdrop-filter:blur(4px);display:none;align-items:center;justify-content:center;padding:16px;z-index:20}
.ov.show{display:flex}
.md{background:#fff;border-radius:22px;max-width:440px;width:100%;padding:24px;animation:rise .35s both}
.md h4{margin:0 0 6px;font-size:19px}
.md p{color:var(--mute);line-height:1.6;font-size:14px}
.md input[type=range]{width:100%}
.mb{display:flex;gap:10px;margin-top:18px}
.mb button{flex:1;padding:13px;border-radius:12px;font:inherit;font-weight:700;cursor:pointer;border:1.5px solid var(--line);background:#fff}
.mb .pri{border:0;color:#fff;background:linear-gradient(135deg,var(--blue),var(--teal))}
.tm{display:flex;gap:10px;margin:14px 0}
.tm div{flex:1;border:1.5px dashed var(--line);border-radius:12px;padding:12px;text-align:center;font-size:13px;color:var(--mute)}
.tm strong{display:block;font-size:18px;color:var(--ink)}
.okc{width:64px;height:64px;border-radius:50%;background:var(--ok);color:#fff;font-size:34px;display:grid;place-items:center;margin:6px auto 10px;animation:pop .5s both}
@keyframes pop{0%{transform:scale(0)}70%{transform:scale(1.15)}100%{transform:scale(1)}}
</style></head>
""";
}