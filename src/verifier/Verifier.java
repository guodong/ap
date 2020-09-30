package verifier;

import jdd.bdd.BDD;
import network.Device;
import network.Network;
import network.Port;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class Verifier {
    HashSet<Integer> AP;
    Network network;
    ReachabilityTree rt;

    HashSet<Integer> allPred = new HashSet<>();
    HashMap<Integer, HashSet<Integer>> predicateREP = new HashMap<>();

    public Verifier(Network network) {
        this.network = network;
        AP = new HashSet<>();
    }

    public ReachabilityTree getRt() {
        return rt;
    }

    public void buildRT(Port start) {
        rt = new ReachabilityTree(network);
        rt.build(start);
    }

    public HashSet<Integer> getAP() {
        return AP;
    }

    public void computePredicates() {
        for (Device d : network.getDevices().values()) {
            APDevice apd = (APDevice) d;
            apd.computePredicates();
            allPred.addAll(apd.getPredicates().values());
        }
    }

    /**
     * compute ap from a set of predicates
     */
    public void computeAP() {
        BDD bdd = BDDEngine.getInstance().bdd;
        HashSet<Integer> done = new HashSet<>();
        AP = new HashSet<>();
        for (Device d : network.getDevices().values()) {
            for (int pred : ((APDevice) d).getPredicates().values()) {
                if (!done.contains(pred)) {
                    done.add(pred);

                    int predNeg = bdd.not(pred);
                    bdd.ref(predNeg);
                    if (AP.size() == 0) {
                        if (pred != APDevice.BDDFalse) {
                            bdd.ref(pred);
                            AP.add(pred);
                        }
                        if (predNeg != APDevice.BDDFalse) {
                            AP.add(predNeg);
                        }
                    } else {
                        // forbid concurrent error
                        HashSet<Integer> oldList = AP;
                        AP = new HashSet<>();
                        Iterator<Integer> iterold = oldList.iterator();
                        while (iterold.hasNext()) {
                            int oldap = iterold.next();
                            int tmps = bdd.and(pred, oldap);
                            bdd.ref(tmps);
                            if (tmps != APDevice.BDDFalse) {
                                AP.add(tmps);
                            }

                            tmps = bdd.and(predNeg, oldap);
                            bdd.ref(tmps);
                            if (tmps != APDevice.BDDFalse) {
                                AP.add(tmps);
                            }
                        }

                        // TODO: deref unused bdd
                        int[] toDeRef = new int[oldList.size() + 1];
                        int cntr = 0;
                        for (int oldbdd : oldList) {
                            toDeRef[cntr] = oldbdd;
                            cntr++;
                        }
                        toDeRef[oldList.size()] = predNeg;
                        BDDEngine.getInstance().DerefInBatch(toDeRef);
                    }
                }
            }
        }

        APSet1.setUniverse(AP);

        // compute ap format of each pred
        for (int pred : allPred) {
            predicateREP.put(pred, computeAPExp(pred));
        }

        // map ap exprs to ports
        for (Device d: network.getDevices().values()) {
            APDevice apd = (APDevice)d;
            for (Port p : apd.getPredicates().keySet()) {
//                HashSet<Integer> apexp = computeAPExp(apd.predicates.get(p));
                APSet1 apset = new APSet1(predicateREP.get(apd.predicates.get(p)));
                apd.APs.put(p, apset);
            }
        }
    }

    /**
     * compute the ap expression of an original predicate
     * @param pred
     * @return the set of aps
     */
    private HashSet<Integer> computeAPExp(int pred) {
        HashSet<Integer> apexp = new HashSet<Integer> ();
        if (pred == BDDEngine.BDDFalse) {
            return apexp;
        } else if (pred == BDDEngine.BDDTrue) {
            return new HashSet<Integer>(AP);
        }

        for (int oneap : AP) {
            if (BDDEngine.getInstance().bdd.and(oneap, pred) != BDDEngine.BDDFalse) {
                apexp.add(oneap);
            }
        }
        return apexp;
    }

    public void insertRule(String dname, int ip, int prefix, String outPort) {
        int pred = BDDEngine.getInstance().encodeDstIPPrefix(ip, prefix);
        if (allPred.contains(pred)) {
            System.out.println("predicate exists.");
            return;
        }
        HashMap<Integer, ArrayList<Integer>> changelist = AddOnePredicate(pred);

    }

    /**
     * add one predicate to the current predicate sets,
     * change AP, PredicateBDD,get the expression for the new predicate, return change list
     */
    private HashMap<Integer, ArrayList<Integer>> AddOnePredicate(int bddToAdd)
    {

        HashMap<Integer, ArrayList<Integer>> changelist = new HashMap<Integer, ArrayList<Integer>>();
        BDD thebdd = BDDEngine.getInstance().bdd;

//        AddPredicateOnly(bddToAdd);

        int bddToAddNeg = thebdd.not(bddToAdd);
        thebdd.ref(bddToAddNeg);

        // old list
        HashSet<Integer> oldList = AP;
        // set up a new list
        AP = new HashSet<Integer>();
        Iterator<Integer> iterold = oldList.iterator();
        HashSet<Integer> newexp = new HashSet<Integer>();
        while(iterold.hasNext())
        {
            int oldap = iterold.next();

            int tmps1 = thebdd.and(bddToAdd, oldap);
            if(tmps1 != BDDEngine.BDDFalse)
            {
                thebdd.ref(tmps1);
                AP.add(tmps1);
                newexp.add(tmps1);
            }

            int tmps2 = thebdd.and(bddToAddNeg, oldap);
            if(tmps2 != BDDEngine.BDDFalse)
            {
                thebdd.ref(tmps2);
                AP.add(tmps2);
            }

            if(tmps1 != oldap && tmps2 != oldap)
            {
                ArrayList<Integer> newary = new ArrayList<Integer>();
                newary.add(tmps1);
                newary.add(tmps2);
                changelist.put(oldap, newary);
            }
        }
        /**
         * in this case, we need to de-ref useless nodes.
         * we still keep bddToAdd, since it is the bdd node for an acl
         * we will de-ref:
         * bddToAddNeg, the whole list of oldList.
         */
        int [] toDeRef = new int[oldList.size() + 1];
        int cntr = 0;
        for(int oldbdd : oldList)
        {
            toDeRef[cntr] = oldbdd;
            cntr ++;
        }
        toDeRef[oldList.size()] = bddToAddNeg;
        BDDEngine.DerefInBatch(toDeRef);

        predicateREP.put(bddToAdd, newexp);

        // TODO: re-map the pred of each port to AP?

        return changelist;
    }
}
