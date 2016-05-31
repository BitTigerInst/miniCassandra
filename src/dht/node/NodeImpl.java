package dht.node;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import leveldb.IStorageService;
import leveldb.StorageServiceImpl;
import rpc.IRpcMethod;
import rpc.RpcFramework;
import util.Debug;
import dht.chord.FingerTable;

public class NodeImpl implements INode, IRpcMethod{
	private int               RING_LEN;
	private InetSocketAddress address;
	private FingerTable       table;
	private InetSocketAddress predecessor;
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

	public static enum Type {
		JOIN,
		LEAVE,
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
		return address.getAddress();
	}

	public int get_hashcode() {
		return hashcode;
	}

	public int get_port() {
		return address.getPort();
	}

	/*
	 * judge if this operation own to current server
	 */
	public boolean is_belong_me(int hashcode) {
		int prede_hash = hash(predecessor.getAddress().hashCode());
		if(hashcode > prede_hash && hashcode <= this.get_hashcode()) {
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
			if(is_belong_me(hash(key.hashCode()))) {
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
		InetSocketAddress first_node = table.get_node(0);
		InetSocketAddress last_node = table.get_node(table.get_list_size() - 1);
		int first_node_hashcode = hash(first_node.getAddress().getHostAddress().hashCode());
		if(list_size==1 || hashcode==first_node_hashcode) {
			return 0;
		}
		int last_node_hashcode = hash(last_node.getAddress().getHostAddress().hashCode());
		if(hashcode>=last_node_hashcode || (hashcode>=0 && hashcode<first_node_hashcode)) {
			return list_size - 1;
		}
		for(int idx = 1;idx<=list_size-2;++idx) {
			InetSocketAddress node = table.get_node(idx);
			InetSocketAddress node_succ = table.get_node(idx + 1);
			int node_hashcode = hash(node.getAddress().getHostAddress().hashCode());
			int node_succ_hashcode = hash(node_succ.getAddress().getHostAddress().hashCode());
			if(hashcode>=node_hashcode && hashcode<node_succ_hashcode) {
				return idx;
			}
		}
		return -1;
	}

	/*
	 * send the operation to appropriate server
	 * by searching finger table
	 */
	private String send_to_other(InetSocketAddress addr, String key, String value, Operation oper) {
		try {
			IRpcMethod service = RpcFramework.refer(IRpcMethod.class, addr.getHostName(), addr.getPort());
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

	private InetSocketAddress get_successor() {
		if(table.get_list_size()>0) {
			return table.get_node(0);
		}else {
			return null;
		}
	}

	@Override
	public InetSocketAddress RPC_get_successor(int hashcode) {
		Debug.debug("calucalate the successor node for the new node");
		if(is_belong_me(hashcode)) {
			return address;
		}
		int idx = calculate(hashcode);
		InetSocketAddress addr = table.get_node(idx);
		try {
			IRpcMethod service = RpcFramework.refer(IRpcMethod.class, addr.getAddress().getHostAddress(), addr.getPort());
			return service.RPC_get_successor(hashcode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 *  send a joinChordRing rpc method request to another node
	 */
	@Override
	public void joinChordRing(NodeImpl node) {
		IRpcMethod service;
		try {
			service = RpcFramework.refer(IRpcMethod.class, node.get_addr().getHostAddress(), node.get_port());
			ArrayList<InetSocketAddress> table_list = service.RPC_JoinChordRing(this);
			table.set_finger_table_list(table_list);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Debug.debug("server " + this.get_hashcode() + " is joining ring succesfully");
	}

	/*
	 *  send a leaveChordRing rpc method request to another node
	 */
	@Override
	public void leaveChordRing() {
		try {
			InetSocketAddress successor = get_successor();
			if(successor==null) {
				return;
			}
			IRpcMethod service = RpcFramework.refer(IRpcMethod.class, 
														successor.getAddress().getHostAddress(), 
																successor.getPort());
			service.RPC_LeaveChordRing(this);
			Debug.debug("Server " + this.get_hashcode() + " is leaving the ring succesfully");
		} catch(Exception e) {
			e.printStackTrace();
		}
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

	@Override
	public ArrayList<InetSocketAddress> RPC_JoinChordRing(NodeImpl node) {
		ArrayList<InetSocketAddress> table_list = null;
		if(is_running) {
			if(is_stable) {
				is_stable = false;
				table_list = handle_join_chord_ring(node);
				is_stable = true;
			}else {
				Debug.debug("Warning:A server want to join a unstable chord ring!");
			}
		}
		return table_list;
	}
	
	private ArrayList<InetSocketAddress> handle_join_chord_ring(NodeImpl node) {
		ArrayList<InetSocketAddress> table_list = null;
		InetSocketAddress succ_addr = RPC_get_successor(hash(node.hashCode()));
		//1.generate a finger table for new server
		IRpcMethod service;
		try {
			service = RpcFramework.refer(IRpcMethod.class, succ_addr.getAddress().getHostAddress(), succ_addr.getPort());
			table_list = service.RPC_Succ_update_finger_table(Type.JOIN, node.get_hashcode());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//1.update finger table for other server?
		//  achieve this operation must need a RPC method
		
		
		//2.create a finger table for the new node
		
		//3.move data(belong to new node) from other server to new node
		return table_list;
	}

	@Override
	public void RPC_LeaveChordRing(NodeImpl successor) {
		if(is_running) {
			if(is_stable) {
				is_stable = false;
				handle_leave_chord_ring(successor);
				is_stable = true;
			}else {
				Debug.debug("Warning:A server want to join a unstable chord ring!");
			}
		}
	}

	private void handle_leave_chord_ring(NodeImpl node) {
		//1.update finger table for every server?
		RPC_Succ_update_finger_table(Type.LEAVE, node.get_hashcode());
		
		//2.move data from current server to other server
		
	}

	@Override
	public void RPC_UpdateAllServerFingerTable(Type type, int hashcode) {
		
	}

	public ArrayList<InetSocketAddress> create_finger_table() {
		ArrayList<InetSocketAddress> list = new ArrayList<>();
		int table_size = table.get_list_size();
		for(int i=0;i<table_size;++i) {
			InetSocketAddress addr = table.get_node(i);
			IRpcMethod service;
			try {
				service = RpcFramework.refer(IRpcMethod.class, addr.getAddress().getHostAddress(), addr.getPort());
				InetSocketAddress succ = service.RPC_get_succ();
				if(!list.contains(succ)) {
					list.add(succ);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	public void update_all_related_server_ftable() {
		//TODO
	}

	@Override
	public ArrayList<InetSocketAddress> RPC_Succ_update_finger_table(Type type, int hashcode) {
		switch(type) {
		case JOIN:
			//1.update the finger table of all related server
			update_all_related_server_ftable();
			//2.create a new Finger table for new node
			return create_finger_table();
		case LEAVE:
			//update the finger table of all related server
			update_all_related_server_ftable();
			break;
		}
		return null;
	}

	@Override
	public InetSocketAddress RPC_get_succ() {
		return get_successor();
	}
}