package verifier;

import jdd.bdd.BDD;

public final class BDDEngine {
    private static BDDEngine instance;
    public BDD bdd;
    int[] dstIp;
    public final static int BDDFalse = 0;
    public final static int BDDTrue = 1;

    public static BDDEngine getInstance() {
        if (instance == null) {
            instance = new BDDEngine();
            instance.bdd = new BDD(10000, 10000);
            instance.dstIp = new int[32];
            instance.DeclareVars(instance.dstIp, 32);
        }
        return instance;
    }

    public static void DerefInBatch(int[] vars) {
        for (int i = 0; i < vars.length; i++) {
            BDDEngine.getInstance().bdd.deref(vars[i]);
        }
    }

    private void DeclareVars(int[] vars, int bits) {
        for (int i = bits - 1; i >= 0; i--) {
            vars[i] = bdd.createVar();
        }
    }

    /***
     * var is a BDD variable if flag == 1, return var if flag == 0, return not
     * var, the new bdd node is referenced.
     */
    private int EncodingVar(int var, int flag) {
        if (flag == 0) {
            int tempnode = bdd.not(var);
            // no need to ref the negation of a variable.
            // the ref count is already set to maximal
            // aclBDD.ref(tempnode);
            return tempnode;
        }
        if (flag == 1) {
            return var;
        }

        // should not reach here
        System.err.println("flag can only be 0 or 1!");
        return -1;
    }

    /**
     * @param prefix -
     * @param vars   - bdd variables used
     * @param bits   - number of bits in the representation
     * @return a bdd node representing the predicate e.g. for protocl, bits = 8,
     * prefix = {1,0,1,0}, so the predicate is protocol[4] and (not
     * protocol[5]) and protocol[6] and (not protocol[7])
     */
    private int EncodePrefix(int[] prefix, int[] vars, int bits) {
        if (prefix.length == 0) {
            return BDDTrue;
        }

        int tempnode = BDDTrue;
        for (int i = 0; i < prefix.length; i++) {
            if (i == 0) {
                tempnode = EncodingVar(vars[bits - prefix.length + i],
                        prefix[i]);
            } else {
                int tempnode2 = EncodingVar(vars[bits - prefix.length + i],
                        prefix[i]);
                int tempnode3 = bdd.and(tempnode, tempnode2);
                bdd.ref(tempnode3);
                // do not need tempnode2, tempnode now
                // aclBDD.deref(tempnode2);
                // aclBDD.deref(tempnode);
                DerefInBatch(new int[]{tempnode, tempnode2});
                // refresh tempnode 3
                tempnode = tempnode3;
            }
        }
        return tempnode;
    }

    public int encodeDstIPPrefix(long ipaddr, int prefixlen) {
        int[] ipbin = Utility.CalBinRep(ipaddr, 32);
        int[] ipbinprefix = new int[prefixlen];
        for (int k = 0; k < prefixlen; k++) {
            ipbinprefix[k] = ipbin[k + 32 - prefixlen];
        }
        int entrybdd = EncodePrefix(ipbinprefix, this.dstIp, 32);
        return entrybdd;
    }
}
