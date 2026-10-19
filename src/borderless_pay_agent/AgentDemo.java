package borderless_pay_agent;

public class AgentDemo {

    public static void main(String[] args) throws Exception {
        SpendingRules rules = new SpendingRules(2000, 20000, 15000);
        PaymentAgent agent = new PaymentAgent(rules);

        agent.handle("Send 1500 rupees to my friend in Germany");
        agent.handle("Pay 9000 rupees tuition fee to my university abroad");
    }
}