package dht.node;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import dht.chord.FingerTable;
import dht.node.NodeImpl.Operation;

public interface INode {

	public InetAddress getAddr();

	public int getHashcode();

	public int getPort();

	public String exec(String key, String value, Operation oper);

	public void joinChordRing(NodeImpl node);

	public void leaveChordRing();

	public void destroy();
}
