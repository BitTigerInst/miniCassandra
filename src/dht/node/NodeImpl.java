package dht.node;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.lang.Math;
import java.util.ArrayList;

import javax.xml.crypto.Data;

import leveldb.IStorageService;
import leveldb.StorageServiceImpl;
import rpc.IRpcMethod;
import rpc.RpcFramework;
import util.Debug;
import dht.chord.FingerTable;
import dht.node.NodeImpl.Type;

public class NodeImpl implements INode, IRpcMethod{
	private int               RING_LEN;
	private int				  bits;
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
	private static int        MAX_WAIT_STABLE_CYCLE = 30;
	private static int        WAIT_STABLE_CYCLE_MS = 1000;

	private NodeImpl(InetSocketAddress address, int bits) throws Exception {
		this.bits = bits;
		this.RING_LEN = (int) Math.pow(2.0, bits);
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

	public static INode createNode(String ip, int port, int RING_LEN) throws Exception {
		InetSocketAddress address = new InetSocketAddress(ip, port);
		return new NodeImpl(address, RING_LEN);
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
	
	
	private boolean in_range(int id, int left, int right) {
		// three cases
		if ((left - right == 0 && left == id) ||
				(left - right < 0 && id > left && id <= right) ||
				(left - right > 0 && ((id >= 0 && id <= right) ||
													(id < RING_LEN && id > left)))) {
			return true;
		} else {
			return false;
		}
	}
	
	/*
	 * judge if this operation own to current server
	 */
	public boolean is_belong_me(int hashcode) {
		int prede_hash = hash(predecessor.getAddress().hashCode());
		return in_range(hashcode, prede_hash, this.get_hashcode());
	}

	/*
	 * wait the cluster be stable
	 */
	public void wait_stable() {
		int cycle = 0;
		while (!is_stable && cycle < MAX_WAIT_STABLE_CYCLE) {
			try {
				Thread.sleep(WAIT_STABLE_CYCLE_MS);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			++cycle;
		}
		if (cycle==MAX_WAIT_STABLE_CYCLE) {
			throw new RuntimeException("Join Chord Ring failure");
		}
	}

	/* 
	 * Firstly calculating the owner for this
	 * operation according to the hashcode of key.
	 * Secondly executing the operation.
	 */
	public String exec(String key, String value, Operation oper) {
		if (is_stable && is_running) {
			if (is_belong_me(hash(key.hashCode()))) {
				switch (oper) {
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
			} else {
				int id = calculate(hash(key.hashCode()));
				return send_to_other(table.get_node(id), key, value, oper);
			}
		} else if(!is_stable && is_running) {
			wait_stable();
			exec(key, value, oper);
		} else {
			throw new IllegalArgumentException("Warning: this server is not alived!!");
		}
		return null;
	}

	/*
	 * caluculate the correct or the most close server id for this hashcode
	 */
	private int calculate(int hashcode) {
		// table size is fixed? log_2^(ring_len)
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

	
	private InetSocketAddress get_predecessor() {
		return predecessor;
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
	 *  send a joinChordRing rpc method request to a known node
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
        this.is_running = true;
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
			service.RPC_LeaveChordRing();
			Debug.debug("Server " + this.get_hashcode() + " is leaving the ring succesfully");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public int hash(int id) {
		return (id+RING_LEN) % RING_LEN;
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
				wait_stable();
				Debug.debug("Warning:A server want to join a unstable chord ring!");
			}
		}
		return table_list;
	}
	
	private ArrayList<InetSocketAddress> init_finger_table(InetSocketAddress succ_addr) {
		ArrayList<InetSocketAddress> table_list = new ArrayList<InetSocketAddress>();
		InetSocketAddress next_addr = succ_addr;
		int next_id = next_addr.hashCode();
		int add = 1;
		for (int i = 0; i < bits; i++) {
			if (!in_range(next_id, hashcode, hash(hashcode + add))) {
				next_addr = RPC_get_successor(hash(hashcode + add));
				next_id = next_addr.hashCode();
			}
			table_list.add(next_addr);
			add = 2*add;
		}
		return table_list;
	}
	
	private void update_others(Type type) {
		int add = 1;
		for (int i = 0; i < bits; i++, add *=2) {
			InetSocketAddress p = RPC_get_predecessor(hash(hashcode-add));//mod Size
			IRpcMethod service;
			try {
				service = RpcFramework.refer(IRpcMethod.class, p.getAddress().getHostAddress(), p.getPort());
				service.RPC_UpdateServerFingerTable(type, p, hashcode, i);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	private void move_data(InetSocketAddress succ_addr) {
		IRpcMethod service;
		ArrayList<String> data = new ArrayList<>();
		try {
			service = RpcFramework.refer(IRpcMethod.class, succ_addr.getAddress().getHostAddress(), succ_addr.getPort());
			data = service.RPC_get_remotedatq();
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (int i = 0; i < data.size(); i+=2) {
			exec(data.get(i), data.get(i + 1), Operation.PUT);
		}
	}
	
	private ArrayList<InetSocketAddress> handle_join_chord_ring(NodeImpl node) {
		ArrayList<InetSocketAddress> table_list;
		InetSocketAddress succ_addr = RPC_get_successor(hash(node.hashCode()));
		IRpcMethod service;
		try {
			service = RpcFramework.refer(IRpcMethod.class, succ_addr.getAddress().getHostAddress(), succ_addr.getPort());
            InetSocketAddress predecessor = service.RPC_get_pred();
			service.RPC_change_pred(node.address);
            service = RpcFramework.refer(IRpcMethod.class, predecessor.getAddress().getHostAddress(), predecessor.getPort());
            service.RPC_change_succ(node.address);
        } catch (Exception e) {
			e.printStackTrace();
		}
		// init finger table
		table_list = init_finger_table(succ_addr);
		// update node info
		update_others(Type.JOIN);
		// move the data
		move_data(succ_addr);
		
		return table_list;
	}

	@Override
	public ArrayList<String> RPC_get_remotedatq(){
		DBIterator iterator = storage_proxy.db.iterator();
		ArrayList<String> ret = new ArrayList<String>();
		try {
		  for(iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
		    String key = asString(iterator.peekNext().getKey());
		    String value = asString(iterator.peekNext().getValue());
		    if (!is_belong_me(hash(key.hashCode()))) {
		    	ret.add(key);
		    	ret.add(value);
		    	storage_proxy.delete(key);
		    }
		    //System.out.println(key+" = "+value);
		  }
		} finally {
		  // Make sure you close the iterator to avoid resource leaks.
		  iterator.close();
		}
		return ret;
	}
	
	@Override
	public void RPC_LeaveChordRing() {
		if(is_running) {
			if(is_stable) {
				is_stable = false;
				handle_leave_chord_ring();
				is_stable = true;
			}else {
				Debug.debug("Warning:A server want to join a unstable chord ring!");
			}
		}
	}

	private void handle_leave_chord_ring() {
		//1.update finger table for every server?
		update_others(Type.LEAVE);
		
		//2.move data from current server to other server
		// unsure about it
		// relocate_data();
	}

	@Override
	public void RPC_UpdateServerFingerTable(Type type, InetSocketAddress addr, int node_hashcode, int i) {
		// the boundary is not clear
		if (in_range(node_hashcode, this.hashcode, hash(table.get_node(i).hashCode()))) {
			switch(type) {
			case JOIN:
				table.replace(i, addr);
				break;
			case LEAVE:
				int ithID = this.hashcode + (int)Math.pow(2, i);
				InetSocketAddress new_addr = RPC_get_successor(ithID);
				table.replace(i, new_addr);
				break;
			}
			InetSocketAddress next = table.get_node(0);
			IRpcMethod service;
			try {
				service = RpcFramework.refer(IRpcMethod.class, next.getAddress().getHostAddress(), next.getPort());
				service.RPC_UpdateServerFingerTable(type, addr, node_hashcode, i);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
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
/*
	public void update_all_related_server_ftable(Type type) {
		switch(type) {
		case JOIN:
			
			break;
		case LEAVE:
			
			break;
		}
	}

	@Override
	public ArrayList<InetSocketAddress> RPC_Succ_update_finger_table(Type type, int hashcode) {
		switch(type) {
		case JOIN:
			//1.update the finger table of all related server
			update_all_related_server_ftable(type);
			//2.create a new Finger table for new node
			return create_finger_table();
		case LEAVE:
			//update the finger table of all related server
			update_all_related_server_ftable(type);
			break;
		}
		return null;
	}
	*/

	@Override
	public InetSocketAddress RPC_get_succ() {
		return get_successor();
	}
	
	@Override
	public InetSocketAddress RPC_get_pred() {
		return get_predecessor();
	}
	
	@Override
	public void RPC_change_pred(InetSocketAddress addr) {
		predecessor = addr;
	}

    @Override
    public void RPC_change_succ(InetSocketAddress addr) {
        table.replace(0, addr);
    }

	@Override
	public InetSocketAddress RPC_get_predecessor(int hashcode) {
		InetSocketAddress succ = RPC_get_successor(hashcode);
		IRpcMethod service;
		InetSocketAddress pred = null;
		try {
			service = RpcFramework.refer(IRpcMethod.class, succ.getAddress().getHostAddress(), succ.getPort());
			pred = service.RPC_get_pred();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pred;
	}
}