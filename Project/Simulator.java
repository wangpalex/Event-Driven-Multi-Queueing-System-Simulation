package cs2030.simulator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * The simulator class that simulates discrete events.
 * @author Wang Pei
 */
public class Simulator {
    /**
     * The simulator contains
     * (1) A SystemState;
     * (2) A list of completed events;
     * (3) A PriorityQueue of futureEvents;
     */
    private final SystemState state;
    private final List<Event> completedEvents;
    private final PriorityQueue<Event> futureEvents;
	
	/**
     * Constructs a simulator with given initial conditions.
     * @param initialEvents		The PriorityQueue of initially scheduled events. 
     * @param initialState      The initial SystemState of the simulator.
     * @return              	An initialized instance of Simulator.
     */
	public Simulator(PriorityQueue<Event> initialEvents, SystemState initialState) {
        this.futureEvents = initialEvents;
        this.completedEvents = new ArrayList<>();
		this.state = initialState;
    }
	
	/**
	 * Runs the simulation and stores completed events 
	 * until there are no more scheduled future events.
	 */
    public void run() {
        while(!futureEvents.isEmpty()) {
            // Get the top priority event.
            Event curr = futureEvents.poll();

            // Get the next event.
            Optional<Event> next = curr.getNextEvent(this.state);

            // Enqueue the next event if present.
            next.ifPresent(e -> futureEvents.add(e));
            
            // Add current event into completed events; rest and back events will not be shown in simulation results.
            if (curr.getType() != EventType.REST && curr.getType() != EventType.BACK) {
                completedEvents.add(curr);
			}
        }
    }
    
    /** 
	 * Encapsulates the printing of result into a callable method.
     */
    public void printResult() {
        System.out.println(this.toString());
    }

    @Override
    public String toString() {
        String s = "";
        for (Event e : completedEvents) {
            s += e + "\n";
        }
        s += Customer.statistics();
        return s;
    }
}
