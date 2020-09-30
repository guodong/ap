package verifier;

import network.Network;
import network.Port;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ReachabilityTree {
    Network network;
    public StateNode startState;

    /**
     * port -> apset
     */
    HashMap<Port, APSet1> reachMap;

    public ReachabilityTree(Network network) {
        this.network = network;
        this.reachMap = new HashMap<>();
    }

    public ArrayList<StateNode> linkTransfer(StateNode s) {
        ArrayList<StateNode> nxtSs = new ArrayList<StateNode>();
        Port nxtpt = s.port.getPair();
        if (nxtpt == null) {
            return nxtSs;
        } else {
            APSet1 faps = s.aps;
            StateNode nxts = new StateNode(nxtpt, faps, s.getAlreadyVisited());
            s.addNextState(nxts);

            if (nxts.loopDetected()) {
            } else {
                nxtSs.add(nxts);
            }

        }
        return nxtSs;
    }

    public void build(Port start) {
        /** currently we use whole space as packet header **/
        this.startState = new StateNode(start, new APSet1(BDDEngine.BDDTrue));
        this.traverse(this.startState);
    }

    private void traverse(StateNode st) {
        ArrayList<StateNode> nextStates = forwardState(st);
        for (StateNode s : nextStates) {
            ArrayList<StateNode> nxtSl = linkTransfer(s);
            for (StateNode nxtsl : nxtSl) {
                traverse(nxtsl);
            }
        }
    }

    private ArrayList<StateNode> forwardState(StateNode s) {
        ArrayList<StateNode> nxtSs = new ArrayList<StateNode>();
        HashMap<Port, APSet1> nexts = ((APDevice) s.port.getDevice()).forward(s.port, s.aps);
        if (nexts.isEmpty()) {
            return nxtSs;
        } else {
            for (Port p : nexts.keySet()) {
                StateNode st = new StateNode(p, nexts.get(p), s.getAlreadyVisited());
                s.addNextState(st);
                if (!st.loopDetected()) {
                    nxtSs.add(st);
                }
            }
        }
        return nxtSs;
    }
}
