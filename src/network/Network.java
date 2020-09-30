package network;
import java.util.*;

public class Network {
    HashMap<String, Device> devices = new HashMap<>();
    ArrayList<Link> links = new ArrayList<>();

    public Device addDevice(String name) {
        Device d = new Device(name);
        this.devices.put(name, d);
        return d;
    }

    public Device getDevice(String name) {
        return devices.get(name);
    }

    public void addDevice(Device device) {
        this.devices.put(device.name, device);
    }

    public HashMap<String, Device> getDevices() {
        return devices;
    }

    public Link addLink(Port from, Port to) {
        Link l = new Link(from, to);
        this.links.add(l);
        from.pair = to;
        to.pair = from;
        return l;
    }

    private Port getPort(String dname, String pname) {
        return this.devices.get(dname).ports.get(pname);
    }

    /**
     * Warning: here we must add ports that may not contained in FIB, see link transfer
     */
    public Link addLink(String fromd, String fromp, String tod, String top) {
        Port fromport = getPort(fromd, fromp);
        Port toport = getPort(tod, top);
        if (fromport == null) {
            // we must create a port that is not in FIB
            fromport = new Port(fromp, getDevice(fromd));
            System.out.println("no such port: "+ fromd + " " + fromp);
        }
        if (toport == null) {
            toport = new Port(top, getDevice(tod));
            System.out.println("no such port: "+ tod + " " + top);
        }
        if (fromport != null && toport != null) {
            return this.addLink(fromport, toport);
        }
        return null;
    }

    public ArrayList<Link> getLinks() {
        return links;
    }
}
