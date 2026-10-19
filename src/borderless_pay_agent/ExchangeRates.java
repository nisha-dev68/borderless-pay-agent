package borderless_pay_agent;

import java.net.URI;
import java.net.http.*;
import java.util.regex.*;

public class ExchangeRates {

    static final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    // Demo value, used only if the live rate cannot be fetched.
    static final double FALLBACK_EUR_PER_INR = 0.01;

    public static double inrToEur() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.frankfurter.dev/v1/latest?base=INR&symbols=EUR"))
                .GET()
                .build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            Matcher m = Pattern.compile("\"EUR\"\\s*:\\s*([0-9.]+)").matcher(res.body());
            if (res.statusCode() == 200 && m.find()) {
                return Double.parseDouble(m.group(1));
            }
        } catch (Exception e) {
            // fall through to the demo rate
        }
        System.out.println("(Live rate not available, using a demo rate)");
        return FALLBACK_EUR_PER_INR;
    }
}
