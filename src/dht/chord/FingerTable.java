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

	//get a node form finger table accroding to the index
	public InetSocketAddress get_node(int index) {
		return starts.get(index);
	}

	protected int get_id() {
		return id;
	}
}
