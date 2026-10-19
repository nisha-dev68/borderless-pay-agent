package borderless_pay_agent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FeeComparator {

    // ASSUMED fees for the demo. Real fees vary, so replace these with real fee tables.
    public record Route(String name, double fxMarkupPercent, double feePercent, double fixedFeeInr) {
    }

    public record Quote(Route route, double costInr, double recipientGetsEur) {
    }

    private final List<Route> routes = List.of(
        new Route("Pay in INR (PayPal converts to EUR)", 4.0, 2.0, 25),
        new Route("Pay in USD, then convert to EUR", 6.0, 2.0, 25),
        new Route("Pay in EUR (card converts INR to EUR)", 3.0, 2.0, 40)
    );

    public List<Quote> compare(double amountInr, double eurPerInr) {
        List<Quote> quotes = new ArrayList<>();
        for (Route r : routes) {
            double cost = amountInr * (r.fxMarkupPercent() + r.feePercent()) / 100.0
                    + r.fixedFeeInr();
            double recipientGets = (amountInr - cost) * eurPerInr;
            quotes.add(new Quote(r, cost, recipientGets));
        }
        quotes.sort(Comparator.comparingDouble(Quote::costInr));
        return quotes;
    }
}