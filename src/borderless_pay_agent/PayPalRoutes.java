package borderless_pay_agent;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PayPalRoutes {

    record Pending(String recipient, double amountInr, double usd) {
    }

    private static final Map<String, Pending> PENDING = new ConcurrentHashMap<>();
    private static final String JSON = "application/json; charset=utf-8";

    public static void register(HttpServer server) {
        server.createContext("/api/order", PayPalRoutes::order);
        server.createContext("/api/schedule", PayPalRoutes::schedule);
        server.createContext("/paypal/return", PayPalRoutes::back);
        server.createContext("/paypal/cancel", ex -> page(ex, false, "Payment cancelled",
                "You cancelled on the PayPal page, so nothing was paid.", ""));
    }

    static String baseUrl() {
        String b = System.getenv("BASE_URL");
        if (b != null && !b.isBlank()) {
            return b;
        }
        return "http://localhost:" + System.getenv().getOrDefault("PORT", "8095");
    }
    
 // The dashboard sends: amountInr|recipient
    static void schedule(HttpExchange ex) throws IOException {
        if (!"POST".equals(ex.getRequestMethod())) {
            WebServer.send(ex, 405, JSON, "{\"error\":\"Use POST\"}");
            return;
        }
        if (!RateLimiter.allow(ex)) {
            WebServer.send(ex, 429, JSON,
                    "{\"error\":\"Too many requests. Please wait a minute and try again.\"}");
            return;
        }
        try {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String[] p = body.split("\\|", 2);
            double amountInr = Double.parseDouble(p[0].trim());
            String recipient = p.length > 1 ? p[1].trim() : "Recipient";
            if (amountInr <= 0 || amountInr > 50000) {
                throw new IllegalArgumentException("Amount is out of range");
            }
            if (recipient.length() > 60) {
                recipient = recipient.substring(0, 60);
            }
            String due = PaymentHistory.addScheduled(recipient, amountInr);
            WebServer.send(ex, 200, JSON, "{\"ok\":true,\"due\":" + WebServer.q(due) + "}");
        } catch (Exception e) {
            WebServer.send(ex, 500, JSON, "{\"error\":" + WebServer.q(String.valueOf(e.getMessage())) + "}");
        }
    }

    // The dashboard sends: amountInr|recipient
    static void order(HttpExchange ex) throws IOException {
        if (!"POST".equals(ex.getRequestMethod())) {
            WebServer.send(ex, 405, JSON, "{\"error\":\"Use POST\"}");
            return;
        }
        if (!RateLimiter.allow(ex)) {
            WebServer.send(ex, 429, JSON,
                    "{\"error\":\"Too many requests. Please wait a minute and try again.\"}");
            return;
        }
        try {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String[] p = body.split("\\|", 2);
            double amountInr = Double.parseDouble(p[0].trim());
            String recipient = p.length > 1 ? p[1].trim() : "Recipient";
            if (recipient.length() > 60) {
                recipient = recipient.substring(0, 60);
            }
            if (amountInr <= 0 || amountInr > 50000) {
                throw new IllegalArgumentException("Amount is out of range");
            }
            double usd = Math.max(amountInr * ExchangeRates.inrToUsd(), 0.01);
            String[] order = PayPalService.createOrder(usd, "Payment to " + recipient, baseUrl());
            PENDING.put(order[0], new Pending(recipient, amountInr, usd));
            WebServer.send(ex, 200, JSON, "{\"orderId\":" + WebServer.q(order[0])
                    + ",\"approveUrl\":" + WebServer.q(order[1])
                    + ",\"usd\":" + WebServer.num(usd) + "}");
        } catch (Exception e) {
            WebServer.send(ex, 500, JSON, "{\"error\":" + WebServer.q(String.valueOf(e.getMessage())) + "}");
        }
    }

    // PayPal sends the buyer back here after approval: /paypal/return?token=ORDERID
    static void back(HttpExchange ex) throws IOException {
        String query = ex.getRequestURI().getQuery();
        Matcher m = Pattern.compile("(?:^|&)token=([A-Za-z0-9]+)").matcher(query == null ? "" : query);
        if (!m.find()) {
            page(ex, false, "Missing order", "PayPal did not send an order number.", "");
            return;
        }
        String orderId = m.group(1);
        Pending pending = PENDING.remove(orderId);
        try {
            String[] r = PayPalService.capture(orderId);
            if (!"COMPLETED".equals(r[0])) {
                page(ex, false, "Payment not completed", "PayPal status: " + esc(r[0])
                        + ". Please go back and try again.", "<table>" + row("Order ID", orderId) + "</table>");
                return;
            }
            double feeUsd = Double.parseDouble(r[1]);
            StringBuilder rows = new StringBuilder("<table>").append(row("Order ID", orderId));
            if (pending != null) {
                double feeInr = pending.usd() > 0 ? feeUsd * pending.amountInr() / pending.usd() : 0;
                PaymentHistory.addPaid(pending.recipient(), pending.amountInr(), orderId, feeInr);
                rows.append(row("Paid to", pending.recipient()))
                    .append(row("Amount", String.format(Locale.US, "Rs %.2f", pending.amountInr())))
                    .append(row("Charged in USD", String.format(Locale.US, "$%.2f", pending.usd())));
            }
            rows.append(row("PayPal fee", String.format(Locale.US, "$%.2f", feeUsd))).append("</table>");
            page(ex, true, "Payment completed",
                    "Your sandbox payment was approved and captured by the agent.", rows.toString());
        } catch (Exception e) {
            page(ex, false, "Something went wrong", esc(String.valueOf(e.getMessage())), "");
        }
    }

    static String row(String k, String v) {
        return "<tr><td>" + k + "</td><td>" + esc(v) + "</td></tr>";
    }

    static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    static void page(HttpExchange ex, boolean ok, String title, String text, String rows)
            throws IOException {
        String html = PAGE.replace("@@COLOR@@", ok ? "#16a34a" : "#ef4444")
                .replace("@@ICON@@", ok ? "&#10003;" : "&#10005;")
                .replace("@@TITLE@@", title)
                .replace("@@TEXT@@", text)
                .replace("@@ROWS@@", rows);
        WebServer.send(ex, 200, "text/html; charset=utf-8", html);
    }

    static final String PAGE = """
<!DOCTYPE html>
<html lang="en"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Borderless Pay Agent</title>
<style>
body{margin:0;font-family:'Segoe UI',Arial,sans-serif;background:#f4f7fc;color:#0f172a;display:grid;place-items:center;min-height:100vh;padding:16px}
.box{background:#fff;border-radius:22px;max-width:460px;width:100%;padding:30px;text-align:center;box-shadow:0 24px 60px rgba(15,23,42,.14)}
.ic{width:70px;height:70px;border-radius:50%;margin:0 auto 14px;display:grid;place-items:center;font-size:36px;color:#fff;background:@@COLOR@@}
h1{margin:0 0 8px;font-size:24px}
p{color:#64748b;line-height:1.6}
table{width:100%;margin:16px 0;border-collapse:collapse;text-align:left;font-size:14px}
td{padding:8px 4px;border-bottom:1px solid #e6ebf4}
td:last-child{text-align:right;font-weight:600}
a{display:inline-block;margin-top:8px;padding:13px 22px;border-radius:12px;color:#fff;text-decoration:none;font-weight:700;background:linear-gradient(135deg,#3b5bff,#12c2a9)}
</style></head><body><div class="box">
<div class="ic">@@ICON@@</div><h1>@@TITLE@@</h1><p>@@TEXT@@</p>@@ROWS@@
<a href="/">Back to dashboard</a>
<p style="font-size:12px">Sandbox demo: no real money moved.</p>
</div></body></html>
""";
}