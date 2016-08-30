package dht.chord;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import dht.node.NodeImpl;

public class FingerTable {
	private ArrayList<InetSocketAddress> starts;

	public FingerTable() {
	}
	
	public void setFingerTableList(ArrayList<InetSocketAddress> starts) {
		this.starts = starts;
	}

	public int getListSize() {
		return starts.size();
	}

	public void replace(int idx, InetSocketAddress var2) {
		if(idx > 0 ) {
			starts.remove(idx);
			starts.add(idx, var2);
		}else {
			System.out.println("replace error, finger table didn't contain var1");
		}
	}
	
	//get a node form finger table according to the index
	public InetSocketAddress getNode(int index) {
		return starts.get(index);
	}
}
