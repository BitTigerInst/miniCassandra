package client;

import rpc.ITransportFactory;
import rpc.RpcFramework;
import service.*;
import util.Debug;

import java.util.*;
import java.math.*;
public class client {

	IService 			  service_provider;
	int                   client_id;
	ArrayList<String>     ip_list;
	ArrayList<Integer>    port_list;
	ITransportFactory     service;

	private client() {
		int idx = get_random_idx(ip_list.size());
		try {
			service = RpcFramework.refer(ITransportFactory.class, ip_list.get(idx), port_list.get(idx));
		} catch (Exception e) {
			Debug.debug("RPC framework refer error!");
			e.printStackTrace();
		}
	}

	public int get_random_idx(int num) {
		return (int)(Math.random()*num);
	}

	public static void main(String[] args) {
		
		
	}

}
