package service;
import dht.node.NodeImpl.Operation;
import util.Debug;
import rpc.*;

public class ServiceImpl implements IService{

	IRpcMethod IRpc;
	int client_id;

	public ServiceImpl(int id, IRpcMethod service_provider) {
		this.client_id = id;
		this.IRpc = service_provider;
	}

	//PAD is short for put append delete
	//those three operaion are quite similar
	//but GET need got a String type return
	@Override
	public void put(String key, String value) {
		boolean ok = IRpc.RPC_Call_PAD(key, value, Operation.PUT);
		if(!ok) {
			Debug.debug("Client[" + client_id +"] send put key:" + key 
					+ " value:" + value + " operation failed!");
		}
	}

	@Override
	public String get(String key) {
		String result = IRpc.RPC_Call_GET(key);
		Debug.debug("Client[" + client_id +"] send get result:" + result);
		return result;
	}

	@Override
	public void append(String key, String value) {
		boolean ok = IRpc.RPC_Call_PAD(key, value, Operation.APPEND);
		if(!ok) {
			Debug.debug("Client[" + client_id +"] send append key:" + key 
					+ " value:" + value + " operation failed!");
		}
	}

	@Override
	public void delete(String key, String value) {
		boolean ok = IRpc.RPC_Call_PAD(key, value, Operation.DELETE);
		if(!ok) {
			Debug.debug("Client[" + client_id +"] send delete key:" + key 
					+ " value:" + value + " operation failed!");
		}		
	}
	
}