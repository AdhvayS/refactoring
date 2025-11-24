package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {
    private Invoice invoice;
    private Map<String, Play> plays;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.setInvoice(invoice);
        this.setPlays(plays);
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     *
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {
        final StringBuilder result =
                new StringBuilder("Statement for " + getInvoice().getCustomer()
                        + System.lineSeparator());

        // loop only responsible for building the lines for each performance
        for (Performance p : getInvoice().getPerformances()) {
            result.append(String.format("  %s: %s (%s seats)%n",
                    getPlay(p).name,
                    usd(getAmount(p)),
                    p.audience));
        }

        final int totalAmount = getTotalAmount();
        final int volumeCredits = getTotalVolumeCredits();

        result.append(String.format("Amount owed is %s%n", usd(totalAmount)));
        result.append(String.format("You earned %s credits%n", volumeCredits));
        return result.toString();
    }

    private Play getPlay(Performance performance) {
        return getPlays().get(performance.playID);
    }

    private int getAmount(Performance performance) {
        int result = 0;
        switch (getPlay(performance).type) {
            case "tragedy":
                result = Constants.TRAGEDY_BASE_AMOUNT;
                if (performance.audience > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.TRAGEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.audience - Constants.TRAGEDY_AUDIENCE_THRESHOLD);
                }
                break;
            case "comedy":
                result = Constants.COMEDY_BASE_AMOUNT;
                if (performance.audience > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.audience - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                result += Constants.COMEDY_AMOUNT_PER_AUDIENCE * performance.audience;
                break;
            default:
                throw new RuntimeException(
                        String.format("unknown type: %s", getPlay(performance).type));
        }
        return result;
    }

    private int getVolumeCredits(Performance performance) {
        int result = 0;
        // base volume credits
        result += Math.max(
                performance.audience - Constants.BASE_VOLUME_CREDIT_THRESHOLD,
                0);
        // extra credit for every five comedy attendees
        if ("comedy".equals(getPlay(performance).type)) {
            result += performance.audience / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        }
        return result;
    }

    private int getTotalVolumeCredits() {
        int result = 0;
        for (Performance p : getInvoice().getPerformances()) {
            result += getVolumeCredits(p);
        }
        return result;
    }

    private int getTotalAmount() {
        int result = 0;
        for (Performance p : getInvoice().getPerformances()) {
            result += getAmount(p);
        }
        return result;
    }

    private String usd(int amount) {
        final NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        return format.format(amount / Constants.PERCENT_FACTOR);
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Map<String, Play> getPlays() {
        return plays;
    }

    public void setPlays(Map<String, Play> plays) {
        this.plays = plays;
    }
}

