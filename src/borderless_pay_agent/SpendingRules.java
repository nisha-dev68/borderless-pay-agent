package borderless_pay_agent;

import java.util.ArrayList;
import java.util.List;

public class SpendingRules {

    public enum Status { AUTO_APPROVED, NEEDS_APPROVAL, OVER_LIMIT }

    public record Decision(Status status, String reason, List<String> options) {
    }

    private final double autoApproveLimit;
    private final double monthlyLimit;
    private final double spentThisMonth;

    public SpendingRules(double autoApproveLimit, double monthlyLimit, double spentThisMonth) {
        this.autoApproveLimit = autoApproveLimit;
        this.monthlyLimit = monthlyLimit;
        this.spentThisMonth = spentThisMonth;
    }

    public Decision evaluate(PaymentRequest req) {
        double amount = req.amountInr();
        double remaining = monthlyLimit - spentThisMonth;

        if (amount > remaining) {
            List<String> options = new ArrayList<>();
            options.add("Approve once");
            options.add("Raise the monthly limit");
            if (remaining > 0) {
                options.add(String.format("Split: pay Rs %.2f now, Rs %.2f next month",
                        remaining, amount - remaining));
            }
            options.add("Schedule the full payment for next month");
            String reason = String.format("Rs %.2f is above your remaining monthly limit of Rs %.2f.",
                    amount, remaining);
            return new Decision(Status.OVER_LIMIT, reason, options);
        }

        if (amount > autoApproveLimit) {
            String reason = String.format("Rs %.2f is above your auto-approve limit of Rs %.2f.",
                    amount, autoApproveLimit);
            return new Decision(Status.NEEDS_APPROVAL, reason, List.of("Approve", "Cancel"));
        }

        return new Decision(Status.AUTO_APPROVED,
                "Within your limits, so it can be paid automatically.", List.of());
    }
}