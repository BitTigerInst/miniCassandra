package rpc;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import dht.node.NodeImpl;
import dht.node.NodeImpl.Operation;
import dht.node.NodeImpl.Type;

/**
 * Transport factory for establishing gRPC connections from clients to a remote server.
 */
public interface IRpcMethod {
	
	/*
	 * 
	 */
	void RPC_UpdateAllServerFingerTable(Type type, int hashcode);
	
	ArrayList<InetSocketAddress> RPC_Succ_update_finger_table(Type type, int hashcode);
	
	InetSocketAddress RPC_get_successor(int hashcode);
	
	InetSocketAddress RPC_get_succ();
	
	/*
	 *  rpc interface for join chord ring
	 */
	ArrayList<InetSocketAddress> RPC_JoinChordRing(NodeImpl node);
	
	/*
	 *  rpc interface for leave chord ring
	 */	
	void RPC_LeaveChordRing(NodeImpl node);
	
	/*
	 *  rpc interface for Operation Put, Append, Delete
	 */
	void RPC_Call_PAD(String key, String value, Operation oper);

	/*
	 *  rpc interface for Operation Get
	 */	
	String  RPC_Call_GET(String key);
}