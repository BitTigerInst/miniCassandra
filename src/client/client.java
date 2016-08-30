package client;

import rpc.IRpcMethod;
import rpc.RpcFramework;
import service.*;
import util.Debug;
import java.util.*;
import dht.node.NodeImpl.Operation;

public class Client {
	IService 			  service;
	int                   clientId;
	ArrayList<String>     ipList;
	ArrayList<Integer>    portList;
	IRpcMethod            rpcMethodInterface;

	private Client(int clientId, ArrayList<String> ipList, ArrayList<Integer> portList) {
		this.clientId = clientId;
		this.ipList = ipList;
		this.portList = portList;
		//select a server from server
		//list randomly and connect it.
		int idx = getRandomIdx(ipList.size());
		try {
			rpcMethodInterface = RpcFramework.refer(IRpcMethod.class, ipList.get(idx), portList.get(idx));
			System.out.println("Client[" + clientId +"] connect server ip:"
							+ ipList.get(idx) + " port:" + portList.get(idx));
		} catch (Exception e) {
			Debug.debug("RPC framework refer error!");
			e.printStackTrace();
		}
		this.service = new ServiceImpl(clientId, rpcMethodInterface);
		System.out.println("Create a Client clientId:" + clientId);
	}

	public String exec(String key, String value, Operation oper) {
		switch (oper) {
			case PUT:
				service.put(key, value);
				break;
			case APPEND:
				service.append(key, value);
				break;
			case DELETE:
				service.delete(key, value);
				break;
			case GET:
				return service.get(key);
		}
		return null;
	}

	//generate a random number between 0~num-1
	public int getRandomIdx(int num) {
		return (int)(Math.random() * num);
	}

	public static Client CreateClient(int clientId, ArrayList<String> ipList, ArrayList<Integer> portList) {
		return new Client(clientId, ipList, portList);
	}

	public static void main(String[] args) {
		
	}
}
