package borderless_pay_agent;

public class RulesDemo {

    public static void main(String[] args) {
        SpendingRules rules = new SpendingRules(2000, 20000, 15000);

        test(rules, new PaymentRequest("Friend in Germany", 1500));
        test(rules, new PaymentRequest("Friend in Germany", 4000));
        test(rules, new PaymentRequest("Tuition fee abroad", 9000));
    }

    static void test(SpendingRules rules, PaymentRequest req) {
        SpendingRules.Decision d = rules.evaluate(req);
        System.out.println("Pay Rs " + req.amountInr() + " to " + req.recipient());
        System.out.println("  Status : " + d.status());
        System.out.println("  Reason : " + d.reason());
        for (String option : d.options()) {
            System.out.println("  Option : " + option);
        }
        System.out.println();
    }
}