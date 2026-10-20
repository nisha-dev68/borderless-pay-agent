package borderless_pay_agent;

import java.net.URI;
import java.net.http.*;
import java.util.Locale;
import java.util.regex.*;

public class PayPalService {

    // Creates a sandbox order. Returns {orderId, approveUrl}.
    public static String[] createOrder(double usd, String description, String baseUrl)
            throws Exception {
        String token = PayPalOrder.getToken();
        String value = String.format(Locale.US, "%.2f", usd);
        String desc = description.replace("\\", " ").replace("\"", " ");
        String unit = "\"purchase_units\":[{\"description\":\"" + desc
                + "\",\"amount\":{\"currency_code\":\"USD\",\"value\":\"" + value + "\"}}]";
        String ret = baseUrl + "/paypal/return";
        String cancel = baseUrl + "/paypal/cancel";
        String ctx = "\"return_url\":\"" + ret + "\",\"cancel_url\":\"" + cancel
                + "\",\"user_action\":\"PAY_NOW\",\"shipping_preference\":\"NO_SHIPPING\","
                + "\"brand_name\":\"Borderless Pay Agent\"";

        String newStyle = "{\"intent\":\"CAPTURE\"," + unit
                + ",\"payment_source\":{\"paypal\":{\"experience_context\":{" + ctx + "}}}}";
        String oldStyle = "{\"intent\":\"CAPTURE\"," + unit
                + ",\"application_context\":{" + ctx + "}}";

        String last = "";
        for (String body : new String[] { newStyle, oldStyle }) {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(PayPalOrder.BASE + "/v2/checkout/orders"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
            HttpResponse<String> res = PayPalOrder.client.send(req, HttpResponse.BodyHandlers.ofString());
            last = res.body();
            String id = PayPalOrder.find(last, "id");
            Matcher m = Pattern.compile(
                "\"href\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"rel\"\\s*:\\s*\"(?:approve|payer-action)\"")
                .matcher(last);
            if (res.statusCode() < 300 && id != null && m.find()) {
                return new String[] { id, m.group(1) };
            }
        }
        throw new RuntimeException("PayPal did not return an approval link: " + last);
    }

    // Captures an approved order. Returns {status, paypalFeeUsd}.
    public static String[] capture(String orderId) throws Exception {
        String token = PayPalOrder.getToken();
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(PayPalOrder.BASE + "/v2/checkout/orders/" + orderId + "/capture"))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();
        HttpResponse<String> res = PayPalOrder.client.send(req, HttpResponse.BodyHandlers.ofString());
        String body = res.body();
        String status = PayPalOrder.find(body, "status");
        Matcher f = Pattern.compile(
            "\"paypal_fee\"\\s*:\\s*\\{[^}]*\"value\"\\s*:\\s*\"([0-9.]+)\"").matcher(body);
        String fee = f.find() ? f.group(1) : "0.00";
        return new String[] { status != null ? status : "FAILED", fee };
    }
}