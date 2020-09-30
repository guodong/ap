import network.*;
import verifier.APComputer;
import verifier.APDevice;
import verifier.Verifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Network n = new Network();

        ///// parse configs of I2 /////
        String foldername2 = "i2/";
        String[] devicenames = {"atla", "chic", "hous", "kans", "losa", "newy32aoa", "salt", "seat", "wash"};

        for (String dn : devicenames) {
            APDevice d = new APDevice(dn);
            n.addDevice(d);
            File inputFile = new File(foldername2 + dn + "ap");
            Scanner line = null;
            try {
                line = new Scanner(inputFile);
                line.useDelimiter("\n");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.exit(0);
            }

            while (line.hasNext()) {
                String linestr = line.next();
                String[] tokens = linestr.split(" ");
                if (tokens[0].equals("fw")) {
                    String portname = tokens[3].split("\\.")[0];
                    Port p = d.getPortByName(portname);
                    if (p == null) {
                        p = d.addPortByName(portname);
                    }
                    Rule rule = new Rule(Long.parseLong(tokens[1]), Integer.parseInt(tokens[2]), p);
                    d.addRule(rule);
                }
            }
        }

        n.addLink("chic","xe-0/1/0","newy32aoa","xe-0/1/3");
        n.addLink("chic","xe-1/0/1","kans","xe-0/1/0");
        n.addLink("chic","xe-1/1/3","wash","xe-6/3/0");
        n.addLink("hous","xe-3/1/0","losa","ge-6/0/0");
        n.addLink("kans","ge-6/0/0","salt","ge-6/1/0");
        n.addLink("chic","xe-1/1/2","atla","xe-0/1/3");
        n.addLink("seat","xe-0/0/0","salt","xe-0/1/1");
        n.addLink("chic","xe-1/0/2","kans","xe-0/0/3");
        n.addLink("hous","xe-1/1/0","kans","xe-1/0/0");
        n.addLink("seat","xe-0/1/0","losa","xe-0/0/0");
        n.addLink("salt","xe-0/0/1","losa","xe-0/1/3");
        n.addLink("seat","xe-1/0/0","salt","xe-0/1/3");
        n.addLink("newy32aoa","et-3/0/0-0","wash","et-3/0/0-0");
        n.addLink("newy32aoa","et-3/0/0-1","wash","et-3/0/0-1");
        n.addLink("chic","xe-1/1/1","atla","xe-0/0/0");
        n.addLink("losa","xe-0/1/0","seat","xe-2/1/0");
        n.addLink("hous","xe-0/1/0","losa","ge-6/1/0");
        n.addLink("atla","xe-0/0/3","wash","xe-1/1/3");
        n.addLink("hous","xe-3/1/0","kans","ge-6/2/0");
        n.addLink("atla","ge-6/0/0","hous","xe-0/0/0");
        n.addLink("chic","xe-1/0/3","kans","xe-1/0/3");
        n.addLink("losa","xe-0/0/3","salt","xe-0/1/0");
        n.addLink("atla","ge-6/1/0","hous","xe-1/0/0");
        n.addLink("atla","xe-1/0/3","wash","xe-0/0/0");
        n.addLink("chic","xe-2/1/3","wash","xe-0/1/3");
        n.addLink("atla","xe-1/0/1","wash","xe-0/0/3");
        n.addLink("kans","xe-0/1/1","salt","ge-6/0/0");
        n.addLink("chic","xe-1/1/0","newy32aoa","xe-0/0/0");
        System.out.println(n.getLinks().size());
        ///// end parse configs of I2 /////


//        APDevice a = (APDevice)n.getDevices().get("atla");
//        System.out.println(a.getPredicates().values());
        Verifier v = new Verifier(n);

        // step 1: compute preds of all devices
        v.computePredicates();

        // step2: compute APs from step 1
        v.computeAP();
        System.out.println(v.getAP().size());

        // step3: build reachiability tree
        v.buildRT(n.getDevice("chic").getPortByName("ge-7/1/0"));
        v.getRt().startState.printLoop();
    }
}
