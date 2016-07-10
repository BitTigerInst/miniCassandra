package rpc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import dht.node.NodeImpl;
import dht.node.NodeImpl.Operation;
import dht.node.NodeImpl.Type;

/**
 * Transport factory for establishing gRPC connections from clients to a remote server.
 */
public interface IRpcMethod {

	void RPC_UpdateServerFingerTable(Type type, InetSocketAddress addr,int hashcode, int i);

	// ArrayList<InetSocketAddress> RPC_Succ_update_finger_table(Type type, int hashcode);
	
	InetSocketAddress RPC_get_successor(int hashcode);
	
	InetSocketAddress RPC_get_predecessor(int hashcode);
	
	InetSocketAddress RPC_get_succ();
	
	InetSocketAddress RPC_get_pred();
	
	ArrayList<String> RPC_get_remotedatq() throws IOException;
	
	void RPC_change_pred(InetSocketAddress addr);

	void RPC_change_succ(InetSocketAddress addr);

	/*
	 *  rpc interface for join chord ring
	 */
	ArrayList<InetSocketAddress> RPC_JoinChordRing(NodeImpl node);

	/*
	 *  rpc interface for leave chord ring
	 */	
	void RPC_LeaveChordRing();

	/*
	 *  rpc interface for Operation Put, Append, Delete
	 */
	void RPC_Call_PAD(String key, String value, Operation oper);

	/*
	 *  rpc interface for Operation Get
	 */	
	String  RPC_Call_GET(String key);
}