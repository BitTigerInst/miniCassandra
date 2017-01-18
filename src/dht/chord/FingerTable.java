package dht.chord;

import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;

public class FingerTable {
	private ArrayList<InetSocketAddress> starts;
	private static Logger logger = Logger.getLogger(FingerTable.class);
    private InetSocketAddress address;
    private int[] ipAddr;

	public FingerTable(InetSocketAddress address) {
        this.address = address;
        this.ipAddr = new int[4];
        String ip = address.getAddress().getHostAddress();
        logger.info(ip);
        String[] ipStrList = ip.split("\\.");
        for (int i = 0; i < 4; i++) {
            ipAddr[i] = Integer.valueOf(ipStrList[i]);
        }
	}
	
	public void setFingerTableList(ArrayList<InetSocketAddress> starts) {
		this.starts = starts;
	}

    public InetSocketAddress getSuccessor() {
        for (InetSocketAddress addr : starts) {
            String[] ipList = addr.getAddress().toString().split(".");
            for (int i = 0; i < 4; i++) {
                if (Integer.valueOf(ipList[i]) > ipAddr[i]) {
                    return addr;
                } else if (Integer.valueOf(ipList[i]) < ipAddr[i]) {
                    break;
                }
            }
        }
        return starts.isEmpty() ? null : starts.get(0);
    }

	public int getListSize() {
		return starts.size();
	}

    public void printList() {
        logger.debug("Finger List: {");
        starts.forEach(logger::debug);
        logger.debug("}");
    }

    public void add(InetSocketAddress var) {
        if (!starts.contains(var)) {
            starts.add(var);
            Collections.sort(starts, (o1, o2) -> {
                String[] ip1 = o1.getAddress().getHostAddress().split("\\.");
                String[] ip2 = o2.getAddress().getHostAddress().split("\\.");
                for (int i = 0; i < 4; i++) {
                    int n1 = Integer.valueOf(ip1[i]), n2 = Integer.valueOf(ip2[i]);
                    if (n1 != n2) {
                        return n1 - n2;
                    }
                }
                return 1;
            });
        }
    }

    public void remove(InetSocketAddress var) {
        if (starts.contains(var)) starts.remove(var);
    }

	public InetSocketAddress getNode(int index) {
		return starts.get(index);
	}
}
