package borderless_pay_agent;

import java.net.URI;
import java.net.http.*;

public class PayPalCapture {
    public static void main(String[] args) throws Exception {
    	String orderId = "0XN94025EC282061J";
        String token = PayPalOrder.getToken();
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(PayPalOrder.BASE + "/v2/checkout/orders/" + orderId + "/capture"))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();
        HttpResponse<String> res = PayPalOrder.client.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println(res.statusCode());
        System.out.println(res.body());
    }
}
