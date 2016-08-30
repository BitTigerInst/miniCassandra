package dht.node;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import dht.chord.FingerTable;
import dht.node.NodeImpl.Operation;

public interface INode extends Serializable{

	InetAddress getAddr();

	int getHashcode();

	int getPort();

	String exec(String key, String value, Operation oper);

	void joinChordRing(NodeImpl node);

	void leaveChordRing();

	void destroy();
}
