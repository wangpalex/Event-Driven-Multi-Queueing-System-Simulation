package cs2030.simulator;

/**
 * EventType is a enum class used to represent different types of Event.
 * @author Wang Pei
 */
public enum EventType {
    /**
     * The EventType has 7 different types arranged according priority.
     * (1). DONE
     * (2). SERVE
     * (3). REST
     * (4). BACK
     * (5). SERVE
     * (6). WAIT
     * (7). LEAVE
     */
    DONE(0),
    SERVE(1),
    REST(2),
    BACK(3),
    ARRIVAL(4),
    WAIT(5),
    LEAVE(6);

    private int type;

    EventType(int s) { 
        this.type = s; 
    }

    @Override
    public String toString() {
        switch (this.type) {
            case 0:
                return "done";
            case 1:
                return "served";
            case 2:
                return "rest";
            case 3:
                return "back";
            case 4:
                return "arrives";
            case 5:
                return "waits";
            case 6:
                return "leaves";
            default:
                return "Undefined type";
        }
    }
}
