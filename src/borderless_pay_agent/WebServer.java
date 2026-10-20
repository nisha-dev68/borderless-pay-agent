package borderless_pay_agent;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

public class WebServer {

    static final SpendingRules RULES = new SpendingRules(2000, 20000, 15000);
    static final PaymentAgent AGENT = new PaymentAgent(RULES);
    static final FeeComparator FEES = new FeeComparator();
    static final String PAGE = DashboardPage.HTML.replace("<div class=\"trust\">",
            HistoryPanel.HTML + "<div class=\"trust\">");

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8095"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", ex -> send(ex, 200, "text/html; charset=utf-8", PAGE));
        server.createContext("/api/history", ex -> send(ex, 200,
                "application/json; charset=utf-8", PaymentHistory.toJson()));
        server.createContext("/api/pay", WebServer::pay);
        PayPalRoutes.register(server);
        server.start();
        System.out.println("Open http://localhost:" + port);
    }

    static void pay(HttpExchange ex) throws IOException {
        if (!"POST".equals(ex.getRequestMethod())) {
            send(ex, 405, "application/json; charset=utf-8", "{\"error\":\"Use POST\"}");
            return;
        }
        if (!RateLimiter.allow(ex)) {
            send(ex, 429, "application/json; charset=utf-8",
                    "{\"error\":\"Too many requests. Please wait a minute and try again.\"}");
            return;
        }
        try {
            String message = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            if (message.isBlank() || message.length() > 300) {
                throw new IllegalArgumentException("Please type a payment request of up to 300 characters.");
            }
            PaymentRequest req = AGENT.understand(message);
            SpendingRules.Decision d = RULES.evaluate(req);
            String explanation = AGENT.explain(req, d);
            List<FeeComparator.Quote> quotes =
                    FEES.compare(req.amountInr(), ExchangeRates.inrToEur());
            PaymentHistory.add(req, d, quotes.get(0));

            StringBuilder sb = new StringBuilder("{");
            sb.append("\"recipient\":").append(q(req.recipient())).append(",");
            sb.append("\"amount\":").append(num(req.amountInr())).append(",");
            sb.append("\"status\":").append(q(d.status().name())).append(",");
            sb.append("\"explanation\":").append(q(explanation)).append(",");

            sb.append("\"options\":[");
            for (int i = 0; i < d.options().size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(q(d.options().get(i)));
            }
            sb.append("],\"fees\":[");
            for (int i = 0; i < quotes.size(); i++) {
                FeeComparator.Quote f = quotes.get(i);
                if (i > 0) sb.append(",");
                sb.append("{\"name\":").append(q(f.route().name()))
                  .append(",\"cost\":").append(num(f.costInr()))
                  .append(",\"gets\":").append(num(f.recipientGetsEur())).append("}");
            }
            sb.append("]}");
            send(ex, 200, "application/json; charset=utf-8", sb.toString());
        } catch (Exception e) {
            send(ex, 500, "application/json; charset=utf-8",
                    "{\"error\":" + q(String.valueOf(e.getMessage())) + "}");
        }
    }

    static String num(double x) {
        return String.format(Locale.US, "%.2f", x);
    }

    static String q(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "") + "\"";
    }

    static void send(HttpExchange ex, int code, String type, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", type);
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

}