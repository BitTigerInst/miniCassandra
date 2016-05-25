package rpc;

import dht.node.NodeImpl.Operation;

/**
 * Transport factory for establishing gRPC connections from clients to a remote server.
 */
public interface IRpcMethod {

	/*
	*  rpc interface for Operation Put, Append, Delete
	 */
	void RPC_Call_PAD(String key, String value, Operation oper);

	/*
	 *  rpc interface for Operation Get
	 */	
	String  RPC_Call_GET(String key);
}