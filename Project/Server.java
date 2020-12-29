package cs2030.simulator;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

/**
 * The Server class defines a server, either a human server or a self-checkout server.
 * @author Wang Pei
 */
public class Server {

    /**
     * Servers have
     * (1) A queue of waiting customers 
     * (2) A maximum size of queue;
     * (3) An unique id;
     * (4) A RandomGenerator that determines 
	         server's servicing and resting behaviors.
     * (5) A double value denoting probability of resting.
	 * (6) A customer it is currently serving.
     * (7) Static fields of:
     *      [1]. Total server number.
     *      [2]. Default serving duration.
     */
    private final Queue<Customer> customerQueue;
    private final int maxQLen;
    private final int id;
    private final RandomGenerator rng; 
    private final double Pr;
	private Optional<Customer> servingCustomer;
    private boolean isResting = false;          // By default, isResting is false.
    private static int SERVER_NUMBER = 0;
    private static double DEFAULT_SERVICE_TIME = 1;

    /**
     * Constructs a server with an RandomGenerator with given parameters.
     * @param maxQLen           The maximum length of the waiting queue.
     * @param rng               The RandomGenerator that determines 
     *                              the servicing and resting behaviors.
     * @param Pr                The probability of resting.
	 * @param customerQueue		The queue of waiting customers of the server.
     */
    private Server(int maxQLen, RandomGenerator rng, double Pr, Queue<Customer> customerQueue) {
        this.servingCustomer = Optional.empty();
		this.customerQueue = customerQueue;
        this.maxQLen = maxQLen;
        this.id = ++SERVER_NUMBER;
        this.rng = rng;
        this.Pr = Pr;
    }

    /**
     * Create a human server with given maximum queue length, RandomGenerator,
     * and resting probability.
     * @param maxQLen   The given maximum queue length.
     * @param rng       The given RandomGenerator.
     * @param Pr        The given resting probability.
     * @return          A human server.
     */
    public static Server createHumanServer(int maxQLen, RandomGenerator rng, double Pr) {
        /*
		 * Each human server has its own customer queue.
		 */
		return new Server(maxQLen, rng, Pr, new LinkedList<Customer>());
    }

    /**
     * Create a self-checkout server with given RandomGenerator and shared queue.
	 * @param maxSharedQLen 	The maximum length of the shared queue of all self-sheckout servers. 
     * @param rng       		The given RandomGenerator.
	 * @param sharedQueue		The shared queue of all self-sheckout servers. 					
     * @return          		A self-checkout server.
     */
    public static Server createSelfCheckout(int maxSharedQLen, RandomGenerator rng, Queue<Customer> sharedQueue) {
        /*
         * Self-checkout servers have a resting probability Pr of -1, flagging it as a self-checkout server.
         */
        return new Server(maxSharedQLen, rng, -1, sharedQueue);
    }

    public boolean isSelfCheckout() {
        return this.Pr == -1;
    }

    /**
     * Check if the server can serve a customer immediately.
     * @return  true if can server and false otherwise.
     */
    public boolean canServe() {
        return servingCustomer.isEmpty() && isResting == false;
    }

    /**
     * Checks if the customer queue is not full.
     * @return 		true if not full. false otherwise.
     */
    public boolean hasQueueingSpace() {
        // Make sure to check serving availability before checking waiting availability.
        assert !canServe();

        return customerQueue.size() < this.maxQLen;
    }

    /**
     * Decides if the server will rest or not.
     * @return      A boolean value denoting if it is going to rest.
     */
    public boolean ifRest() {
		assert (!isSelfCheckout());
		
        return this.rng.genRandomRest() < Pr;
    }

    /**
     * Toggle the resting state of the server,
     * and returns the calling server wrapped in an Optional.
     * @return      This server wrapped as a Optional<Server>.
     */
    public Optional<Server> toggleRest() {
        // Only servers with positive Pr can rest.
        assert Pr > 0;

        this.isResting = (!this.isResting);
        return Optional.of(this);
    }

    /**
     * Returns the back time of the resting server 
     * based on the given startig time of resting.
     * @param time		The starting time of resting.
     * @return          The given starting time plus a random resting period.
     */
    public double getBackTime(double time) {
        // Only resting servers can get back time.
        assert isResting == true;

        return time + rng.genRestPeriod();
    }

    /**
     * Serve a customer if is available.
     * @param customer  The customer to be served;
     * @return          The service time.
     */
    public double serveAndGetTime(Customer customer) {
        assert canServe();
		
        this.servingCustomer = Optional.of(customer);
        return rng.genServiceTime();
    }
    
    /**
     * Remove current serving customer and get next customer if applicable.
     * @return      An Optional customer to be next served.
     */
    public Optional<Customer> doneServingAndGetNext() {
        // Get the next customer from the queue and update current serving customer.
        this.servingCustomer = Optional.ofNullable(this.customerQueue.poll());
        
		return this.servingCustomer;
    }
    
    /**
     * Enqueue a customer
     * @param customer The customer to be enqueued.
     */
    public void enqueue(Customer customer) {
        assert this.hasQueueingSpace();
		
        this.customerQueue.add(customer);
    }
    
    public int currentQLen() {
        return this.customerQueue.size();
    }

    @Override
    public String toString() {
        return isSelfCheckout()
            ? "self-check " + this.id
            : "server " + this.id;
    }
}
