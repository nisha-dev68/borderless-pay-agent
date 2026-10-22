package borderless_pay_agent;

public class RulesTest {

    static int passed = 0;
    static int failed = 0;

    static void check(String name, boolean ok) {
        if (ok) {
            passed++;
            System.out.println("PASS  " + name);
        } else {
            failed++;
            System.out.println("FAIL  " + name);
        }
    }

    static SpendingRules.Decision decide(SpendingRules rules, double amount) {
        return rules.evaluate(new PaymentRequest("Test", amount));
    }

    public static void main(String[] args) {
        SpendingRules rules = new SpendingRules(2000, 20000, 15000);

        check("Rs 1500 is auto-approved",
                decide(rules, 1500).status() == SpendingRules.Status.AUTO_APPROVED);
        check("Rs 2000 (exact auto limit) is auto-approved",
                decide(rules, 2000).status() == SpendingRules.Status.AUTO_APPROVED);
        check("Rs 2001 needs approval",
                decide(rules, 2001).status() == SpendingRules.Status.NEEDS_APPROVAL);
        check("Rs 5000 (exact remaining) needs approval, not over limit",
                decide(rules, 5000).status() == SpendingRules.Status.NEEDS_APPROVAL);
        check("Rs 5001 is over the limit",
                decide(rules, 5001).status() == SpendingRules.Status.OVER_LIMIT);

        SpendingRules.Decision over = decide(rules, 9000);
        check("Over-limit offers a split of Rs 5000 now",
                over.options().stream().anyMatch(o -> o.contains("pay Rs 5000.00 now")));
        check("Over-limit offers 4 choices", over.options().size() == 4);

        SpendingRules full = new SpendingRules(2000, 20000, 20000);
        SpendingRules.Decision none = decide(full, 100);
        check("Nothing left: payment is over the limit",
                none.status() == SpendingRules.Status.OVER_LIMIT);
        check("Nothing left: no split is offered",
                none.options().stream().noneMatch(o -> o.startsWith("Split")));

        FeeComparator fees = new FeeComparator();
        check("Rs 1000: INR route is cheapest",
                fees.compare(1000, 0.01).get(0).route().name().startsWith("Pay in INR"));
        check("Rs 9000: EUR route is cheapest",
                fees.compare(9000, 0.01).get(0).route().name().startsWith("Pay in EUR"));
        var q = fees.compare(5000, 0.01);
        check("Fees are sorted cheapest first",
                q.get(0).costInr() <= q.get(1).costInr() && q.get(1).costInr() <= q.get(2).costInr());

        System.out.println();
        System.out.println(passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }
}