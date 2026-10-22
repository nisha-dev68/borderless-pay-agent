package borderless_pay_agent;

import com.sun.net.httpserver.HttpExchange;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {

    private static final int MAX_REQUESTS = 10;      // per visitor, per minute
    private static final long WINDOW_MS = 60_000;
    private static final int DAILY_CAP = 300;        // all visitors together

    private static final Map<String, Deque<Long>> HITS = new ConcurrentHashMap<>();
    private static long dayStart = System.currentTimeMillis();
    private static int dayCount = 0;

    public static boolean allow(HttpExchange ex) {
        long now = System.currentTimeMillis();
        synchronized (RateLimiter.class) {
            if (now - dayStart > 86_400_000L) {
                dayStart = now;
                dayCount = 0;
            }
            if (dayCount >= DAILY_CAP) {
                return false;
            }
        }
        Deque<Long> q = HITS.computeIfAbsent(clientIp(ex), k -> new ArrayDeque<>());
        synchronized (q) {
            while (!q.isEmpty() && now - q.peekFirst() > WINDOW_MS) {
                q.pollFirst();
            }
            if (q.size() >= MAX_REQUESTS) {
                return false;
            }
            q.addLast(now);
        }
        synchronized (RateLimiter.class) {
            dayCount++;
        }
        return true;
    }

    static String clientIp(HttpExchange ex) {
        String fwd = ex.getRequestHeaders().getFirst("X-Forwarded-For");
        if (fwd != null && !fwd.isBlank()) {
            return fwd.split(",")[0].trim();
        }
        return ex.getRemoteAddress().getAddress().getHostAddress();
    }
}