package network;

public class Rule implements Comparable<Rule> {
    long dstIp;
    int prefix;
    Port outPort;

    public Rule(long dstIp, int prefix, Port outPort) {
        this.dstIp = dstIp;
        this.prefix = prefix;
        this.outPort = outPort;
    }

    public long getDstIp() {
        return dstIp;
    }

    public int getPrefix() {
        return prefix;
    }

    public Port getOutPort() {
        return outPort;
    }

    @Override
    public int compareTo(Rule o) {
        return this.prefix - o.prefix;
    }
}
