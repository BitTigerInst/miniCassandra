package client;

import rpc.IRpcMethod;
import rpc.RpcFramework;
import service.*;
import util.Debug;
import java.util.*;
import dht.node.NodeImpl.Operation;

public class Client {
	IService 			  service;
	int                   client_id;
	ArrayList<String>     ip_list;
	ArrayList<Integer>    port_list;
	IRpcMethod            rpc_method_interface;

	private Client(int client_id, ArrayList<String> ip_list, ArrayList<Integer> port_list) {
		this.client_id = client_id;
		this.ip_list = ip_list;
		this.port_list = port_list;
		//select a server from server
		//list randomly and connect it.
		int idx = get_random_idx(ip_list.size());
		try {
			rpc_method_interface = RpcFramework.refer(IRpcMethod.class, ip_list.get(idx), port_list.get(idx));
			System.out.println("Client[" + client_id +"] connect server ip:" 
							+ ip_list.get(idx) + " port:" + port_list.get(idx));
		} catch (Exception e) {
			Debug.debug("RPC framework refer error!");
			e.printStackTrace();
		}
		this.service = new ServiceImpl(client_id, rpc_method_interface);
		System.out.println("Create a Client client_id:" + client_id);
	}

	public String exec(String key, String value, Operation oper) {
		switch(oper) {
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
	public int get_random_idx(int num) {
		return (int)(Math.random()*num);
	}

	public static Client CreateClient(int client_id, ArrayList<String> ip_list, ArrayList<Integer> port_list) {
		return new Client(client_id, ip_list, port_list);
	}

	public static void main(String[] args) {
		
	}
}
