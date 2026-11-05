package borderless_pay_agent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaymentAgent {

    private final SpendingRules rules;

    public PaymentAgent(SpendingRules rules) {
        this.rules = rules;
    }

    // Step 1: the AI reads the sentence. If the AI is not available, a simple reader is used.
    public PaymentRequest understand(String userMessage) {
        try {
            String prompt = "Extract the payment details from this message. "
                + "Reply with ONE line only, in this exact format: recipient|amount_in_rupees. "
                + "Use digits only for the amount. No other text.\nMessage: " + userMessage;
            String line = GeminiClient.ask(prompt).trim();
            String[] parts = line.split("\\|");
            if (parts.length >= 2) {
                double amount = Double.parseDouble(parts[1].replaceAll("[^0-9.]", ""));
                if (amount > 0) {
                    return new PaymentRequest(parts[0].trim(), amount);
                }
            }
        } catch (Exception e) {
            System.out.println("AI not available for reading the request, using the simple reader.");
        }
        return simpleRead(userMessage);
    }

    static PaymentRequest simpleRead(String msg) {
        Matcher n = Pattern.compile("(\\d[\\d,]*(?:\\.\\d+)?)").matcher(msg);
        if (!n.find()) {
            throw new IllegalArgumentException(
                "I could not find an amount. Try: Pay 4000 rupees to my brother in Dubai");
        }
        double amount = Double.parseDouble(n.group(1).replace(",", ""));
        if (amount <= 0) {
            throw new IllegalArgumentException("The amount must be more than zero.");
        }
        String recipient = "the recipient";
        Matcher r = Pattern.compile("\\bto\\s+(.+)$", Pattern.CASE_INSENSITIVE).matcher(msg.trim());
        if (r.find()) {
            recipient = r.group(1).replaceAll("[.!?]+$", "").trim();
        }
        return new PaymentRequest(recipient, amount);
    }

    // Step 3: the AI explains the decision. If the AI is not available, a fixed explanation is used.
    public String explain(PaymentRequest req, SpendingRules.Decision d) {
        try {
            String prompt = "You are a payment assistant. Explain this decision to the user "
                + "in 2 or 3 simple sentences. Use only these facts. Do not invent numbers. "
                + "Never say the payment was declined or approved unless the decision is AUTO_APPROVED. "
                + "If the decision is OVER_LIMIT, say the payment is on hold until the user picks an option.\n"
                + "Payment: Rs " + req.amountInr() + " to " + req.recipient() + "\n"
                + "Decision: " + d.status() + "\n"
                + "Reason: " + d.reason() + "\n"
                + "Options: " + d.options();
            return GeminiClient.ask(prompt).trim();
        } catch (Exception e) {
            System.out.println("AI not available for the explanation, using a fixed explanation.");
            return fallbackExplanation(req, d);
        }
    }

    static String fallbackExplanation(PaymentRequest req, SpendingRules.Decision d) {
        String amt = String.format("Rs %.2f", req.amountInr());
        switch (d.status()) {
            case AUTO_APPROVED:
                return amt + " to " + req.recipient()
                        + " is within your limits, so it can be paid automatically.";
            case NEEDS_APPROVAL:
                return d.reason() + " Please approve it before the payment is made.";
            default:
                return "This payment is on hold. " + d.reason()
                        + " Nothing is paid until you pick an option: approve once, raise the limit,"
                        + " split the payment, or schedule it.";
        }
    }

    public void handle(String userMessage) {
        PaymentRequest req = understand(userMessage);
        SpendingRules.Decision d = rules.evaluate(req);

        System.out.println("You said    : " + userMessage);
        System.out.println("Understood  : Rs " + req.amountInr() + " to " + req.recipient());
        System.out.println("Decision    : " + d.status());
        System.out.println("Explanation : " + explain(req, d));
        for (String option : d.options()) {
            System.out.println("  Option    : " + option);
        }
        System.out.println();
    }
}