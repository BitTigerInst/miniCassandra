package rpc;

import java.net.InetSocketAddress;
import dht.node.NodeImpl.Operation;

/**
 * Transport factory for establishing gRPC connections from clients to a remote server.
 */
public interface ITransportFactory {
	/**
	 * Opens a client transport to a gRPC server.
	 */
	boolean RPC_Call_PAD(InetSocketAddress addr, String key, String value);

	String  RPC_Call_GET(InetSocketAddress addr, String key, Operation oper);
}
