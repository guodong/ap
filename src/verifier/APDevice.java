package verifier;

import jdd.bdd.BDD;
import network.Device;
import network.Port;
import network.Rule;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class APDevice extends Device {

    HashMap<Port, Integer> predicates;
    HashMap<Port, APSet1> APs;
    BDD aclBDD;

//    int[] dstIP;
//    final static int protocolBits = 8;
//    int[] protocol; // protocol[0] is the lowest bits
//    final static int portBits = 16;
//    int[] srcPort;
//    int[] dstPort;
//    final static int ipBits = 32;
//    int[] srcIP;

    /**
     * for readability. In bdd: 0 is the false node 1 is the true node
     */
    public final static int BDDFalse = 0;
    public final static int BDDTrue = 1;

    public APDevice(String name) {
        super(name);
        this.predicates = new HashMap<>();
        this.APs = new HashMap<>();
        aclBDD = BDDEngine.getInstance().bdd;
    }

    public HashMap<Port, Integer> getPredicates() {
        return predicates;
    }

    public void computePredicates() {
        Collections.sort(rules);
//        Collections.reverse(this.getRules());
        int alreadyfwded = BDDFalse;
        for (int j = rules.size() - 1; j >= 0; j--) {
            Rule r = rules.get(j);
            Port outPort = r.getOutPort();
            int entrybdd = BDDEngine.getInstance().encodeDstIPPrefix(r.getDstIp(),
                    r.getPrefix());

            int notalreadyfwded = aclBDD.not(alreadyfwded);
            aclBDD.ref(notalreadyfwded);
            int toadd = aclBDD.and(entrybdd, notalreadyfwded);
            aclBDD.ref(toadd);
            aclBDD.deref(notalreadyfwded);
            int altmp = aclBDD.or(alreadyfwded, entrybdd);
            aclBDD.ref(altmp);
            aclBDD.deref(alreadyfwded);
            alreadyfwded = altmp;
            aclBDD.deref(entrybdd);

            if (predicates.containsKey(outPort)) {
                int oldkey = predicates.get(outPort);
                int newkey = aclBDD.or(toadd, oldkey);
                aclBDD.ref(newkey);
                aclBDD.deref(toadd);
                aclBDD.deref(oldkey);
                predicates.put(outPort, newkey);
            } else {
                predicates.put(outPort, toadd);
            }
        }
        aclBDD.deref(alreadyfwded);
    }

    public HashMap<Port, APSet1> forward(Port inPort, APSet1 apset) {
        HashMap<Port, APSet1> out = new HashMap<>();
        for (Port p : this.APs.keySet()) {
            if (p != inPort) {
                APSet1 outap = new APSet1(apset);
                outap.intersect(this.APs.get(p));
                if (!outap.isempty()) {
                    out.put(p, outap);
                }
            }
        }
        return out;
    }
}
