package cs2030.simulator;

/**
 * The Customer class defines a customer.
 * @author Wang Pei
 */
public class Customer {

    /*
     * Customers have:
     * (1) An unique id.
     * (2) A time of arrival.
     * (3) A boolean value denoting if it is greedy.
     * (3) Static fields storing customer statistics:
     *      [1]. total customer number.
     *      [2]. total wait time.
     *      [3]. number of customers served.
     *      [4]. number of customers left.
     */

    private final int id;
    private final double arrival;
    private final boolean greedy;
    public static int CUSTOMER_NUMBER = 0;
    public static double WAIT_TIME = 0;
    public static int CUSTOMER_SERVED = 0;
    public static int CUSTOMER_LEFT = 0;

    /**
     * Constructs a Customer.
     * @param arrival   The time a customer arrives.
     * @param isGreedy  The boolean value denoting if it is greedy.
     */
    private Customer(double arrival, boolean isGreedy) {
        this.id = ++CUSTOMER_NUMBER;
		this.arrival = arrival;
        this.greedy = isGreedy;
    }

    /**
     * Creates a typical customer.
     * @param arrival   The time of arrival of the customer.
     * @return          A typical customer.
     */
    public static Customer createCustomer(double arrival) {
        return new Customer(arrival, false);
    }

    /**
     * Creates a greedy customer.
     * @param arrival   The time of arrival of the customer.
     * @return          A greedy customer.
     */
    public static Customer createGreedy(double arrival) {
        return new Customer(arrival, true);
    }

    public boolean isGreedy() {
        return this.greedy;
    }

    /**
     * Returns time duration of waiting to a specified time.
     * @param time  The specified time time to calculate duration.
     * @return      The time duration between arrival and the specified time.
     */
    public double getWaitTime(double time) {
        return time - this.arrival;
    }

    /**
     * Calculates statistics about customers.
     * The returned string contains:
     * (1) Average waiting time.
     * (2) Number of customers served.
     * (3) Number of customers left.
     * @return  A String of statistics.
     */
    public static String statistics() {
        return String.format("[%.3f %d %d]", 
                (CUSTOMER_SERVED == 0) ? 0.0 : WAIT_TIME / CUSTOMER_SERVED, 
                CUSTOMER_SERVED, 
                CUSTOMER_LEFT);
    }

    @Override
    public String toString() {
        return (isGreedy())
            ? this.id + "(greedy)"
            : this.id + "";
    }
}
