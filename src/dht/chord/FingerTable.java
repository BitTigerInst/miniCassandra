package dht.chord;

import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.ArrayList;

public class FingerTable {
	private ArrayList<InetSocketAddress> starts;
    private InetSocketAddress successor;

	private static Logger logger = Logger.getLogger(FingerTable.class);

	public FingerTable() {
	}
	
	public void setFingerTableList(ArrayList<InetSocketAddress> starts) {
		this.starts = starts;
	}

    public void setSuccessor(InetSocketAddress successor) {
        this.successor = successor;
    }

	public int getListSize() {
		return starts.size();
	}

    public void add(InetSocketAddress var) {
        if (!starts.contains(var)) starts.add(var);
    }

    public void remove(InetSocketAddress var) {
        if (starts.contains(var)) starts.remove(var);
    }
	
	public InetSocketAddress getNode(int index) {
		return starts.get(index);
	}
}
