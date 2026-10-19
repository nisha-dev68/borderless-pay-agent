package borderless_pay_agent;

import java.util.List;

public class FeeDemo {

    public static void main(String[] args) throws Exception {
        FeeComparator comparator = new FeeComparator();
        double eurPerInr = ExchangeRates.inrToEur();
        System.out.printf("Rate used: 1 INR = %.5f EUR%n%n", eurPerInr);

        compareAndExplain(comparator, 1000, eurPerInr);
        compareAndExplain(comparator, 9000, eurPerInr);
    }

    static void compareAndExplain(FeeComparator comparator, double amountInr, double eurPerInr)
            throws Exception {
        List<FeeComparator.Quote> quotes = comparator.compare(amountInr, eurPerInr);
        System.out.println("Sending Rs " + amountInr);

        StringBuilder facts = new StringBuilder();
        for (FeeComparator.Quote q : quotes) {
            String line = String.format("%s -> cost Rs %.2f, recipient gets EUR %.2f",
                    q.route().name(), q.costInr(), q.recipientGetsEur());
            System.out.println("  " + line);
            facts.append(line).append("\n");
        }

        String prompt = "Explain in 2 or 3 simple sentences why the first option is the cheapest. "
            + "Use only these facts and do not invent numbers.\n" + facts;
        System.out.println("  Why: " + GeminiClient.ask(prompt).trim());
        System.out.println();
    }
}