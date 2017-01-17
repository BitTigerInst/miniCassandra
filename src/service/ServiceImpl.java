package service;
import dht.node.NodeImpl.Operation;
import org.apache.log4j.Logger;
import rpc.IRpcMethod;

public class ServiceImpl implements IService{
	IRpcMethod IRpc;
	int clientId;
	Logger logger;

	public ServiceImpl(int id, IRpcMethod serviceProvider) {
		this.clientId = id;
		this.IRpc = serviceProvider;
		this.logger = Logger.getLogger(ServiceImpl.class);
	}

	/**
     *  PAD is short for 'put', 'append', 'delete'
	 *  because those three operation are quite similar
	 *  but GET need got a String type return
	 */
	@Override
	public void put(String key, String value) {
		IRpc.rpcCallPad(key, value, Operation.PUT);
		logger.debug("Client[" + clientId +"] send put key:" + key
				    + " value:" + value + " operation failed!");
	}

	@Override
	public String get(String key) {
		String result = IRpc.rpcCallGet(key);
		logger.debug("Client[" + clientId +"] send get result:" + result);
		return result;
	}

	@Override
	public void append(String key, String value) {
		IRpc.rpcCallPad(key, value, Operation.APPEND);
		logger.debug("Client[" + clientId +"] send append key:" + key
					+ " value:" + value + " operation failed!");
	}

	@Override
	public void delete(String key, String value) {
		IRpc.rpcCallPad(key, value, Operation.DELETE);
		logger.debug("Client[" + clientId +"] send delete key:" + key
					+ " value:" + value + " operation failed!");
	}
}