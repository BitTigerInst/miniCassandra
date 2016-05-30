package dht.node;

import java.net.*;
import leveldb.IStorageService;
import leveldb.StorageServiceImpl;
import dht.chord.FingerTable;
import util.Debug;
import rpc.IRpcMethod;
import rpc.RpcFramework;

public class NodeImpl implements INode, IRpcMethod{
	private int               RING_LEN;
	private InetSocketAddress address;
	private FingerTable       table;
	private NodeImpl          successor;
	private NodeImpl    	  predecessor;
	private boolean           is_stable;
	private boolean           is_running;
	private IRpcMethod        Itrans;
	private IStorageService   storage_proxy;
	private int               hashcode;
	private RpcFramework      rpc_framework;
	private static int        file_count = 0;

	private NodeImpl(InetSocketAddress address, FingerTable fTable, int RING_LEN) throws Exception {
		this.RING_LEN = RING_LEN;
		this.address = address;
		this.hashcode = hash(this.hashCode());
		Itrans = this;
		storage_proxy = new StorageServiceImpl(this, generate_file_name());
		rpc_framework = new RpcFramework(true);
		rpc_framework.export(Itrans, address.getPort());
		System.out.println("Create Server Address:" + address.getHostString() + ", port:" + address.getPort());
	}
	
	public static enum Operation {
		PUT,
		APPEND,
		GET,
		DELETE,
	}

	private String generate_file_name() {
		return get_addr().toString() + "_" + file_count++;
	}

	public static INode createNode(String ip, int port, FingerTable fTable, int RING_LEN) throws Exception {
		InetSocketAddress address = new InetSocketAddress(ip, port);
		return new NodeImpl(address, fTable, RING_LEN);
	}

	@Override
	public void destroy() {
		this.storage_proxy.destroy();	
		this.rpc_framework.destroy();
	}
	
	public InetAddress get_addr() {
		return this.address.getAddress();
	}

	public int get_hashcode() {
		return hashcode;
	}

	public int get_port() {
		return this.address.getPort();
	}

	/*
	 * judge if this operation own to current server
	 */
	public boolean is_belong_me(String key) {
		int hashcode = hash(key.hashCode());
		if(hashcode > this.predecessor.get_hashcode() && hashcode <= this.get_hashcode()) {
			return true;
		}else {
			return false;
		}
	}

	/* 
	 * Firstly calculating the owner for this
	 * operation according to the hashcode of key.
	 * Secondly executing the operation.
	 */
	public String exec(String key, String value, Operation oper) {
		if(is_stable && is_running) {
			if(is_belong_me(key)){
				switch(oper) {
				case PUT:
					storage_proxy.put(key, value);
					break;
				case APPEND:
					storage_proxy.append(key, value);
					break;
				case GET:
					return storage_proxy.get(key);
				case DELETE:
					storage_proxy.delete(key);
					break;
				}
			}else {
				int id = calculate(hash(key.hashCode()));
				return send_to_other(table.get_node(id), key, value, oper);
			}
		}else if(!is_stable && is_running){
			
		}else {
			Debug.debug("Warning: this server is not alived!!");
		}
		return null;
	}

	/*
	 * caluculate the correct or the most close server id for this hashcode
	 */
	private int calculate(int hashcode) {
		int list_size = table.get_list_size();
		NodeImpl first_node = table.get_node(0);
		NodeImpl last_node = table.get_node(table.get_list_size() - 1);
		if(list_size==1 || hashcode==first_node.hashcode) {
			return 0;
		}
		if(hashcode>=last_node.hashcode || (hashcode>=0 && hashcode<first_node.hashcode)){
			return list_size - 1;
		}
		for(int idx = 1;idx<=list_size-2;++idx) {
			NodeImpl node = table.get_node(idx);
			NodeImpl node_succ = table.get_node(idx + 1);
			if(hashcode>=node.hashcode && hashcode<node_succ.hashcode) {
				return idx;
			}
		}
		return -1;
	}

	/*
	 * send the operation to appropriate server
	 * by searching finger table
	 */
	private String send_to_other(NodeImpl node, String key, String value, Operation oper) {
		try {
			IRpcMethod service = RpcFramework.refer(IRpcMethod.class, node.get_addr().getHostAddress(), node.get_port());
			switch(oper) {
			case GET:
				return service.RPC_Call_GET(key);
			default:
				service.RPC_Call_PAD(key, value, oper);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void joinChordRing(NodeImpl node) {
	    //TODO
		
	}

	@Override
	public void leaveChordRing(NodeImpl node) {
		//TODO
		
	}

	public int hash(int id) {
		return id % RING_LEN;
	}

	@Override
	public void RPC_Call_PAD(String key, String value, Operation oper) {
		exec(key, value, oper);
	}

	@Override
	public String RPC_Call_GET(String key) {
		return exec(key, null, Operation.GET);
	}
}