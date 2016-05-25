package dht.chord;

import java.util.ArrayList;
import util.Debug;
import dht.node.INode;
import dht.node.NodeImpl;

public class Chord {
	private static ArrayList<INode> NodeList = new ArrayList<>();
	private static Chord chord = null;
	private int RING_LEN;

	//initiate the finger table for this cluster
	//according to the ip_list and port_list
	public static ArrayList<FingerTable>  init_fingertable(ArrayList<String> ip_list, ArrayList<Integer>port_list) {
		ArrayList<FingerTable> finger_table = new ArrayList<>();
		//TODO
		
		
		return finger_table;
	}

	public ArrayList<INode> get_node_list() {
		return NodeList;
	}

	public static Chord CreateCluster(int num, int ring_len, ArrayList<String> ip_list, ArrayList<Integer>port_list) {
		if(chord!=null) {
			return chord;
		}

		if(ip_list.size()!=port_list.size() || ip_list.size()!=num) {
			throw new IllegalArgumentException("Cluster create failed, IP list or Port list size error");
		}

		ArrayList<FingerTable> table_list = init_fingertable(ip_list, port_list);
		if(table_list.size()!=ip_list.size()) {
			throw new IllegalArgumentException("Cluster create failed, FingerTable list size error");
		}

		int cnt = 0;
		while(cnt < num) {
			String addr = ip_list.get(cnt);
			int port = port_list.get(cnt);
			FingerTable table = table_list.get(cnt);
			try {
				NodeList.add(NodeImpl.createNode(addr, port, table, ring_len));
			} catch (Exception e) {
				e.printStackTrace();
			}
			++cnt;
		}
		chord = new Chord(ring_len);
		return chord;
	}

	public static void DestroyCluster() {
		for(INode node:NodeList) {
			node.destroy();
		}
	}
	
	private Chord(int RING_LEN) {
		this.RING_LEN = RING_LEN;
		Debug.debug("Creating a cluster RING_LEN:" + RING_LEN + 
						", contain " + NodeList + "nodes.");
	}
}
