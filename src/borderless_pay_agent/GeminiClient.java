package borderless_pay_agent;

import java.net.URI;
import java.net.http.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.*;

public class GeminiClient {

    // Change the model without editing code: set GEMINI_MODEL in Render or in Eclipse.
    static final String MODEL = System.getenv().getOrDefault("GEMINI_MODEL", "gemini-3.6-flash");
    static final HttpClient client = HttpClient.newHttpClient();

    // Same question, same answer: saves the free daily quota.
    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

    public static String ask(String prompt) throws Exception {
        String cached = CACHE.get(prompt);
        if (cached != null) {
            return cached;
        }
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
            throw new RuntimeException("Gemini error " + res.statusCode());
        }
        String text = extractText(res.body());
        CACHE.put(prompt, text);
        return text;
    }

    static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    static String extractText(String json) {
        Matcher m = Pattern.compile("\"text\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(json);
        if (!m.find()) {
            throw new RuntimeException("No text in response");
        }
        return m.group(1).replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
    }
}