package borderless_pay_agent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PaymentHistory {

    record Entry(String time, String recipient, double amount, String status,
                 String bestRoute, double feeInr, double friendGetsEur, String source) {
    }

    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd MMM, HH:mm", Locale.ENGLISH);

    static {
        // Demo rows so the table is not empty on first open.
        String inr = "Pay in INR (PayPal converts to EUR)";
        String eur = "Pay in EUR (card converts INR to EUR)";
        ENTRIES.add(new Entry("12 Oct, 10:15", "Friend in Germany", 800, "AUTO_APPROVED", inr, 73, 7.27, "Demo data"));
        ENTRIES.add(new Entry("13 Oct, 18:40", "Friend in Germany", 1500, "AUTO_APPROVED", inr, 115, 13.85, "Demo data"));
        ENTRIES.add(new Entry("15 Oct, 09:05", "Cousin in France", 2500, "NEEDS_APPROVAL", eur, 165, 23.35, "Demo data"));
        ENTRIES.add(new Entry("16 Oct, 20:30", "Brother in Dubai", 4000, "NEEDS_APPROVAL", eur, 240, 37.60, "Demo data"));
        ENTRIES.add(new Entry("17 Oct, 11:50", "University abroad", 9000, "OVER_LIMIT", eur, 490, 85.10, "Demo data"));
    }

    public static synchronized void add(PaymentRequest req, SpendingRules.Decision d,
                                        FeeComparator.Quote best) {
        ENTRIES.add(new Entry(LocalDateTime.now().format(FMT), req.recipient(), req.amountInr(),
                d.status().name(), best.route().name(), best.costInr(),
                best.recipientGetsEur(), "Live check"));
        if (ENTRIES.size() > 200) {
            ENTRIES.remove(0);
        }
    }
    
    public static synchronized void addPaid(String recipient, double amountInr,
            String orderId, double feeInr) {
double friendGetsEur = Math.max(amountInr - feeInr, 0) * ExchangeRates.inrToEur();
ENTRIES.add(new Entry(LocalDateTime.now().format(FMT), recipient, amountInr, "PAID",
"PayPal sandbox order " + orderId, feeInr, friendGetsEur, "PayPal sandbox"));
if (ENTRIES.size() > 200) {
ENTRIES.remove(0);
}
}

    public static synchronized String toJson() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = ENTRIES.size() - 1; i >= 0; i--) {
            Entry e = ENTRIES.get(i);
            if (i != ENTRIES.size() - 1) {
                sb.append(",");
            }
            sb.append("{\"time\":").append(WebServer.q(e.time()))
              .append(",\"recipient\":").append(WebServer.q(e.recipient()))
              .append(",\"amount\":").append(WebServer.num(e.amount()))
              .append(",\"status\":").append(WebServer.q(e.status()))
              .append(",\"bestRoute\":").append(WebServer.q(e.bestRoute()))
              .append(",\"feeInr\":").append(WebServer.num(e.feeInr()))
              .append(",\"friendGetsEur\":").append(WebServer.num(e.friendGetsEur()))
              .append(",\"source\":").append(WebServer.q(e.source()))
              .append("}");
        }
        return sb.append("]").toString();
    }
}