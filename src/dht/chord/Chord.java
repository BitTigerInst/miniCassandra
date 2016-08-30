package dht.chord;

import java.util.ArrayList;
import util.Debug;
import dht.node.INode;
import dht.node.NodeImpl;

public class Chord {
	private static ArrayList<INode> NodeList = new ArrayList<INode>();
	private static Chord chord = null;
	private int length;
	//private int RING_LEN;

	//initiate the finger table for this cluster
	//according to the ip_list and port_list
	public static ArrayList<FingerTable>  init_fingertable(ArrayList<String> ip_list, ArrayList<Integer>port_list) {
		ArrayList<FingerTable> finger_table = new ArrayList<FingerTable>();
		//TODO
		
		
		return finger_table;
	}

	public ArrayList<INode> getNodeList() {
		return NodeList;
	}
	
	public boolean addNode(String ip, Integer port) {
		
		return true;
	}

	public static Chord CreateCluster(int num, int ringLen, ArrayList<String> ipList, ArrayList<Integer>portList) {
		if (chord != null) {
			return chord;
		}

		if (ipList.size() != portList.size() || ipList.size() != num) {
			throw new IllegalArgumentException("Cluster create failed, IP list or Port list size error");
		}

		for (int cnt = 0;cnt < ipList.size();++cnt) {
			String addr = ipList.get(cnt);
			int port = portList.get(cnt);
			//FingerTable table = table_list.get(cnt);
			try {
				NodeList.add(NodeImpl.createNode(addr, port, ringLen));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		chord = new Chord(ringLen);
		return chord;
	}

	public static void DestroyCluster() {
		for(INode node:NodeList) {
			node.destroy();
		}
	}
	
	private Chord(int n) {
		this.length = n;
		Debug.debug("Creating a cluster RING_LEN:" + length + ", contain " + NodeList + "nodes.");
	}
}
