package dht.chord;

import dht.node.NodeImpl;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;

public class Chord implements Serializable {
	private static ArrayList<NodeImpl> NodeList = new ArrayList<>();
	private static transient Chord chord = null;
	private int length;
	private static Logger logger = Logger.getLogger(Chord.class);
	//private int RING_LEN;

	/**
	 * initiate the finger table for this cluster
	 * according to the ipList and portList
	 */
	public static ArrayList<FingerTable> initFingertable(ArrayList<String> ipList, ArrayList<Integer> portList) {
		ArrayList<FingerTable> fingerTables = new ArrayList<>();
		//TODO
		return fingerTables;
	}

	public ArrayList<NodeImpl> getNodeList() {
		return NodeList;
	}

	public static Chord CreateCluster(int num, int ringLen, ArrayList<String> ipList, ArrayList<Integer>portList) throws Exception {
		if (ipList.size() != portList.size() || ipList.size() != num) {
			throw new IllegalArgumentException("Cluster create failed, the size of IP list should equal to the size of Port list");
		}
		if (chord != null) return chord;
		if (ipList.size() <= 0) return null;
		//1. start first node
		NodeImpl firstNode =  NodeImpl.createNode(ipList.get(0), portList.get(0), ringLen);
		firstNode.joinChordRing(null);
		firstNode.start();
		//2. start other nodes
		for (int cnt = 1; cnt < ipList.size(); cnt++) {
			String ip = ipList.get(cnt);
			int port = portList.get(cnt);
			NodeImpl node = NodeImpl.createNode(ip, port, ringLen);
			node.joinChordRing(firstNode);
			node.start();
		}
		chord = new Chord(ringLen);
		return chord;
	}

	public static void DestroyCluster() {
		NodeList.forEach(NodeImpl::destroyNode);
	}
	
	private Chord(int n) {
		this.length = n;
        logger.debug("Creating a cluster RING_LEN:" + length + ", contain " + NodeList + "nodes.");
	}
}