package cs2030.simulator;

import java.util.Optional;
import java.util.Queue;

/**
 *  The event abstract class changes the state of the system 
 *  and schedules new events.
 *  @author Wang Pei
 */
public abstract class Event implements Comparable<Event> {
    /*
     * All events have
     * (1) A customer involved in the event;
     * (2) A server involved in the event;
     * (3) Time of the event;
     * (4) Type of the event.
     */
    
	// Using default access modifier to allow access of enclosed class
	final Customer customer; 
    final Server server;
    final double time;
    final EventType type;

    /**
     * Constructs an Event.
     * @param customer		The customer of the event.
     * @param server		The server of the event.
     * @param time			The time of the event.
     * @param type  		The type of the event.
     */
    private Event(Customer customer, Server server, double time, EventType type) {
        this.customer = customer;
        this.server = server;
        this.time = time;
        this.type = type;
    }

    /**
     * Gets the next event based current system state
     * and updates statistics in Customer class.
     * @param state     The current SystemState.
     * @return          The next scheduled Event.
     */
    abstract Optional<Event> getNextEvent(SystemState state);

    @Override
    public String toString() {
        return String.format("%.3f %s %s", this.time, this.customer, this.type);
    }

    @Override
    public int compareTo(Event other) {
        return this.time != other.time
            ? Double.compare(this.time, other.time)
            : this.type.compareTo(other.type);
    }

    public EventType getType() {
        return this.type;
    }

    /**
     * returns an arrival event.
     * @param time      The time of arrival.
     * @param customer  The arrived customer.
     * @return          An arrival Event.
     */
    public static Event arrivalEvent(double time, Customer customer) {
        return new Event(customer, null, time, EventType.ARRIVAL) {
            
			/**
             * Arrival Events have 3 types of next event:
             * (1) A Serve Event if a server is free, or
             * (2) A Wait Event if a server can enqueue a customer, or
             * (3) A Leave Event if no server can serve or enqueue a customer.
			 * @param state 	Current SystemState.
			 * @return 			An Optional event.
             */
            @Override
            public Optional<Event> getNextEvent(SystemState state) {
				return Optional.of(
                        state.getFirstServableServer()
                        .map(s -> serveEvent(time, this.customer, s))
                        .orElseGet(() -> {
                            if (this.customer.isGreedy()) {
                                return state.getShortestQueueServer()
                                    .map(s -> waitEvent(time, this.customer, s))
                                    .orElse(leaveEvent(time, this.customer));
                            } else {
                                return state.getFirstWaitableServer()
                                    .map(s -> waitEvent(time, this.customer, s))
                                    .orElse(leaveEvent(time, this.customer));
                            }
                        }));
            }
        };
    }

    /**
     * Returns an serveEvent.
     * @param time      The time of the event.
     * @param customer  The customer to be served.
     * @param server    The server used to serve customer.
     * @return          An serve event.
     */
    public static Event serveEvent(double time, Customer customer, Server server) {
        return new Event(customer, server, time, EventType.SERVE) {
            
			/**
             * Serve Events can only have next events as Done Event.
			 * @param state 	Current SystemState.
			 * @return 			An Optional event.
             */
            @Override
            public Optional<Event> getNextEvent(SystemState state) {
                double serviceTime = this.server.serveAndGetTime(customer);
                double doneTime = time + serviceTime;

                // update statistics
                Customer.CUSTOMER_SERVED++;
                Customer.WAIT_TIME += this.customer.getWaitTime(time);
                return Optional.of(doneEvent(doneTime, customer, server)); 
            }

            @Override
            public String toString() {
                return String.format("%.3f %s served by %s", this.time, this.customer, this.server);
            }
        };
    }
    
    /**
     * Returns a restEvent
     * @param server    The server involved in the event.
     * @param time      The time involved in the event.
     * @return          A restEvent.
     */
    public static Event restEvent(double time, Server server) {
        return new Event(null, server, time, EventType.REST) {
			
			/**
			 * A restEvent has a next event as backEvent.
			 * @param state 	Current SystemState.
			 * @return 			An Optional event.
			 */
            @Override
            public Optional<Event> getNextEvent(SystemState state) {
                double backTime = server.getBackTime(time);
                return server.toggleRest().map(s -> Event.backEvent(backTime, s));
            }

            @Override
            public String toString() {
                return String.format("%.3f %s %s", this.time, this.server, this.type); 
            }
        };
    }

    /**
     * Returns a backEvent.
     * @param time     	The time when the server is back.
     * @param server    The server involved in the event.
     * @return          A backEvent.
     */
    public static Event backEvent(double time, Server server) {
        return new Event(null, server, time, EventType.BACK) {
			
			/**
			 * A backEvent can have a next event as serveEvent 
			 * if there is customer waiting in queue.
			 * @param state 	Current SystemState.
			 * @return 			An Optional event.
			 */
            @Override
            public Optional<Event> getNextEvent(SystemState state) {
                return server.doneServingAndGetNext().map(c -> serveEvent(time, c, server));
            } 

            @Override
            public String toString() {
                return String.format("%.3f %s %s", this.time, this.server, this.type); 
            }
        }; 
    }

    /**
     * Returns a Done Event.
     * @param time      The time of the event.
     * @param customer  The customer of the event.
     * @param server    The server of the event.
     * @return          A Serve Event if any customer is waiting in queue.
     */
    public static Event doneEvent(double time, Customer customer, Server server) {
        return new Event(customer, server, time, EventType.DONE) {
            
			/**
             * The next event of a done Event can be
             * a Serve Event (of waiting customer)
             * or a Rest Event
             * or an empty Optional
			 * @param state 	Current SystemState.
			 * @return 			An Optional event.
             */
            @Override
            public Optional<Event> getNextEvent(SystemState state) {          
                if ((!server.isSelfCheckout()) && server.ifRest()) {
                    return server.toggleRest().map(s -> restEvent(time, s));
                } else {
                    return server.doneServingAndGetNext().map(c -> serveEvent(time, c, server));
                }
            }
            
            @Override
            public String toString() {
                return String.format("%.3f %s done serving by %s", this.time, this.customer, this.server);
            }
        };
    }

    /**
     * Returns a Wait Event.
     * @param time      The time of the event.
     * @param customer  The customer to be waited in the event
     * @param server    The serve of the event.
     * @return          A Wait Event.
     */
    public static Event waitEvent(double time, Customer customer, Server server) {
        return new Event(customer, server, time, EventType.WAIT) {
            
			/**
             * Wait events gives empty next event as
             * Done events will handle waiting customers.
			 * @param state 	Current SystemState.
			 * @return 			An Optional event.
             */
            @Override
            public Optional<Event> getNextEvent(SystemState state) {
				this.server.enqueue(this.customer);
				return Optional.empty();    
            }

            @Override
            public String toString() {
                return String.format("%.3f %s waits to be served by %s", this.time, this.customer, this.server);
            }
        };
    }

    /**
     * Returns a Leave Event
     * @param time      The time of the event.
     * @param customer  The customer of the event.
     * @param server    The server of the event.
     * @return          A Leave Event.
     */
    public static Event leaveEvent(double time, Customer customer) {
        return new Event(customer, null, time, EventType.LEAVE) {
            
			/**
             * Leave Events have empty Optional as next events.
			 * @param state 	Current SystemState.
			 * @return 			An Optional event.
             */
            @Override
            public Optional<Event> getNextEvent(SystemState state) {
                Customer.CUSTOMER_LEFT++;
                return Optional.empty();
            }
        };
    }
}
