package cs2030.simulator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Initializer processes the raw data and initializes a Simulator.
 * @author Wang Pei
 */
public class Initializer {
    /**
     * Initialize a Simulator with given raw data
     * @param serverNumber          Number of servers involved in the simulation.
     * @param selfCheckoutNumner    Number of self-checkout servers in the simulation.
     * @param customerNumber        Number of customers involved in the simulation.
     * @param maxQLen               The maximum queueing capacity of servers.
     * @param seed                  The seed value of RandomGenerator.
     * @param lambda                The parameter for the arrival rate of RandomGenerator.
     * @param mu                    The parameter for the service rate of RandomGenerator.
     * @param rho                   The parameter for the resting rate of RandomGenerator.
     * @param Pr                    The threshold probability of human server resting.
     * @param Pg                    The probability for a greedy customer occuring.
     * @return                      An initialized Simulator.
     */
    public static Simulator init(
            int serverNumber, int selfCheckoutNumber,  
            int customerNumber, 
            int maxQLen, 
            int seed, double lambda, double mu, double rho, 
            double Pr, double Pg) 
    {
        List<Server> servers = new ArrayList<>();
        List<Customer> customers = new ArrayList<>();
        List<Server> selfCheckouts = new ArrayList<>();
        Queue<Customer> sharedQueue = new LinkedList<>();
		PriorityQueue<Event> initialEvents = new PriorityQueue<>();
		RandomGenerator rng = new RandomGenerator(seed, lambda, mu, rho);
		
		// Initialize human servers.
        while (serverNumber-- > 0) {
            servers.add(Server.createHumanServer(maxQLen, rng, Pr));
        }
		
        // Initailize self-checkout servers.
        while (selfCheckoutNumber-- > 0) {
            selfCheckouts.add(Server.createSelfCheckout(maxQLen, rng, sharedQueue));
        }
		
		// Initialize customers.
        double arrivalTime = 0;
        while (customerNumber-- > 0) {
            Customer c = (rng.genCustomerType() < Pg)
                ? Customer.createGreedy(arrivalTime)
                : Customer.createCustomer(arrivalTime);
            customers.add(c);
            initialEvents.add(Event.arrivalEvent(arrivalTime, c));
            arrivalTime += rng.genInterArrivalTime();
        }
		
		// Initialize SystemState.
        SystemState initialState = new SystemState(customers, servers, selfCheckouts, sharedQueue, maxQLen);
		
        return new Simulator(initialEvents, initialState);
    }
}
