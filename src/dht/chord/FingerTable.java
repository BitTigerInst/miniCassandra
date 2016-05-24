package dht.chord;

import dht.node.*;
import java.util.*;

public class FingerTable {
	protected NodeImpl   node;
	private   int        id;
	protected ArrayList<NodeImpl> starts;

	public FingerTable(NodeImpl node, int id) {
		this.node = node;
		this.id = id;
		starts = new ArrayList<>();
	}

	public NodeImpl get_node(int chord_id) {
		return starts.get(chord_id);
	}

	protected int get_id() {
		return id;
	}
}
