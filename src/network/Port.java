package network;

public class Port {
    String name;
    Device device;
    Port pair = null;

    public Port(String name, Device device) {
        this.name = name;
        this.device = device;
        this.pair = null;
    }

    public void setPair(Port pair) {
        this.pair = pair;
    }

    public String getName() {
        return name;
    }

    public Device getDevice() {
        return device;
    }

    public Port getPair() {
        return pair;
    }
}
