package network;

import java.util.*;

public class Device {
    String name;
    protected ArrayList<Rule> rules = new ArrayList<>();
    HashMap<String, Port> ports = new HashMap<>();

    public Device(String name) {
        this.name = name;
    }

    public Port addPortByName(String name) {
        Port p = new Port(name, this);
        this.ports.put(name, p);
        return p;
    }

    public void addRule(Rule rule) {
        this.rules.add(rule);
    }

    public ArrayList<Rule> getRules() {
        return rules;
    }

    public Port getPortByName(String name) {
        return this.ports.get(name);
    }
}
