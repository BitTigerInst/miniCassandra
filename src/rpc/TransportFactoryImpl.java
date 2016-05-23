package rpc;

import java.net.InetSocketAddress;
import dht.node.NodeImpl.Operation;

public class TransportFactoryImpl implements ITransportFactory{

	@Override
	public boolean RPC_Call_PAD(InetSocketAddress addr, String key, String value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String RPC_Call_GET(InetSocketAddress addr, String key,
			Operation oper) {
		// TODO Auto-generated method stub
		return null;
	}

}
