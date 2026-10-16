package borderless_pay_agent;

import java.net.URI;
import java.net.http.*;
import java.util.Base64;
import java.util.regex.*;

public class PayPalOrder {
    static final String BASE = "https://api-m.sandbox.paypal.com";
    static HttpClient client = HttpClient.newHttpClient();

    static String find(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\":\"([^\"]+)\"").matcher(json);
        return m.find() ? m.group(1) : null;
    }

    static String getToken() throws Exception {
        String auth = Base64.getEncoder().encodeToString(
            (System.getenv("PAYPAL_CLIENT_ID") + ":" + System.getenv("PAYPAL_SECRET")).getBytes());
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(BASE + "/v1/oauth2/token"))
            .header("Authorization", "Basic " + auth)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
            .build();
        return find(client.send(req, HttpResponse.BodyHandlers.ofString()).body(), "access_token");
    }

    public static void main(String[] args) throws Exception {
        String token = getToken();
        String body = "{\"intent\":\"CAPTURE\",\"purchase_units\":[{\"amount\":{\"currency_code\":\"USD\",\"value\":\"10.00\"}}]}";
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(BASE + "/v2/checkout/orders"))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        String res = client.send(req, HttpResponse.BodyHandlers.ofString()).body();
        System.out.println("ORDER ID: " + find(res, "id"));
        System.out.println(res);
    }
}
