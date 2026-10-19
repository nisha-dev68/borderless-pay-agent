package borderless_pay_agent;

public class PaymentAgent {

    private final SpendingRules rules;

    public PaymentAgent(SpendingRules rules) {
        this.rules = rules;
    }

    // Step 1: the AI reads the user's sentence and extracts the details.
    public PaymentRequest understand(String userMessage) throws Exception {
        String prompt = "Extract the payment details from this message. "
            + "Reply with ONE line only, in this exact format: recipient|amount_in_rupees. "
            + "Use digits only for the amount. No other text.\nMessage: " + userMessage;
        String line = GeminiClient.ask(prompt).trim();
        String[] parts = line.split("\\|");
        if (parts.length < 2) {
            throw new IllegalStateException("Could not understand the reply: " + line);
        }
        double amount = Double.parseDouble(parts[1].replaceAll("[^0-9.]", ""));
        return new PaymentRequest(parts[0].trim(), amount);
    }

    // Step 3: the AI explains the decision in simple words.
    public String explain(PaymentRequest req, SpendingRules.Decision d) throws Exception {
        String prompt = "You are a payment assistant. Explain this decision to the user "
            + "in 2 or 3 simple sentences. Use only these facts. Do not invent numbers. "
            + "Never say the payment was declined or approved unless the decision is AUTO_APPROVED. "
            + "If the decision is OVER_LIMIT, say the payment is on hold until the user picks an option.\n"
            + "Payment: Rs " + req.amountInr() + " to " + req.recipient() + "\n"
            + "Decision: " + d.status() + "\n"
            + "Reason: " + d.reason() + "\n"
            + "Options: " + d.options();
        return GeminiClient.ask(prompt).trim();
    }

    public void handle(String userMessage) throws Exception {
        PaymentRequest req = understand(userMessage);
        SpendingRules.Decision d = rules.evaluate(req);   // Step 2: rules decide

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