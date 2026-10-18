package borderless_pay_agent;

import java.net.URI;
import java.net.http.*;
import java.util.regex.*;

public class GeminiClient {

    // If you get a 404 error, change this to a model name shown in Google AI Studio.
    static final String MODEL = "gemini-3.5-flash";
    static final HttpClient client = HttpClient.newHttpClient();

    public static String ask(String prompt) throws Exception {
        String key = System.getenv("GEMINI_API_KEY");
        String body = "{\"contents\":[{\"parts\":[{\"text\":\"" + escape(prompt) + "\"}]}]}";

        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/"
                    + MODEL + ":generateContent"))
            .header("x-goog-api-key", key)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new RuntimeException("Gemini error " + res.statusCode() + ": " + res.body());
        }
        return extractText(res.body());
    }

    static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    static String extractText(String json) {
        Matcher m = Pattern.compile("\"text\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(json);
        if (!m.find()) {
            throw new RuntimeException("No text in response: " + json);
        }
        return m.group(1).replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
    }
}