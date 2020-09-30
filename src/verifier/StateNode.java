package verifier;

import network.Port;

import java.util.HashMap;
import java.util.HashSet;

public class StateNode {
    public static final int contflag = 0;
    public static final int loopflag = 2;
    public static final int deadflag = 3;
    int flag = deadflag;
    Port port;
    APSet1 aps;
    HashSet<Port> visitedPorts;

    HashMap<Port, StateNode> nextStateNodes;

    public StateNode(Port port, APSet1 aps) {
        this.port = port;
        this.aps = aps;

        this.visitedPorts = new HashSet<>();
        this.nextStateNodes = null;
    }

    public StateNode(Port port, APSet1 aps, HashSet<Port> visitedPorts) {
        this.port = port;
        this.aps = aps;

        this.visitedPorts = visitedPorts;
        this.nextStateNodes = null;
    }

    public void addNextState(StateNode s) {
        if (nextStateNodes == null) {
            nextStateNodes = new HashMap<Port, StateNode>();
            flag = contflag;
        }
        nextStateNodes.put(s.port, s);
    }

    public HashSet<Port> getAlreadyVisited()
    {
        HashSet<Port> alv = new HashSet<Port> (visitedPorts);
        alv.add(port);
        return alv;
    }

    public boolean loopDetected() {
        if (visitedPorts.contains(port)) {
            flag = loopflag;
            return true;
        } else {
            return false;
        }
    }

    private String printFlag() {
        switch (flag) {
            case contflag:
                return "cont";
            case loopflag:
                return "loop";
            case deadflag:
                return "dead";
            default:
                System.err.println("unknown flag " + flag);
                System.exit(1);
        }
        return null;
    }

    public void printLoop() {
        printLoop_recur("");
    }

    private void printLoop_recur(String headstr) {
        String newheadstr = headstr + port.getName() + " " + aps + " ";
        if (flag == loopflag) {
            System.out.println(newheadstr + printFlag());
            return;
        } else if (nextStateNodes == null) {
            return;
        } else {
            for (StateNode nxtS : nextStateNodes.values()) {
                nxtS.printLoop_recur(newheadstr);
            }
        }
    }
}
