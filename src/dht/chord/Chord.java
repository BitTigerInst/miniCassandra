package dht.chord;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import util.Debug;
import dht.node.*;

public class Chord {
	private ArrayList<INode> NodeList;
	private int RING_LEN;

	private static Chord chord;

	//initiate the finger table for this cluster
	//according to the ip_list and port_list
	public static ArrayList<FingerTable>  init_fingertable(ArrayList<String> ip_list, ArrayList<Integer>port_list) {
		
	}

	public static Chord CreateCluster(int num, int ring_len, ArrayList<String> ip_list, ArrayList<Integer>port_list) {
		if(chord!=null) {
			return chord;
		}

		if(ip_list.size()!=port_list.size() || ip_list.size()!=num) {
			Debug.debug("Cluster create failed, IP list or Port list size error");
			return null;
		}

		int cnt = 0;
		ArrayList<FingerTable> table_list = init_fingertable(ip_list, port_list);
		if(table_list.size()!=ip_list.size()) {
			Debug.debug("Cluster create failed, FingerTable list size error");
			return null;
		}

		while(cnt < num) {
			String addr = ip_list.get(cnt);
			int port = port_list.get(cnt);
			FingerTable table = table_list.get(cnt);
			NodeList.add(NodeImpl.createNode(addr, port, table, ring_len));
			++cnt;
		}

		chord = new Chord(NodeList, ring_len);
		return chord;
	}

	private Chord(ArrayList<INode> node_list, int RING_LEN) {
		this.NodeList = node_list;
		this.RING_LEN = RING_LEN;
	}
}
