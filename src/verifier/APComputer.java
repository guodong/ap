package verifier;

import java.util.ArrayList;
import java.util.HashMap;

public class APComputer {

    ArrayList<Integer> predicates;
    HashMap<Integer, Integer> predicateBDD;

    public APComputer(ArrayList<Integer> predicates) {
        this.predicates = predicates;
    }

    public void AddPredicateOnly(int pbdd) {
        if (predicateBDD.containsKey(pbdd)) {
            predicateBDD.put(pbdd, predicateBDD.get(pbdd) + 1);
        } else {
            predicateBDD.put(pbdd, 1);
        }
    }

    public void compute() {
        predicateBDD = new HashMap<Integer, Integer>();
        for (int abdd : this.predicates) {
            AddPredicateOnly(abdd);
        }
        int[] index = new int[this.predicates.size()];
        for (int i = 0; i < index.length; i++) {
            index[i] = i;
        }

        System.out.println(index.length);
    }
}
