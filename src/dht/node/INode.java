package dht.node;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import dht.chord.FingerTable;
import dht.node.NodeImpl.Operation;

public interface INode {

	public InetAddress get_addr();

	public int get_hashcode();

	public int get_port();

	public String exec(String key, String value, Operation oper);

	public void joinChordRing(NodeImpl node);
	
	public void leaveChordRing(NodeImpl node);
	
	public void destroy();
}
