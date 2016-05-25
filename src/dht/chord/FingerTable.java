package dht.chord;

import dht.node.NodeImpl;
import java.util.ArrayList;

public class FingerTable {
	private NodeImpl   node;
	private int        id;
	private ArrayList<NodeImpl> starts;

	public FingerTable(NodeImpl node, int id) {
		this.node = node;
		this.id = id;
		starts = new ArrayList<>();
	}

	public int get_list_size() {
		return starts.size();
	}
	
	public NodeImpl get_node(int index) {
		return starts.get(index);
	}

	protected int get_id() {
		return id;
	}
}
