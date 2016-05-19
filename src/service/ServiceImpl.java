package service;
import java.net.*;
import dht.node.*;
import dht.node.NodeImpl.Operation;
import util.Debug;

public class ServiceImpl implements IService{

	InetSocketAddress socket;
	int client_id;
	ServiceImpl(int id, String ip, int port) {
		this.client_id = id;
		socket = new InetSocketAddress(ip, port);
	}
	
	//PAD is short for put append delete
	//those three operaion are quite similar
	//but GET need got a String type return
	@Override
	public void put(String key, String value) {
		boolean ok = RPC_Call_PAD(socket, key, value, Operation.PUT);
		if(!ok) {
			Debug.debug("Client[" + client_id +"] send put key:" + key 
					+ " value:" + value + " operation failed!");
		}
	}

	@Override
	public String get(String key) {
		String result = RPC_Call_GET(socket, key, Operation.GET);
		Debug.debug("Client[" + client_id +"] send get result:" + result);
		return result;
	}

	@Override
	public void append(String key, String value) {
		boolean ok = RPC_Call_PAD(socket, key, value, Operation.APPEND);
		if(!ok) {
			Debug.debug("Client[" + client_id +"] send append key:" + key 
					+ " value:" + value + " operation failed!");
		}		
	}

	@Override
	public void delete(String key, String value) {
		boolean ok = RPC_Call_PAD(socket, key, value, Operation.DELETE);
		if(!ok) {
			Debug.debug("Client[" + client_id +"] send delete key:" + key 
					+ " value:" + value + " operation failed!");
		}		
	}
	
}