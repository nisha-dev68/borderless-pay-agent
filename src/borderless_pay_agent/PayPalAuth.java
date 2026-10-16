package borderless_pay_agent;

import java.net.URI;
import java.net.http.*;
import java.util.Base64;

public class PayPalAuth {
    public static void main(String[] args) throws Exception {
        String id = System.getenv("PAYPAL_CLIENT_ID");
        String secret = System.getenv("PAYPAL_SECRET");
        String auth = Base64.getEncoder()
            .encodeToString((id + ":" + secret).getBytes());

        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create("https://api-m.sandbox.paypal.com/v1/oauth2/token"))
            .header("Authorization", "Basic " + auth)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
            .build();

        HttpResponse<String> res = HttpClient.newHttpClient()
            .send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println(res.statusCode());
        System.out.println(res.body());
    }
}