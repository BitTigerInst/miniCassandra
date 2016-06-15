package dht.chord;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import dht.node.NodeImpl;

public class FingerTable {
	private NodeImpl   node;
	private int        id;
	private ArrayList<InetSocketAddress> starts;

	public FingerTable(NodeImpl node, int id) {
		this.node = node;
		this.id = id;
		starts = new ArrayList<>();
	}
	
	public void set_finger_table_list(ArrayList<InetSocketAddress> starts) {
		this.starts = starts;
	}

	public int get_list_size() {
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
	
	//get a node form finger table accroding to the index
	public InetSocketAddress get_node(int index) {
		return starts.get(index);
	}

	protected int get_id() {
		return id;
	}
}
