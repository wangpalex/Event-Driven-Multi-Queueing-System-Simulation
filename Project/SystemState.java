package cs2030.simulator;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

/**
 * The immutable SystemState class stores the state of customers and servers.
 * @author Wang Pei
 */
public class SystemState {
    /**
     * SystemState contains
     * (1) A list of customers,
     * (2) A list of human servers,
     * (3) A list of self-checkout servers,
     * (4) A shared queue of all self-checkout servers,
	 * (5) The maximum length of the shared queue.
     */
    private final List<Customer> customers;
    private final List<Server> humanServers;
    private final List<Server> selfCheckouts;
    private final Queue<Customer> sharedQueue;
    private final int maxQLen;

    /**
     * constructs a SystemState
     * @param customers			The list of customers.
     * @param humanServers   	The list of human servers.
	 * @param selfCheckouts		The list of self-checkout servers.
	 * @param sharedQueue		The shared queue of all self-checkout servers.
	 * @param maxQLen			The maximum length of the shared queue.
     */
    public SystemState(List<Customer> customers, List<Server> humanServers, 
            List<Server> selfCheckouts, Queue<Customer> sharedQueue, int maxQLen) {
        this.customers = customers;
        this.humanServers = humanServers;
        this.selfCheckouts = selfCheckouts;
        this.sharedQueue = sharedQueue;
        this.maxQLen = maxQLen;
    }

    /**
     * Get the first server which can serve a customer immediately, if any.
     * @return an Optional server that can serve the customer.
     */
    Optional<Server> getFirstServableServer() {
        for (Server s : humanServers) {
            if (s.canServe())
                return Optional.of(s);
        }
        for (Server s : selfCheckouts) {
            if (s.canServe())
                return Optional.of(s);
        }
        return Optional.empty();
    }

    /**
     * Gets the first server which has space to enqueue a customer.
     * @return The server which can enqueue a customer.
     */
    public Optional<Server> getFirstWaitableServer() {
        for (Server s : humanServers) {
            if (s.hasQueueingSpace()) {
                return Optional.of(s);
            }
        }
        if ((!selfCheckouts.isEmpty()) && sharedQueue.size() < maxQLen) {
            // The first self-sheckout server will be used to indicate availability of the shared queue.
			return Optional.of(selfCheckouts.get(0));
		}
        return Optional.empty();
    }

    /**
     * Gets the waitable server with the least number of queueing customers
     * @return The waitable server with the least queueing size.
     */
    public Optional<Server> getShortestQueueServer() {
		Server[] hs = humanServers.toArray(new Server[0]);
		// Order servers ascendingly according to queueing length.
		Arrays.sort(hs, (s1, s2) -> s1.currentQLen() - s2.currentQLen());
		Server minServer = (hs.length > 0) ? hs[0] : null;
		int minLen = (minServer != null) ? minServer.currentQLen() : maxQLen;
		
		if (!selfCheckouts.isEmpty() && sharedQueue.size() < minLen) {
			minServer = selfCheckouts.get(0);
			minLen = sharedQueue.size();
		}
		
		// If all queues are full, returns empty optional.
		return (minLen < maxQLen) ? Optional.ofNullable(minServer) : Optional.empty();
    }
}
