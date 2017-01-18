package rpc;

import dht.chord.FingerTable;
import dht.node.NodeImpl;
import dht.node.NodeImpl.Operation;
import dht.node.NodeImpl.Type;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.ArrayList;

/**
 * Transport factory for establishing gRPC connections from clients to a remote server.
 */
public interface IRpcMethod extends Serializable {

	void rpcUpdateServerFingerTable(Type type, InetSocketAddress addr);

	InetSocketAddress rpcGetSuccessor(int hashcode);
	
	InetSocketAddress rpcGetPredecessor(int hashcode);
	
	InetSocketAddress rpcGetSucc();
	
	InetSocketAddress rpcGetPred();

	FingerTable rpcGetFingerTable();
	
	ArrayList<String> rpcGetRemotedatq() throws IOException;
	
	void rpcChangePred(InetSocketAddress addr);
	
	/*
	 *  rpc interface for join chord ring
	 */
	ArrayList<InetSocketAddress> rpcJoinChordRing(NodeImpl node) throws Exception;

	/*
	 *  rpc interface for leave chord ring
	 */	
	void rpcLeaveChordRing();

	/*
	 *  rpc interface for Operation Put, Append, Delete
	 */
	void rpcCallPad(String key, String value, Operation oper);

	/*
	 *  rpc interface for Operation Get
	 */	
	String rpcCallGet(String key);
}