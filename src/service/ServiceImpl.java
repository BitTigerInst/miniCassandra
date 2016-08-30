package service;
import dht.node.NodeImpl.Operation;
import util.Debug;
import rpc.IRpcMethod;

public class ServiceImpl implements IService{
	IRpcMethod IRpc;
	int clientId;

	public ServiceImpl(int id, IRpcMethod serviceProvider) {
		this.clientId = id;
		this.IRpc = serviceProvider;
	}

	/**
     *  PAD is short for 'put', 'append', 'delete'
	 *  because those three operation are quite similar
	 *  but GET need got a String type return
	 */
	@Override
	public void put(String key, String value) {
		IRpc.rpcCallPad(key, value, Operation.PUT);
		Debug.debug("Client[" + clientId +"] send put key:" + key
				    + " value:" + value + " operation failed!");
	}

	@Override
	public String get(String key) {
		String result = IRpc.rpcCallGet(key);
		Debug.debug("Client[" + clientId +"] send get result:" + result);
		return result;
	}

	@Override
	public void append(String key, String value) {
		IRpc.rpcCallPad(key, value, Operation.APPEND);
		Debug.debug("Client[" + clientId +"] send append key:" + key
					+ " value:" + value + " operation failed!");
	}

	@Override
	public void delete(String key, String value) {
		IRpc.rpcCallPad(key, value, Operation.DELETE);
		Debug.debug("Client[" + clientId +"] send delete key:" + key
					+ " value:" + value + " operation failed!");
	}
}