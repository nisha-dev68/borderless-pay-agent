package borderless_pay_agent;

import java.net.URI;
import java.net.http.*;
import java.util.regex.*;

public class ExchangeRates {

    static final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    // Demo values, used only if the live rate cannot be fetched.
    static final double FALLBACK_EUR_PER_INR = 0.01;
    static final double FALLBACK_USD_PER_INR = 0.012;

    public static double inrToEur() {
        return rate("EUR", FALLBACK_EUR_PER_INR);
    }

    public static double inrToUsd() {
        return rate("USD", FALLBACK_USD_PER_INR);
    }

    private static double rate(String currency, double fallback) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.frankfurter.dev/v1/latest?base=INR&symbols=" + currency))
                .GET()
                .build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            Matcher m = Pattern.compile("\"" + currency + "\"\\s*:\\s*([0-9.]+)").matcher(res.body());
            if (res.statusCode() == 200 && m.find()) {
                return Double.parseDouble(m.group(1));
            }
        } catch (Exception e) {
            // fall through to the demo rate
        }
        System.out.println("(Live " + currency + " rate not available, using a demo rate)");
        return fallback;
    }
}