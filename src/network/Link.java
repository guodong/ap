package network;

public class Link {
    Port from;
    Port to;

    public Link(Port from, Port to) {
        this.from = from;
        this.to = to;
    }
}
